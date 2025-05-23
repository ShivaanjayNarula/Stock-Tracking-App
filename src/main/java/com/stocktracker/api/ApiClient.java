
package com.stocktracker.api;

import com.stocktracker.models.StockData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ApiClient {
    private static final String API_KEY = "KV38II1661IAMO23";
    private static final OkHttpClient client = new OkHttpClient();
    private static final int MAX_CALLS_PER_MINUTE = 5;
    private static final LinkedList<Long> callTimestamps = new LinkedList<>();

    // Historical data endpoint
    public static String getDailySeries(String symbol) throws IOException {
        checkRateLimit();
        String url = String.format(
                "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=compact&apikey=%s",
                symbol, API_KEY
        );

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            validateResponse(response);
            return response.body().string();
        }
    }

    // Real-time quote endpoint
    public static StockData getGlobalQuote(String symbol) throws IOException {
        checkRateLimit();
        String url = String.format(
                "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                symbol, API_KEY
        );

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            validateResponse(response);
            JSONObject json = new JSONObject(response.body().string());

            if (!json.has("Global Quote")) {
                throw new IOException("Invalid API response format");
            }

            JSONObject quote = json.getJSONObject("Global Quote");
            return parseQuote(quote);
        }
    }

    // Parsing logic for daily series
    public static List<StockData> parseDailySeries(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        checkForApiErrors(json);

        JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
        return timeSeries.keySet().stream()
                .sorted((d1, d2) -> d2.compareTo(d1)) // Most recent first
                .limit(30)
                .map(date -> createStockData(date, timeSeries.getJSONObject(date)))
                .collect(Collectors.toList());
    }

    // Rate limiting control methods
    public static synchronized boolean isRateLimited() {
        purgeOldTimestamps();
        return callTimestamps.size() >= MAX_CALLS_PER_MINUTE;
    }

    public static synchronized void startRateLimitTimer() {
        callTimestamps.add(System.currentTimeMillis());
    }

    private static void checkRateLimit() throws IOException {
        if (isRateLimited()) {
            throw new IOException("API rate limit exceeded (5 calls/minute). Please wait.");
        }
        startRateLimitTimer();
    }

    private static void purgeOldTimestamps() {
        long cutoff = System.currentTimeMillis() - 60_000;
        callTimestamps.removeIf(ts -> ts < cutoff);
    }

    private static void validateResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }
    }

    private static void checkForApiErrors(JSONObject json) {
        if (json.has("Note")) {
            throw new RuntimeException("API Rate Limit: " + json.getString("Note"));
        }
        if (json.has("Error Message")) {
            throw new RuntimeException("API Error: " + json.getString("Error Message"));
        }
    }

    private static StockData createStockData(String date, JSONObject entry) {
        return new StockData(
                LocalDateTime.parse(date + "T00:00:00"),
                entry.getDouble("1. open"),
                entry.getDouble("2. high"),
                entry.getDouble("3. low"),
                entry.getDouble("4. close"),
                entry.getLong("5. volume")
        );
    }

    private static StockData parseQuote(JSONObject quote) {
        String[] requiredFields = {
                "02. open", "03. high", "04. low",
                "05. price", "06. volume"
        };

        for (String field : requiredFields) {
            if (!quote.has(field)) {
                throw new RuntimeException("Missing required field: " + field);
            }
        }

        return new StockData(
                LocalDateTime.now(),
                quote.getDouble("02. open"),
                quote.getDouble("03. high"),
                quote.getDouble("04. low"),
                quote.getDouble("05. price"),
                quote.getLong("06. volume")
        );
    }
}