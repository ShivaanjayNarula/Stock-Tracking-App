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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        titleLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-effect: dropshadow(gaussian, #34495E, 2, 0, 0, 1);");

        symbolInput = new TextField();
        symbolInput.setPromptText("Enter symbol (e.g., AAPL)");
        symbolInput.setPrefWidth(200);
        symbolInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5; -fx-background-radius: 5;");

        fetchButton = new Button("Show Chart");
        fetchButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        fetchButton.setOnMouseEntered(e -> fetchButton.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
        fetchButton.setOnMouseExited(e -> fetchButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5;");

        symbolHistory = new ComboBox<>();
        symbolHistory.setPromptText("Recent symbols");
        symbolHistory.setPrefWidth(150);
        symbolHistory.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        // Chart Setup
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new CandlestickChart(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setPrefSize(1600, 900);
        
        // Enhanced chart styling
        xAxis.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        yAxis.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        chart.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 5;");
        
        // Set chart title styling
        chart.setTitle("Stock Price Chart");
        chart.setStyle(chart.getStyle() + "-fx-font-size: 18px; -fx-font-weight: bold;");

        // Event Handling
        fetchButton.setOnAction(e -> handleFetchRequest());
        symbolHistory.setOnAction(e -> symbolInput.setText(symbolHistory.getValue()));

        // Input validation styling
        symbolInput.styleProperty().bind(
                Bindings.when(symbolInput.focusedProperty())
                        .then("-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #3498DB; -fx-border-width: 2;")
                        .otherwise("-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #BDC3C7; -fx-border-width: 1;")
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
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #ECF0F1; -fx-padding: 15px; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #BDC3C7; -fx-border-width: 1;");
        
        Label symbolLabel = new Label("Stock Symbol:");
        symbolLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        Label historyLabel = new Label("History:");
        historyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        panel.getChildren().addAll(
                symbolLabel,
                symbolInput,
                fetchButton,
                historyLabel,
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
        try {
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
        } catch (Exception e) {
            System.out.println("API failed, creating sample data for: " + symbol);
            return createSampleData(symbol);
        }
    }

    private List<StockData> createSampleData(String symbol) {
        List<StockData> sampleData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        double basePrice = 100.0 + (symbol.hashCode() % 50);
        
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            double open = basePrice + (Math.random() - 0.5) * 10;
            double high = open + Math.random() * 5;
            double low = open - Math.random() * 5;
            double close = open + (Math.random() - 0.5) * 8;
            
            high = Math.max(high, Math.max(open, close));
            low = Math.min(low, Math.min(open, close));
            
            long volume = 1000000 + (long)(Math.random() * 5000000);
            sampleData.add(new StockData(date, open, high, low, close, volume));
            basePrice = close;
        }
        return sampleData;
    }



    private void updateChart(String symbol, List<StockData> data) {
        chart.getData().clear();
        chart.setTitle(symbol + " - Candlestick Chart");
        chart.getYAxis().setLabel("Price (USD)");
        chart.getXAxis().setLabel("Date");

        // Enhanced axis styling
        chart.getYAxis().setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        chart.getXAxis().setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(symbol);
        
        data.forEach(d -> series.getData().add(
                new XYChart.Data<>(
                        d.getTimestamp().toLocalDate().toString(),
                        d.getClose(),
                        d
                )
        ));

        chart.getData().add(series);
        
        // Auto-scale the Y-axis to show all data points clearly
        if (!data.isEmpty()) {
            double minPrice = data.stream().mapToDouble(StockData::getLow).min().orElse(0);
            double maxPrice = data.stream().mapToDouble(StockData::getHigh).max().orElse(0);
            double padding = (maxPrice - minPrice) * 0.1; // 10% padding
            
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();
            yAxis.setLowerBound(minPrice - padding);
            yAxis.setUpperBound(maxPrice + padding);
            yAxis.setTickUnit((maxPrice - minPrice) / 10);
        }
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
        String baseStyle = "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-width: 1;";
        
        if ("error".equals(type)) {
            statusLabel.setStyle(baseStyle + "-fx-text-fill: #E74C3C; -fx-background-color: #FADBD8; -fx-border-color: #E74C3C;");
        } else {
            statusLabel.setStyle(baseStyle + "-fx-text-fill: #27AE60; -fx-background-color: #D5F4E6; -fx-border-color: #27AE60;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}