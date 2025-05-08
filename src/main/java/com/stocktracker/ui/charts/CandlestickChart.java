package com.stocktracker.ui.charts;

import com.stocktracker.models.StockData;
import javafx.collections.FXCollections;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;

public class CandlestickChart extends XYChart<String, Number> {

    public CandlestickChart(Axis<String> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        setAnimated(false);
        setData(FXCollections.observableArrayList());
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

        // Calculate candle dimensions
        double candleWidth = 18;
        double bodyTop = Math.min(yOpen, yClose);
        double bodyHeight = Math.abs(yOpen - yClose);

        // Get or create body rectangle
        Rectangle body = null;
        if (!nodeGroup.getChildren().isEmpty()) {
            body = (Rectangle) nodeGroup.getChildren().get(0);
        }
        if (body == null) {
            body = new Rectangle();
            nodeGroup.getChildren().add(body);
        }

        // Update body properties
        body.setWidth(candleWidth);
        body.setHeight(bodyHeight);
        body.setX(x - candleWidth/2);
        body.setY(bodyTop);
        body.setStroke(Color.BLACK);
        body.setFill(stock.getClose() > stock.getOpen() ? Color.GREEN : Color.RED);

        // Get or create wick line
        Line wick = null;
        if (nodeGroup.getChildren().size() > 1) {
            wick = (Line) nodeGroup.getChildren().get(1);
        }
        if (wick == null) {
            wick = new Line();
            nodeGroup.getChildren().add(wick);
        }

        // Update wick properties
        wick.setStartX(x);
        wick.setStartY(yHigh);
        wick.setEndX(x);
        wick.setEndY(yLow);
        wick.setStroke(((Color) body.getFill()).darker());
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