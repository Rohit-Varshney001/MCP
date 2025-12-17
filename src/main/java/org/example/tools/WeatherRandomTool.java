//package org.example.tools;
//
//
//
//import java.util.*;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.example.mcp.Utils;
//
//public class WeatherRandomTool implements Tool {
//
//    List<String> cities = List.of("Delhi", "London", "Tokyo", "New York", "Mumbai");
//
//    @Override
//    public Map<String, Object> getSchema() {
//        return Map.of(
//                "name", "weather.random",
//                "description", "Fetch weather for random city",
//                "input_schema", Map.of(
//                        "type", "object",
//                        "properties", Map.of()
//                )
//        );
//    }
//
//    @Override
//    public Object run(JsonNode arguments) throws Exception {
//        String city = cities.get(new Random().nextInt(cities.size()));
//        String url = "http://localhost:8080/api/weather/city/" + city;
//        return Map.of("city", city, "weather", Utils.httpGet(url));
//    }
//}
