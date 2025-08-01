package com.stocktracker.ui.charts;

import com.stocktracker.models.StockData;
import javafx.collections.FXCollections;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class CandlestickChart extends XYChart<String, Number> {

    private static final double CANDLE_WIDTH = 20;
    private static final double WICK_WIDTH = 2;
    private static final double SHADOW_RADIUS = 3;
    private static final double SHADOW_SPREAD = 1;
    
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    // Color schemes
    private static final Color BULLISH_FILL = Color.rgb(76, 175, 80, 0.9); // Green
    private static final Color BULLISH_STROKE = Color.rgb(56, 142, 60);
    private static final Color BEARISH_FILL = Color.rgb(244, 67, 54, 0.9); // Red
    private static final Color BEARISH_STROKE = Color.rgb(211, 47, 47);
    private static final Color WICK_COLOR = Color.rgb(33, 33, 33);
    private static final Color SHADOW_COLOR = Color.rgb(0, 0, 0, 0.3);

    public CandlestickChart(Axis<String> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        setAnimated(false);
        setData(FXCollections.observableArrayList());
        
        // Set chart styling
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
    }

    @Override
    protected void layoutPlotChildren() {
        if (getData().isEmpty()) return;

        for (Series<String, Number> series : getData()) {
            for (Data<String, Number> item : series.getData()) {
                Group nodeGroup = (Group) item.getNode();
                if (nodeGroup == null) {
                    nodeGroup = new Group();
                    item.setNode(nodeGroup);
                    getPlotChildren().add(nodeGroup);
                }

                StockData stock = (StockData) item.getExtraValue();
                if (stock == null) continue;

                drawCandlestick(item, nodeGroup, stock);
            }
        }
    }

    private void drawCandlestick(Data<String, Number> item, Group nodeGroup, StockData stock) {
        double x = getXAxis().getDisplayPosition(item.getXValue());
        double yOpen = getYAxis().getDisplayPosition(stock.getOpen());
        double yClose = getYAxis().getDisplayPosition(stock.getClose());
        double yHigh = getYAxis().getDisplayPosition(stock.getHigh());
        double yLow = getYAxis().getDisplayPosition(stock.getLow());

        // Determine if bullish or bearish
        boolean isBullish = stock.getClose() > stock.getOpen();
        Color fillColor = isBullish ? BULLISH_FILL : BEARISH_FILL;
        Color strokeColor = isBullish ? BULLISH_STROKE : BEARISH_STROKE;

        // Calculate body dimensions
        double bodyTop = Math.min(yOpen, yClose);
        double bodyHeight = Math.max(Math.abs(yOpen - yClose), 1); // Minimum height of 1

        // Create or update body rectangle
        Rectangle body = getOrCreateRectangle(nodeGroup, 0);
        updateRectangle(body, x - CANDLE_WIDTH/2, bodyTop, CANDLE_WIDTH, bodyHeight, fillColor, strokeColor);

        // Create or update wick
        Line wick = getOrCreateLine(nodeGroup, 1);
        updateLine(wick, x, yHigh, x, yLow, WICK_COLOR, WICK_WIDTH);

        // Add shadow effect
        DropShadow shadow = new DropShadow(SHADOW_RADIUS, SHADOW_SPREAD, SHADOW_SPREAD, SHADOW_COLOR);
        body.setEffect(shadow);

        // Add hover effects
        setupHoverEffects(body, wick, stock, x, yHigh, yLow);

        // Add tooltip
        setupTooltip(nodeGroup, stock);
    }

    private Rectangle getOrCreateRectangle(Group nodeGroup, int index) {
        Rectangle rect = null;
        if (nodeGroup.getChildren().size() > index) {
            rect = (Rectangle) nodeGroup.getChildren().get(index);
        }
        if (rect == null) {
            rect = new Rectangle();
            nodeGroup.getChildren().add(rect);
        }
        return rect;
    }

    private Line getOrCreateLine(Group nodeGroup, int index) {
        Line line = null;
        if (nodeGroup.getChildren().size() > index) {
            line = (Line) nodeGroup.getChildren().get(index);
        }
        if (line == null) {
            line = new Line();
            nodeGroup.getChildren().add(line);
        }
        return line;
    }

    private void updateRectangle(Rectangle rect, double x, double y, double width, double height, Color fill, Color stroke) {
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setFill(fill);
        rect.setStroke(stroke);
        rect.setStrokeWidth(1.5);
    }

    private void updateLine(Line line, double startX, double startY, double endX, double endY, Color color, double width) {
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        line.setStroke(color);
        line.setStrokeWidth(width);
    }

    private void setupHoverEffects(Rectangle body, Line wick, StockData stock, double x, double yHigh, double yLow) {
        // Hover effects for body
        body.setOnMouseEntered(e -> {
            body.setEffect(new DropShadow(8, 2, 2, Color.rgb(0, 0, 0, 0.4)));
            body.setScaleX(1.1);
            body.setScaleY(1.1);
            wick.setStrokeWidth(WICK_WIDTH * 1.5);
        });

        body.setOnMouseExited(e -> {
            body.setEffect(new DropShadow(SHADOW_RADIUS, SHADOW_SPREAD, SHADOW_SPREAD, SHADOW_COLOR));
            body.setScaleX(1.0);
            body.setScaleY(1.0);
            wick.setStrokeWidth(WICK_WIDTH);
        });

        // Hover effects for wick
        wick.setOnMouseEntered(e -> {
            wick.setStrokeWidth(WICK_WIDTH * 2);
            wick.setEffect(new Glow(0.3));
        });

        wick.setOnMouseExited(e -> {
            wick.setStrokeWidth(WICK_WIDTH);
            wick.setEffect(null);
        });
    }

    private void setupTooltip(Group nodeGroup, StockData stock) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-font-size: 12px;");
        
        String tooltipText = String.format(
            "Date: %s\n" +
            "Open: $%s\n" +
            "High: $%s\n" +
            "Low: $%s\n" +
            "Close: $%s\n" +
            "Volume: %,d\n" +
            "Change: %s%.2f%%",
            stock.getTimestamp().format(dateFormat),
            priceFormat.format(stock.getOpen()),
            priceFormat.format(stock.getHigh()),
            priceFormat.format(stock.getLow()),
            priceFormat.format(stock.getClose()),
            stock.getVolume(),
            stock.getClose() > stock.getOpen() ? "+" : "",
            ((stock.getClose() - stock.getOpen()) / stock.getOpen()) * 100
        );
        
        tooltip.setText(tooltipText);
        Tooltip.install(nodeGroup, tooltip);
    }

    @Override
    protected void dataItemAdded(Series<String, Number> series, int itemIndex, Data<String, Number> item) {
        Group nodeGroup = new Group();
        // Initialize with empty shapes
        nodeGroup.getChildren().addAll(new Rectangle(), new Line());
        item.setNode(nodeGroup);
        getPlotChildren().add(nodeGroup);
    }

    @Override
    protected void dataItemRemoved(Data<String, Number> item, Series<String, Number> series) {
        getPlotChildren().remove(item.getNode());
        item.setNode(null);
    }

    @Override
    protected void dataItemChanged(Data<String, Number> item) {
        // Handle data changes if needed
    }

    @Override
    protected void seriesAdded(Series<String, Number> series, int seriesIndex) {
        // Handle series addition
        for (Data<String, Number> item : series.getData()) {
            item.setNode(new Group());
            getPlotChildren().add(item.getNode());
        }
    }

    @Override
    protected void seriesRemoved(Series<String, Number> series) {
        // Handle series removal
        for (Data<String, Number> item : series.getData()) {
            getPlotChildren().remove(item.getNode());
            item.setNode(null);
        }
    }
}