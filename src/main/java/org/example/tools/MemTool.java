package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class MemTool implements Tool {

    @Override
    public Map<String, Object> getSchema() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("order_name", Map.of(
                "type", "string",
                "description", "Order name (e.g., #21321) to hold or unhold"
        ));

        properties.put("hold", Map.of(
                "type", "boolean",
                "description", "true to hold order, false to unhold"
        ));

        return Map.of(
                "name", "MEM",
                "description", "Hold or unhold merchant orders by order name.",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", properties,
                        "required", new String[]{"order_name", "hold"}
                )
        );
    }

    @Override
    public Object run(JsonNode arguments) throws Exception {
        // For now, a single operation (hold/unhold). In future, you can route
        // to additional handlers here based on extra arguments.
        return MerchantOrdersHandlers.handleHoldStatus(arguments);
    }

   

}

