package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mcp.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handlers for MEM domain (merchant order hold/unhold and future MEM operations).
 */
public class MerchantOrdersHandlers {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String STORE_ID = "69569216732";

    /**
     * Common function to fetch order details by order name.
     * Returns a map containing the apporderid and the full order details response.
     * 
     * @param orderName The order name (e.g., "#21321")
     * @return Map with "apporderid" (String) and "orderDetails" (JsonNode)
     * @throws Exception if order not found or apporderid cannot be extracted
     */
    public static Map<String, Object> fetchOrderDetailsByName(String orderName) throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("order_name", orderName);
        queryParams.put("store_id", STORE_ID);

        String orderDetailsResponse = Utils.httpGet(
                "/api/merchant/orders/byNameAndStore",
                queryParams
        );

        // Parse the response to extract apporderid
        JsonNode orderDetailsJson = mapper.readTree(orderDetailsResponse);
        String apporderid = null;

        // Helper function to extract apporderid from a JsonNode
        // Checks for both "apporderid" and "appOrderId" (camelCase)
        java.util.function.Function<JsonNode, String> extractAppOrderId = (node) -> {
            if (node.has("appOrderId")) {
                return node.get("appOrderId").asText();
            } else if (node.has("apporderid")) {
                return node.get("apporderid").asText();
            }
            return null;
        };

        // Try to extract apporderid from the response
        // The response might be an object with apporderid field, or an array with objects containing apporderid
        // Response structure: {"data": [{"appOrderId": "...", ...}]}
        if (orderDetailsJson.has("data")) {
            JsonNode data = orderDetailsJson.get("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode firstOrder = data.get(0);
                apporderid = extractAppOrderId.apply(firstOrder);
            } else if (!data.isArray()) {
                apporderid = extractAppOrderId.apply(data);
            }
        } else if (orderDetailsJson.isArray() && orderDetailsJson.size() > 0) {
            JsonNode firstOrder = orderDetailsJson.get(0);
            apporderid = extractAppOrderId.apply(firstOrder);
        } else {
            apporderid = extractAppOrderId.apply(orderDetailsJson);
        }

        if (apporderid == null || apporderid.isEmpty()) {
            throw new Exception("Could not find apporderid in order details response. Response: " + orderDetailsResponse);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("apporderid", apporderid);
        result.put("orderDetails", orderDetailsJson);
        result.put("orderDetailsResponse", orderDetailsResponse);
        return result;
    }

    /**
     * Handle hold/unhold status for merchant orders based on order_name.
     * First fetches order details by name to get apporderid, then calls hold/unhold API.
     */
    public static Object handleHoldStatus(JsonNode arguments) throws Exception {
        String orderName = arguments.get("order_name").asText();
        boolean hold = arguments.get("hold").asBoolean();

        // Step 1: Fetch order details by name to get apporderid
        Map<String, Object> orderDetails = fetchOrderDetailsByName(orderName);
        String apporderid = (String) orderDetails.get("apporderid");
        String orderDetailsResponse = (String) orderDetails.get("orderDetailsResponse");

        // Step 2: Call hold/unhold API with apporderid
        List<String> appOrderIds = new ArrayList<>();
        appOrderIds.add(apporderid);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("appOrderIds", appOrderIds);
        payload.put("isHold", hold);

        String jsonBody = mapper.writeValueAsString(payload);

        String holdStatusResponse = Utils.httpPostJson(
                "/api/merchant/orders/holdStatus",
                jsonBody
        );

        Map<String, Object> result = new HashMap<>();
        result.put("type", "mem_hold_status");
        result.put("order_name", orderName);
        result.put("apporderid", apporderid);
        result.put("hold", hold);
        result.put("order_details_response", orderDetailsResponse);
        result.put("hold_status_response", holdStatusResponse);
        return result;
    }
}


