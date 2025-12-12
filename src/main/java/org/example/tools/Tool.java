package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface Tool {

    Map<String, Object> getSchema();

    Object run(JsonNode arguments) throws Exception;
}

