package org.example.mcp;

import java.io.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MCPServer {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final PrintWriter writer = new PrintWriter(System.out, true);

    private static boolean isInitialized = false;
    private static ToolRegistry toolRegistry;

    public static void main(String[] args) {
        System.err.println("MCP Java Server Started");

        // Initialize tool registry
        toolRegistry = new ToolRegistry();
        System.err.println("ToolRegistry initialized with " + toolRegistry.getToolCount() + " tools");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processRequest(line);
            }
        } catch (IOException e) {
            System.err.println("Fatal I/O error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.err.println("MCP Java Server Shutting Down");
    }

    private static void processRequest(String line) {
        try {
            JsonNode request = mapper.readTree(line);

            if (!request.has("method")) {
                System.err.println("Received invalid request: missing 'method' field");
                return;
            }

            String method = request.get("method").asText();
            Object id = request.has("id") ? getIdValue(request.get("id")) : null;
            JsonNode params = request.has("params") ? request.get("params") : null;

            System.err.println("Received request - Method: " + method + ", ID: " + id);

            handleRequest(method, id, params);

        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Object getIdValue(JsonNode idNode) {
        if (idNode.isNumber()) {
            return idNode.asInt();
        } else if (idNode.isTextual()) {
            return idNode.asText();
        }
        return idNode.toString();
    }

    private static void handleRequest(String method, Object id, JsonNode params) {
        try {
            switch (method) {
                case "initialize":
                    handleInitialize(id, params);
                    break;

                case "notifications/initialized":
                    handleInitialized();
                    break;

                case "tools/list":
                    handleToolsList(id);
                    break;

                case "tools/call":
                    handleToolCall(id, params);
                    break;

                case "resources/list":
                    handleResourcesList(id);
                    break;

                case "prompts/list":
                    handlePromptsList(id);
                    break;

                case "ping":
                    handlePing(id);
                    break;

                default:
                    if (id != null) {
                        System.err.println("Unknown method: " + method);
                        sendError(id, -32601, "Method not found: " + method);
                    } else {
                        System.err.println("Received unknown notification: " + method);
                    }
            }
        } catch (Exception e) {
            System.err.println("Error handling method '" + method + "': " + e.getMessage());
            e.printStackTrace();
            if (id != null) {
                sendError(id, -32603, "Internal error: " + e.getMessage());
            }
        }
    }

    private static void handleInitialize(Object id, JsonNode params) {
        System.err.println("Handling initialize request");

        // Parse client capabilities if needed
        if (params != null && params.has("capabilities")) {
            System.err.println("Client capabilities: " + params.get("capabilities"));
        }

        // Build server capabilities
        ObjectNode capabilities = mapper.createObjectNode();
        ObjectNode toolsCapability = mapper.createObjectNode();
        toolsCapability.put("listChanged", true);
        capabilities.set("tools", toolsCapability);

        // Build server info
        ObjectNode serverInfo = mapper.createObjectNode();
        serverInfo.put("name", "my-mcp-server");
        serverInfo.put("version", "1.0.0");

        // Build result
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        result.set("capabilities", capabilities);
        result.set("serverInfo", serverInfo);

        isInitialized = true;
        sendResponse(JsonRpcMessage.success(id, result));
        System.err.println("Initialization complete with tools capability enabled");
    }

    private static void handleInitialized() {
        // This is a notification from the client - no response required
        System.err.println("Client sent initialized notification - handshake complete");
    }

    private static void handleToolsList(Object id) {
        if (!isInitialized) {
            sendError(id, -32002, "Server not initialized");
            return;
        }

        System.err.println("Handling tools/list request");

        try {
            JsonNode response = toolRegistry.getToolListResponse(mapper.valueToTree(id));
            sendResponse((ObjectNode) response);
        } catch (Exception e) {
            System.err.println("Error listing tools: " + e.getMessage());
            e.printStackTrace();
            sendError(id, -32603, "Error listing tools: " + e.getMessage());
        }
    }

    private static void handleToolCall(Object id, JsonNode params) {
        if (!isInitialized) {
            sendError(id, -32002, "Server not initialized");
            return;
        }

        System.err.println("Handling tools/call request with params: " + params);

        try {
            JsonNode response = toolRegistry.executeTool(mapper.valueToTree(id), params);
            sendResponse((ObjectNode) response);
        } catch (Exception e) {
            System.err.println("Error executing tool: " + e.getMessage());
            e.printStackTrace();
            sendError(id, -32603, "Error executing tool: " + e.getMessage());
        }
    }

    private static void handleResourcesList(Object id) {
        if (!isInitialized) {
            sendError(id, -32002, "Server not initialized");
            return;
        }

        System.err.println("Handling resources/list request");

        ArrayNode resources = mapper.createArrayNode();
        ObjectNode result = mapper.createObjectNode();
        result.set("resources", resources);

        sendResponse(JsonRpcMessage.success(id, result));
    }

    private static void handlePromptsList(Object id) {
        if (!isInitialized) {
            sendError(id, -32002, "Server not initialized");
            return;
        }

        System.err.println("Handling prompts/list request");

        ArrayNode prompts = mapper.createArrayNode();
        ObjectNode result = mapper.createObjectNode();
        result.set("prompts", prompts);

        sendResponse(JsonRpcMessage.success(id, result));
    }

    private static void handlePing(Object id) {
        System.err.println("Handling ping request");
        ObjectNode result = mapper.createObjectNode();
        sendResponse(JsonRpcMessage.success(id, result));
    }

    private static void sendResponse(ObjectNode response) {
        try {
            String json = mapper.writeValueAsString(response);
            writer.println(json);
            writer.flush();
            System.err.println("Sent response: " + json);
        } catch (Exception e) {
            System.err.println("Error sending response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendError(Object id, int code, String message) {
        try {
            ObjectNode error = mapper.createObjectNode();
            error.put("code", code);
            error.put("message", message);

            ObjectNode response = mapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.set("id", mapper.valueToTree(id));
            response.set("error", error);

            String json = mapper.writeValueAsString(response);
            writer.println(json);
            writer.flush();
            System.err.println("Sent error response: " + json);
        } catch (Exception e) {
            System.err.println("Error sending error response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
