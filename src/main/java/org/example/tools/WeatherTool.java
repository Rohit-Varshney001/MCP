package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.*;

public class WeatherTool implements Tool {

    private final List<String> randomCities = List.of("Delhi", "London", "Tokyo", "New York", "Mumbai");

    @Override
    public Map<String, Object> getSchema() {
        Map<String, Object> properties = new HashMap<>();

        // Weather-related params
        properties.put("city", Map.of(
                "type", "string",
                "description", "City name to get weather for"
        ));
        properties.put("latitude", Map.of(
                "type", "number",
                "description", "Latitude coordinate (use with longitude)"
        ));
        properties.put("longitude", Map.of(
                "type", "number",
                "description", "Longitude coordinate (use with latitude)"
        ));
        properties.put("forecast", Map.of(
                "type", "boolean",
                "description", "If true, returns 5-day forecast (requires city parameter)"
        ));

        // Inventory forecast / reorder suggestion
        properties.put("sku", Map.of(
                "type", "string",
                "description", "Inventory SKU for inventory forecast or reorder suggestion"
        ));
        properties.put("lookaheadDays", Map.of(
                "type", "integer",
                "description", "Lookahead days for inventory forecast (default 30)"
        ));
        properties.put("reorderSuggestion", Map.of(
                "type", "boolean",
                "description", "If true with sku, calls inventory reorder-suggestion endpoint instead of forecast"
        ));

        // Churn forecast
        properties.put("customerId", Map.of(
                "type", "string",
                "description", "Customer ID for churn risk"
        ));
        properties.put("cohortId", Map.of(
                "type", "string",
                "description", "Cohort ID for churn cohort analysis"
        ));

        // Sales forecast
        properties.put("product", Map.of(
                "type", "string",
                "description", "Product for sales forecast (default all-products)"
        ));
        properties.put("region", Map.of(
                "type", "string",
                "description", "Region for sales forecast/simulation (default global)"
        ));
        properties.put("scenario", Map.of(
                "type", "string",
                "description", "Scenario for sales simulation (e.g. promo-heavy, baseline)"
        ));

        return Map.of(
                "name", "get_weather",
                "description", "Fetch weather information and business forecasts. Supports: current weather, 5-day weather forecast, inventory forecast/reorder suggestion, churn risk/cohort analysis, and sales forecast/simulation.",
                // MCP spec expects camelCase key name
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", properties,
                        "required", new String[]{}  // Nothing is required - all params are optional
                )
        );
    }

    @Override
    public Object run(JsonNode arguments) throws Exception {
        // Priority:
        // 1. Business apps (inventory / churn / sales)
        // 2. Weather forecast (if city + forecast)
        // 3. Weather by city
        // 4. Weather by coordinates
        // 5. Random city weather

        // ===== Inventory endpoints =====
        if (arguments.has("sku") && !arguments.get("sku").isNull()) {
            String sku = arguments.get("sku").asText();

            boolean reorder = arguments.has("reorderSuggestion")
                    && !arguments.get("reorderSuggestion").isNull()
                    && arguments.get("reorderSuggestion").asBoolean();

            if (reorder) {
                String url = "http://localhost:8080/apps/inventory/reorder-suggestion?sku=" + sku;
                return Map.of(
                        "type", "inventory_reorder_suggestion",
                        "sku", sku,
                        "data", Utils.httpGet(url)
                );
            } else {
                int lookaheadDays = 30;
                if (arguments.has("lookaheadDays") && !arguments.get("lookaheadDays").isNull()) {
                    lookaheadDays = arguments.get("lookaheadDays").asInt();
                }
                String url = "http://localhost:8080/apps/inventory/forecast?sku=" + sku + "&lookaheadDays=" + lookaheadDays;
                return Map.of(
                        "type", "inventory_forecast",
                        "sku", sku,
                        "lookaheadDays", lookaheadDays,
                        "data", Utils.httpGet(url)
                );
            }
        }

        // ===== Churn endpoints =====
        if (arguments.has("customerId") && !arguments.get("customerId").isNull()) {
            String customerId = arguments.get("customerId").asText();
            String url = "http://localhost:8080/apps/churn/risk?customerId=" + customerId;
            return Map.of(
                    "type", "churn_risk",
                    "customerId", customerId,
                    "data", Utils.httpGet(url)
            );
        }

        if (arguments.has("cohortId") && !arguments.get("cohortId").isNull()) {
            String cohortId = arguments.get("cohortId").asText();
            String url = "http://localhost:8080/apps/churn/cohort/" + cohortId;
            return Map.of(
                    "type", "churn_cohort",
                    "cohortId", cohortId,
                    "data", Utils.httpGet(url)
            );
        }

        // ===== Sales endpoints =====
        if (arguments.has("product") || arguments.has("region") || arguments.has("scenario")) {
            String region = "global";
            if (arguments.has("region") && !arguments.get("region").isNull()) {
                region = arguments.get("region").asText();
            }

            if (arguments.has("scenario") && !arguments.get("scenario").isNull()) {
                String scenario = arguments.get("scenario").asText();
                String url = "http://localhost:8080/apps/sales/simulate?scenario=" + scenario + "&region=" + region;
                return Map.of(
                        "type", "sales_simulation",
                        "scenario", scenario,
                        "region", region,
                        "data", Utils.httpGet(url)
                );
            } else {
                String product = "all-products";
                if (arguments.has("product") && !arguments.get("product").isNull()) {
                    product = arguments.get("product").asText();
                }
                String url = "http://localhost:8080/apps/sales/forecast?product=" + product + "&region=" + region;
                return Map.of(
                        "type", "sales_forecast",
                        "product", product,
                        "region", region,
                        "data", Utils.httpGet(url)
                );
            }
        }

        // ===== Weather endpoints =====
        // Check if forecast is requested with city
        boolean isForecast = arguments.has("forecast") && !arguments.get("forecast").isNull() && arguments.get("forecast").asBoolean();
        if (isForecast && arguments.has("city") && !arguments.get("city").isNull()) {
            String city = arguments.get("city").asText();
            String url = "http://localhost:8080/weather/" + city + "/forecast";
            return Map.of(
                    "type", "forecast",
                    "city", city,
                    "forecast", Utils.httpGet(url)
            );
        }

        // Check if city is provided
        if (arguments.has("city") && !arguments.get("city").isNull()) {
            String city = arguments.get("city").asText();
            String url = "http://localhost:8080/weather/" + city;
            return Map.of(
                    "type", "city",
                    "query", city,
                    "weather", Utils.httpGet(url)
            );
        }

        // Check if coordinates are provided
        if (arguments.has("latitude") && arguments.has("longitude")
                && !arguments.get("latitude").isNull() && !arguments.get("longitude").isNull()) {
            double lat = arguments.get("latitude").asDouble();
            double lon = arguments.get("longitude").asDouble();
            String url = "http://localhost:8080/weather/coords?lat=" + lat + "&lon=" + lon;
            return Map.of(
                    "type", "coordinates",
                    "latitude", lat,
                    "longitude", lon,
                    "weather", Utils.httpGet(url)
            );
        }

        // Default: Random city
        String randomCity = randomCities.get(new Random().nextInt(randomCities.size()));
        String url = "http://localhost:8080/weather/" + randomCity;
        return Map.of(
                "type", "random",
                "city", randomCity,
                "weather", Utils.httpGet(url)
        );
    }
}
