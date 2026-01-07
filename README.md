## my-mcp-server – Weather & Business Forecast MCP (Java)

This project is a Java MCP server that exposes a **single tool** `get_weather` which can call multiple backend endpoints:

- Weather: current weather, 5‑day forecast, weather by coordinates, random city
- Inventory: forecast and reorder suggestion
- Churn: risk and cohort analysis
- Sales: forecast and scenario simulation

It is designed to plug directly into **Claude / MCP clients** using a single tool with multiple parameters.

---

### 1. Prerequisites

- **Java**: 21+
- **Maven**: 3.9+ (for building)
- **Backend API** running with endpoints (examples):
  - `GET /weather/{city}`
  - `GET /weather/{city}/forecast`
  - `GET /weather/coords?lat=&lon=`
  - `GET /apps/inventory/forecast?sku=&lookaheadDays=`
  - `GET /apps/inventory/reorder-suggestion?sku=`
  - `GET /apps/churn/risk?customerId=`
  - `GET /apps/churn/cohort/{cohortId}`
  - `GET /apps/sales/forecast?product=&region=`
  - `GET /apps/sales/simulate?scenario=&region=`

By default the MCP server calls `http://localhost:8080`. You can change this via an env var (see below).

---

### 2. Build the MCP server JAR

From the `MCP_Server` directory:

```bash
mvn clean package
```

This produces a shaded JAR:

- `target/MCP_Server-1.0-SNAPSHOT.jar`

The JAR has `org.example.mcp.MCPServer` as its main class and is ready to be used as an MCP server.

---

### 3. Configure base URL for your backend (production vs local)

The class `org.example.mcp.Utils` defines a configurable base URL:

- Env var: `API_BASE_URL`
- Default: `http://localhost:8080`

Behavior:

- If `API_BASE_URL` is set (e.g. `https://api.yourdomain.com`), **all** calls go to that base.
- If not set, everything hits `http://localhost:8080` (great for local dev).

Example (Linux/macOS):

```bash
export API_BASE_URL="https://api.yourdomain.com"
java -jar target/MCP_Server-1.0-SNAPSHOT.jar
```

Example (Windows PowerShell):

```powershell
$env:API_BASE_URL = "https://api.yourdomain.com"
java -jar target\MCP_Server-1.0-SNAPSHOT.jar
```

---

### 4. Integrating with Claude (MCP)

Claude Desktop (and other MCP‑aware clients) can be pointed at this JAR via an MCP config.

#### 4.1. Example Claude MCP config entry

In your client’s MCP config (for example `claude_desktop_config.json` or `.cursor/mcp.json`), add:

```json
{
  "mcpServers": {
    "my-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/MCP_Server/target/MCP_Server-1.0-SNAPSHOT.jar"
      ],
      "env": {
        "API_BASE_URL": "http://localhost:8080"
      },
      "cwd": "/absolute/path/to/MCP_Server",
      "disabled": false,
      "alwaysAllow": []
    }
  }
}
```

Adjust the paths for your environment and OS (Windows will use backslashes).

After saving the config:

1. Restart Claude / the MCP client.
2. You should see a server named **`my-mcp-server`** become available.
3. It will expose a single tool: **`get_weather`**.

---

### 5. Using the `get_weather` tool from Claude

The tool accepts a JSON object of arguments. Example use cases:

- **Current weather for a city**
  - Arguments:
    ```json
    { "city": "Delhi" }
    ```
- **5‑day weather forecast**
  - Arguments:
    ```json
    { "city": "Delhi", "forecast": true }
    ```
- **Weather by coordinates**
  - Arguments:
    ```json
    { "latitude": 28.6139, "longitude": 77.2090 }
    ```
- **Random city weather**
  - Arguments:
    ```json
    {}
    ```

- **Inventory forecast**
  - Arguments:
    ```json
    { "sku": "SKU-123", "lookaheadDays": 45 }
    ```

- **Inventory reorder suggestion**
  - Arguments:
    ```json
    { "sku": "SKU-123", "reorderSuggestion": true }
    ```

- **Churn risk**
  - Arguments:
    ```json
    { "customerId": "cust-001" }
    ```

- **Churn cohort analysis**
  - Arguments:
    ```json
    { "cohortId": "enterprise-2025" }
    ```

- **Sales forecast**
  - Arguments:
    ```json
    { "product": "laptop-pro", "region": "apac" }
    ```

- **Sales simulation**
  - Arguments:
    ```json
    { "scenario": "promo-heavy", "region": "emea" }
    ```

In Claude, 
You don’t need to write JSON directly; just give Claude a natural‑language request and it will call `get_weather` with the right arguments.

#### Weather

