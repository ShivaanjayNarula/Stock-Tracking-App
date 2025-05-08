package com.stocktracker.ui;

import com.stocktracker.api.ApiClient;
import com.stocktracker.database.DatabaseManager;
import com.stocktracker.database.daos.StockDAO;
import com.stocktracker.models.StockData;
import com.stocktracker.ui.charts.CandlestickChart;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MainApp extends Application {
    private CandlestickChart chart;
    private Label statusLabel;
    private TextField symbolInput;
    private ComboBox<String> symbolHistory;
    private Button fetchButton;

    @Override
    public void start(Stage stage) {
        DatabaseManager.initialize();

        // UI Components
        Label titleLabel = new Label("Stock Tracker");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        symbolInput = new TextField();
        symbolInput.setPromptText("Enter symbol (e.g., AAPL)");
        symbolInput.setPrefWidth(200);

        fetchButton = new Button("Show Chart");
        fetchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        symbolHistory = new ComboBox<>();
        symbolHistory.setPromptText("Recent symbols");
        symbolHistory.setPrefWidth(150);

        // Chart Setup
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new CandlestickChart(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setPrefSize(1600, 900);

        // Event Handling
        fetchButton.setOnAction(e -> handleFetchRequest());
        symbolHistory.setOnAction(e -> symbolInput.setText(symbolHistory.getValue()));

        // Input validation styling
        symbolInput.styleProperty().bind(
                Bindings.when(symbolInput.focusedProperty())
                        .then("-fx-border-color: #4CAF50;")
                        .otherwise("-fx-border-color: #BDBDBD;")
        );

        // Layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(
                titleLabel,
                createInputPanel(),
                chart,
                statusLabel
        );

        Scene scene = new Scene(root, 1920, 1080);
        stage.setTitle("Stock Data Visualizer");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createInputPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.getChildren().addAll(
                new Label("Stock Symbol:"),
                symbolInput,
                fetchButton,
                new Label("History:"),
                symbolHistory
        );
        return panel;
    }

    private void handleFetchRequest() {
        String symbol = symbolInput.getText().trim().toUpperCase();

        if (symbol.isEmpty()) {
            showStatus("Please enter a stock symbol", "error");
            symbolInput.requestFocus();
            return;
        }

        if (!isValidSymbol(symbol)) {
            showStatus("Invalid symbol format. Use 1-5 uppercase letters", "error");
            symbolInput.selectAll();
            return;
        }

        try {
            if (ApiClient.isRateLimited()) {
                showStatus("API rate limit exceeded. Please wait 1 minute", "error");
                return;
            }

            List<StockData> stockData = fetchAndProcessData(symbol);
            updateChart(symbol, stockData);
            updateHistory(symbol);
            showStatus(String.format("Loaded %d data points for %s",
                    stockData.size(), symbol), "success");

        } catch (IOException ex) {
            showStatus("Network error: " + ex.getMessage(), "error");
        } catch (SQLException ex) {
            showStatus("Database error: " + ex.getMessage(), "error");
        } catch (RuntimeException ex) {
            handleApiError(ex, symbol);
        }
    }

    private List<StockData> fetchAndProcessData(String symbol)
            throws IOException, SQLException {
        String jsonResponse = ApiClient.getDailySeries(symbol);
        List<StockData> stockData = ApiClient.parseDailySeries(jsonResponse);

        for (StockData data : stockData) {
            StockDAO.insertStockData(symbol,
                    data.getOpen(), data.getHigh(),
                    data.getLow(), data.getClose(),
                    data.getVolume()
            );
        }
        return stockData;
    }

    private void updateChart(String symbol, List<StockData> data) {
        chart.getData().clear();
        chart.setTitle(symbol + " Candlestick Chart");
        chart.getYAxis().setLabel("Price (USD)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach(d -> series.getData().add(
                new XYChart.Data<>(
                        d.getTimestamp().toLocalDate().toString(),
                        d.getClose(),
                        d
                )
        ));

        chart.getData().add(series);
    }

    private boolean isValidSymbol(String symbol) {
        return symbol.matches("^[A-Z]{1,5}$");
    }

    private void updateHistory(String symbol) {
        ObservableList<String> history = symbolHistory.getItems();
        if (!history.contains(symbol)) {
            history.add(0, symbol);
            if (history.size() > 5) {
                history.remove(5, history.size());
            }
        }
        symbolHistory.getSelectionModel().select(symbol);
    }

    private void handleApiError(RuntimeException ex, String symbol) {
        if (ex.getMessage().contains("Rate Limit")) {
            ApiClient.startRateLimitTimer();
            showStatus("API Limit: " + ex.getMessage(), "error");
        } else if (ex.getMessage().contains("No time series")) {
            showStatus("No data available for " + symbol, "error");
            symbolHistory.getItems().remove(symbol);
        } else {
            showStatus("API Error: " + ex.getMessage(), "error");
        }
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " +
                ("error".equals(type) ? "#D32F2F" : "#388E3C") + ";");
    }

    public static void main(String[] args) {
        launch(args);
    }
}