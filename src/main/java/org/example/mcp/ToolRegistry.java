package org.example.mcp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tools.Tool;
// Old approach - individual tools (commented out, kept for reference)
// import org.example.tools.WeatherByCityTool;
// import org.example.tools.WeatherByLatLonTool;
// import org.example.tools.WeatherRandomTool;

// New approach - unified weather tool
import org.example.tools.WeatherTool;

import java.util.*;

public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ToolRegistry() {
        // Old approach - three separate tools (commented out, kept for reference)
        // tools.put("weather.getByCity", new WeatherByCityTool());
        // tools.put("weather.getByLatLon", new WeatherByLatLonTool());
        // tools.put("weather.random", new WeatherRandomTool());

        // New approach - single unified weather tool
        tools.put("get_weather", new WeatherTool());
    }

    public int getToolCount() {
        return tools.size();
    }

    public JsonNode getToolListResponse(JsonNode idNode) {
        List<Object> list = new ArrayList<>();

        for (Tool t : tools.values()) {
            list.add(t.getSchema());
        }

        return JsonRpcMessage.success(idNode, Map.of("tools", list));
    }

    public JsonNode executeTool(JsonNode id, JsonNode params) {
        try {
            String toolName = params.get("name").asText();
            Tool tool = tools.get(toolName);

            if (tool == null) {
                return JsonRpcMessage.error(id, -32602, "Unknown tool: " + toolName);
            }

            JsonNode arguments = params.get("arguments");
            Object result = tool.run(arguments);

            // Return proper MCP content format
            return JsonRpcMessage.success(id, Map.of(
                    "content", List.of(
                            Map.of(
                                    "type", "text",
                                    "text", mapper.writeValueAsString(result)
                            )
                    )
            ));

        } catch (Exception e) {
            return JsonRpcMessage.error(id, -32603, "Tool execution error: " + e.getMessage());
        }
    }
}

