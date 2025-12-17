//package org.example.tools;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.example.mcp.Utils;
//
//import java.util.Map;
//
//public class WeatherByCityTool implements Tool {
//
//    @Override
//    public Map<String, Object> getSchema() {
//        return Map.of(
//                "name", "weather.getByCity",
//                "description", "Fetch weather by city name",
//                // MCP spec expects camelCase key name
//                "inputSchema", Map.of(
//                        "type", "object",
//                        "properties", Map.of(
//                                "city", Map.of("type", "string")
//                        ),
//                        "required", new String[]{"city"}
//                )
//        );
//    }
//
//    @Override
//    public Object run(JsonNode arguments) throws Exception {
//        String city = arguments.get("city").asText();
//        String url = "http://localhost:8080/api/weather/city/" + city;
//        return Utils.httpGet(url);
//    }
//}
//
