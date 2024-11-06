package com.example.caracceleration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class CarChartWindow {
    private final Stage stage;
    private final XYChart.Series<Number, Number> carSeries;
    private final List<Integer> carCountHistory;
    private int timeInSeconds = 0;
    private Controller controller;
    private Timeline carCoutnUpdateTimeline;

    public CarChartWindow(Controller controller) {
        stage = new Stage();
        carCountHistory = new ArrayList<>();
        this.controller = controller;

        // Оси графика
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Cars");

        // Создание графика
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Car Count Over Time");

        carSeries = new XYChart.Series<>();
        carSeries.setName("Number of Cars");
        lineChart.getData().add(carSeries);

        startChartUpdate();

        // Настройка сцены
        Scene scene = new Scene(lineChart, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Car Count Chart");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icons/graphicon.png")));
    }

    public void show() {
        stage.show();
    }

    public void addDataPoint(int carCount) {
        carCountHistory.add(carCount);
        Platform.runLater(() -> {
            carSeries.getData().add(new XYChart.Data<>(timeInSeconds++, carCount));
        });
    }

    private void startChartUpdate() {
        if (carCoutnUpdateTimeline != null) {
            carCoutnUpdateTimeline.stop();
        }

        // Таймер для обновления графика каждую секунду
        carCoutnUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int carCount = controller.getNumberOfCars();
            addDataPoint(carCount);
        }));
        carCoutnUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        carCoutnUpdateTimeline.play();
    }
}