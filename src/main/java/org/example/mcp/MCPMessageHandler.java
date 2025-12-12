package org.example.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;

public class MCPMessageHandler {

    private final ToolRegistry registry;
    private final PrintWriter writer;
    private final ObjectMapper mapper = new ObjectMapper();

    public MCPMessageHandler(ToolRegistry registry, PrintWriter writer) {
        this.registry = registry;
        this.writer = writer;
    }

    public void handle(String rawMsg) {
        try {
            JsonNode node = mapper.readTree(rawMsg);

            String method = node.get("method").asText();
            JsonNode idNode = node.get("id");

            if ("tools/list".equals(method)) {
                JsonNode response = registry.getToolListResponse(idNode);
                writer.println(response.toString());
                return;
            }

            if ("tools/call".equals(method)) {
                JsonNode params = node.get("params");
                JsonNode response = registry.executeTool(idNode, params);
                writer.println(response.toString());
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
