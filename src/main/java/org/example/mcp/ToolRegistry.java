package org.example.mcp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tools.Tool;
import org.example.tools.WeatherByCityTool;
import org.example.tools.WeatherByLatLonTool;
import org.example.tools.WeatherRandomTool;

import java.util.*;

public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ToolRegistry() {
        tools.put("weather.getByCity", new WeatherByCityTool());
        tools.put("weather.getByLatLon", new WeatherByLatLonTool());
        tools.put("weather.random", new WeatherRandomTool());
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

            return JsonRpcMessage.success(id, Map.of("output", result));

        } catch (Exception e) {
            return JsonRpcMessage.error(id, -32603, "Tool execution error: " + e.getMessage());
        }
    }
}

