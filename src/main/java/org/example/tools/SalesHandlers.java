package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.Map;

public class SalesHandlers {

    public static Object handleSimulation(JsonNode arguments) throws Exception {
        String region = "global";
        if (arguments.has("region") && !arguments.get("region").isNull()) {
            region = arguments.get("region").asText();
        }

        String scenario = arguments.get("scenario").asText();
        return Map.of(
                "type", "sales_simulation",
                "scenario", scenario,
                "region", region,
                "data", Utils.httpGet(
                        "/apps/sales/simulate",
                        Map.of(
                                "scenario", scenario,
                                "region", region
                        )
                )
        );
    }

    public static Object handleForecast(JsonNode arguments) throws Exception {
        String region = "global";
        if (arguments.has("region") && !arguments.get("region").isNull()) {
            region = arguments.get("region").asText();
        }

        String product = "all-products";
        if (arguments.has("product") && !arguments.get("product").isNull()) {
            product = arguments.get("product").asText();
        }

        return Map.of(
                "type", "sales_forecast",
                "product", product,
                "region", region,
                "data", Utils.httpGet(
                        "/apps/sales/forecast",
                        Map.of(
                                "product", product,
                                "region", region
                        )
                )
        );
    }
}


