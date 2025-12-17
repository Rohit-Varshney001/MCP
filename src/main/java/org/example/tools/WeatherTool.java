package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class WeatherTool implements Tool {

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
            return InventoryHandlers.handle(arguments);
        }

        // ===== Churn endpoints =====
        if (arguments.has("customerId") && !arguments.get("customerId").isNull()) {
            return ChurnHandlers.handleRisk(arguments);
        }

        if (arguments.has("cohortId") && !arguments.get("cohortId").isNull()) {
            return ChurnHandlers.handleCohort(arguments);
        }

        // ===== Sales endpoints =====
        if (arguments.has("product") || arguments.has("region") || arguments.has("scenario")) {
            if (arguments.has("scenario") && !arguments.get("scenario").isNull()) {
                return SalesHandlers.handleSimulation(arguments);
            } else {
                return SalesHandlers.handleForecast(arguments);
            }
        }

        // ===== Weather endpoints =====
        boolean isForecast = arguments.has("forecast")
                && !arguments.get("forecast").isNull()
                && arguments.get("forecast").asBoolean();

        if (isForecast && arguments.has("city") && !arguments.get("city").isNull()) {
            return WeatherHandlers.handleForecast(arguments);
        }

        if (arguments.has("city") && !arguments.get("city").isNull()) {
            return WeatherHandlers.handleCity(arguments);
        }

        if (arguments.has("latitude") && arguments.has("longitude")
                && !arguments.get("latitude").isNull()
                && !arguments.get("longitude").isNull()) {
            return WeatherHandlers.handleCoords(arguments);
        }

        // Default: Random city
        return WeatherHandlers.handleRandom();
    }
}
