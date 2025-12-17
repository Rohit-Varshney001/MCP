package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.Map;

public class InventoryHandlers {

    public static Object handle(JsonNode arguments) throws Exception {
        String sku = arguments.get("sku").asText();

        boolean reorder = arguments.has("reorderSuggestion")
                && !arguments.get("reorderSuggestion").isNull()
                && arguments.get("reorderSuggestion").asBoolean();

        if (reorder) {
            return Map.of(
                    "type", "inventory_reorder_suggestion",
                    "sku", sku,
                    "data", Utils.httpGet(
                            "/apps/inventory/reorder-suggestion",
                            Map.of("sku", sku)
                    )
            );
        } else {
            int lookaheadDays = 30;
            if (arguments.has("lookaheadDays") && !arguments.get("lookaheadDays").isNull()) {
                lookaheadDays = arguments.get("lookaheadDays").asInt();
            }
            return Map.of(
                    "type", "inventory_forecast",
                    "sku", sku,
                    "lookaheadDays", lookaheadDays,
                    "data", Utils.httpGet(
                            "/apps/inventory/forecast",
                            Map.of(
                                    "sku", sku,
                                    "lookaheadDays", String.valueOf(lookaheadDays)
                            )
                    )
            );
        }
    }
}