- **Current weather by city**  
  Prompt: `get the current weather in Delhi`

- **5‑day weather forecast**  
  Prompt: `get the weather forecast of Delhi for next 3 days`

- **Weather by coordinates**  
  Prompt: `get weather for these coordinates latitude=28.6139 and longitude=77.2090`

- **Random city weather**  
  Prompt: `get random city weather`

#### Inventory

- **Inventory forecast**  
  Prompt: `get inventory forecast : sku="SKU-123" and lookaheadDays=45`

- **Inventory reorder suggestion**  
  Prompt: `get a reorder suggestion for sku="SKU-123"`

#### Churn

- **Churn risk by customer**  
  Prompt: `get churn risk customerId="cust-001"`

- **Churn cohort analysis**  
  Prompt: `get churn cohort analysis for cohortId="enterprise-2025"`

#### Sales

- **Sales forecast**  
  Prompt: `get a sales forecast for product="laptop-pro" and region="apac"`

- **Sales simulation**  
  Prompt: `get sales scenario for scenario="promo-heavy" and region="emea"`

Claude will choose the tool and arguments based on the schema.

---

### 6. Code structure and where to change things

The project is structured so you have **one tool** but **separate handler classes**:

- Tool registration:
  - `org.example.mcp.ToolRegistry`
    - Registers a single tool: `get_weather` → `org.example.tools.WeatherTool`

- Tool entry point:
  - `org.example.tools.WeatherTool`
    - Defines the **schema** and **input parameters** for `get_weather`
    - Routes calls to domain‑specific handlers based on which arguments are present

- Domain handlers:
  - `org.example.tools.InventoryHandlers`
    - Inventory forecast and reorder suggestion
  - `org.example.tools.ChurnHandlers`
    - Churn risk and cohort analysis
  - `org.example.tools.SalesHandlers`
    - Sales forecast and scenario simulation
  - `org.example.tools.WeatherHandlers`
    - Weather forecast, city, coordinates, random

- HTTP & config:
  - `org.example.mcp.Utils`
    - Handles `BASE_URL`, timeouts, URL building, and GET requests

---

### 7. How to add a new backend endpoint (while keeping one tool)

If you want to add a new endpoint (for example, a **marketing** forecast) but still keep **only `get_weather`**:

1. **Choose or create a handler class**
   - If it’s a new business domain, create a new handler (e.g. `MarketingHandlers` in `org.example.tools`).
   - If it fits an existing domain (e.g. sales/marketing), add a method to `SalesHandlers`.

2. **Add parameters to the tool schema**
   - Open `org.example.tools.WeatherTool` → `getSchema()`.
   - Add a new entry to the `properties` map, e.g.:
     ```java
     properties.put("campaignId", Map.of(
             "type", "string",
             "description", "Campaign ID for marketing forecast"
     ));
     ```

3. **Update the routing logic in `WeatherTool.run`**
   - Open `WeatherTool.run(JsonNode arguments)`.
   - Add a new condition that detects your new argument(s) and delegates to your handler, for example:
     ```java
     if (arguments.has("campaignId") && !arguments.get("campaignId").isNull()) {
         return MarketingHandlers.handleCampaignForecast(arguments);
     }
     ```

4. **Implement the handler method**
   - In your handler (existing or new), implement the call to your backend endpoint using `Utils.httpGet`:
     ```java
     public static Object handleCampaignForecast(JsonNode arguments) throws Exception {
         String campaignId = arguments.get("campaignId").asText();
         return Map.of(
                 "type", "marketing_campaign_forecast",
                 "campaignId", campaignId,
                 "data", Utils.httpGet(
                         "/apps/marketing/forecast",
                         Map.of("campaignId", campaignId)
                 )
         );
     }
     ```

5. **Rebuild the JAR**
   - Run:
     ```bash
     mvn clean package
     ```
   - Restart the MCP client so it picks up the new JAR.

You **do not** need to change `ToolRegistry` as long as the tool name remains `get_weather`.

---

### 8. Error handling & production notes

- Network and HTTP errors are surfaced as exceptions from `Utils.httpGet`, which are translated into MCP errors.
- You can change timeouts or logging by editing `Utils` (e.g., integrate a real logging backend via SLF4J).
- For production:
  - Always set `API_BASE_URL` explicitly.
  - Run the MCP server close to your backend for low latency.
  - Secure your backend with appropriate auth; this MCP server assumes it can call your API directly.

With these pieces, a new user should be able to:

1. Run `mvn package`
2. Point Claude (or any MCP client) at the JAR
3. Start using the single `get_weather` tool for all supported endpoints
4. Extend the tool by adding new parameters + handler methods without touching the MCP protocol logic.


