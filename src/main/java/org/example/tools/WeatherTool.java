package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.*;

public class WeatherTool implements Tool {

    private final List<String> randomCities = List.of("Delhi", "London", "Tokyo", "New York", "Mumbai");

    @Override
    public Map<String, Object> getSchema() {
        return Map.of(
                "name", "get_weather",
                "description", "Fetch weather information. Can get weather by city name, coordinates (latitude/longitude), or for a random city. If no parameters provided, returns random city weather.",
                // MCP spec expects camelCase key name
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "city", Map.of(
                                        "type", "string",
                                        "description", "City name to get weather for"
                                ),
                                "latitude", Map.of(
                                        "type", "number",
                                        "description", "Latitude coordinate (use with longitude)"
                                ),
                                "longitude", Map.of(
                                        "type", "number",
                                        "description", "Longitude coordinate (use with latitude)"
                                )
                        ),
                        "required", new String[]{}  // Nothing is required - all params are optional
                )
        );
    }

    @Override
    public Object run(JsonNode arguments) throws Exception {
        // Priority: 1. City, 2. Lat/Lon, 3. Random

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
