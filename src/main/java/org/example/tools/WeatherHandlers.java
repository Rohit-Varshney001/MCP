package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeatherHandlers {

    private static final List<String> randomCities = List.of("Delhi", "London", "Tokyo", "New York", "Mumbai");

    public static Object handleForecast(JsonNode arguments) throws Exception {
        String city = arguments.get("city").asText();
        return Map.of(
                "type", "forecast",
                "city", city,
                "forecast", Utils.httpGet(
                        "/weather/" + city + "/forecast",
                        null
                )
        );
    }

    public static Object handleCity(JsonNode arguments) throws Exception {
        String city = arguments.get("city").asText();
        return Map.of(
                "type", "city",
                "query", city,
                "weather", Utils.httpGet(
                        "/weather/" + city,
                        null
                )
        );
    }

    public static Object handleCoords(JsonNode arguments) throws Exception {
        double lat = arguments.get("latitude").asDouble();
        double lon = arguments.get("longitude").asDouble();
        return Map.of(
                "type", "coordinates",
                "latitude", lat,
                "longitude", lon,
                "weather", Utils.httpGet(
                        "/weather/coords",
                        Map.of(
                                "lat", String.valueOf(lat),
                                "lon", String.valueOf(lon)
                        )
                )
        );
    }

    public static Object handleRandom() throws Exception {
        String randomCity = randomCities.get(new Random().nextInt(randomCities.size()));
        return Map.of(
                "type", "random",
                "city", randomCity,
                "weather", Utils.httpGet(
                        "/weather/" + randomCity,
                        null
                )
        );
    }
}


