package org.example.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonRpcMessage {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectNode success(Object id, Object result) {
        ObjectNode msg = mapper.createObjectNode();
        msg.put("jsonrpc", "2.0");
        msg.set("id", mapper.valueToTree(id));
        msg.set("result", mapper.valueToTree(result));
        return msg;
    }

    public static ObjectNode error(Object id, int code, String message) {
        ObjectNode errorObj = mapper.createObjectNode();
        errorObj.put("code", code);
        errorObj.put("message", message);

        ObjectNode msg = mapper.createObjectNode();
        msg.put("jsonrpc", "2.0");
        msg.set("id", mapper.valueToTree(id));
        msg.set("error", errorObj);
        return msg;
    }

    public static ObjectNode notification(String method, Object params) {
        ObjectNode msg = mapper.createObjectNode();
        msg.put("jsonrpc", "2.0");
        msg.put("method", method);
        if (params != null) {
            msg.set("params", mapper.valueToTree(params));
        }
        return msg;
    }
}
