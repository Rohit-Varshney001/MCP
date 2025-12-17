//package org.example.tools;
//
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.example.mcp.Utils;
//
//import java.util.Map;
//
//public class WeatherByLatLonTool implements Tool {
//
//    @Override
//    public Map<String, Object> getSchema() {
//        return Map.of(
//                "name", "weather.getByLatLon",
//                "description", "Fetch weather by latitude & longitude",
//                // MCP spec expects camelCase key name
//                "inputSchema", Map.of(
//                        "type", "object",
//                        "properties", Map.of(
//                                "latitude", Map.of("type", "number"),
//                                "longitude", Map.of("type", "number")
//                        ),
//                        "required", new String[]{"latitude", "longitude"}
//                )
//        );
//    }
//
//    @Override
//    public Object run(JsonNode args) throws Exception {
//        double lat = args.get("latitude").asDouble();
//        double lon = args.get("longitude").asDouble();
//
//        String url = "http://localhost:8080/api/weather/coords?lat=" + lat + "&lon=" + lon;
//        return Utils.httpGet(url);
//    }
//}
