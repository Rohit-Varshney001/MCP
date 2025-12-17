package org.example.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.mcp.Utils;

import java.util.Map;

public class ChurnHandlers {

    public static Object handleRisk(JsonNode arguments) throws Exception {
        String customerId = arguments.get("customerId").asText();
        return Map.of(
                "type", "churn_risk",
                "customerId", customerId,
                "data", Utils.httpGet(
                        "/apps/churn/risk",
                        Map.of("customerId", customerId)
                )
        );
    }

    public static Object handleCohort(JsonNode arguments) throws Exception {
        String cohortId = arguments.get("cohortId").asText();
        return Map.of(
                "type", "churn_cohort",
                "cohortId", cohortId,
                "data", Utils.httpGet(
                        "/apps/churn/cohort/" + cohortId,
                        null
                )
        );
    }
}


