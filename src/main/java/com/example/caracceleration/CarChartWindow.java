package com.example.caracceleration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class CarChartWindow implements IObserver {
    private final Stage stage;
    private final XYChart.Series<Number, Number> carSeries;
    private final List<Integer> carCountHistory;
    private int timeInSeconds;
    private Timeline carCountUpdateTimeline;
    private int numberOfCars;

    private short subStatus;

    public CarChartWindow(int timeInSeconds) {
        subStatus = 1;
        stage = new Stage();
        this.timeInSeconds = timeInSeconds;
        carCountHistory = new ArrayList<>();

        stage.setOnCloseRequest(event -> {
            subStatus = 2;
        });

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

        // Кнопка "Подписаться/Отписаться"
        Button subscribeButton = new Button("Отписаться");
        subscribeButton.setOnAction(event -> {
            if (subStatus == 1) {
                subStatus = 0;
                subscribeButton.setText("Подписаться");
            } else {
                subStatus = 1;
                subscribeButton.setText("Отписаться");
            }
        });

        // Макет окна
        BorderPane root = new BorderPane();
        root.setCenter(lineChart);
        root.setBottom(subscribeButton);
        BorderPane.setMargin(subscribeButton, new javafx.geometry.Insets(10));

        startChartUpdate();

        // Настройка сцены
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Car Count Chart");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icons/graphicon.png")));
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void addDataPoint(int carCount) {
        carCountHistory.add(carCount);
        Platform.runLater(() -> {
            carSeries.getData().add(new XYChart.Data<>(timeInSeconds++, carCount));
        });
    }

    private void startChartUpdate() {
        if (carCountUpdateTimeline != null) {
            carCountUpdateTimeline.stop();
        }

        // Таймер для обновления графика каждую секунду
        carCountUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int carCount = numberOfCars;
            addDataPoint(carCount);
        }));
        carCountUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        carCountUpdateTimeline.play();
    }

    @Override
    public short getSubStatus() {
        return subStatus;
    }

    @Override
    public void update(int passingCars, int numberOfCars) {
        this.numberOfCars = numberOfCars;
    }
}
