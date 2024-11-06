package com.example.caracceleration;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightApp extends Application {
    private List<Car> cars;
    private Pane roadPane;
    private TrafficLight trafficLight;
    private Controller controller;
    private Label passingCarsLabel;
    private CarChartWindow carChartWindow;

    @Override
    public void start(Stage primaryStage) {
        trafficLight = new TrafficLight();
        cars = new ArrayList<>();

        // Label для общего количества пройденных авто
        passingCarsLabel = new Label("Cars passed: 0");
        passingCarsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

        // Создание кнопок и слайдеров для управления светофором
        VBox controls = createControlPanel();

        // Дорожное полотно
        roadPane = new Pane();
        roadPane.setPrefSize(1600, 400);
        roadPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        roadPane.getChildren().addAll(trafficLight, passingCarsLabel); // Добавление светофора в правую часть дороги
        trafficLight.setLayoutX(1500);  // Позиция светофора ближе к правому краю
        trafficLight.setLayoutY(10);
        passingCarsLabel.setLayoutX(10); // Позиция в левом верхнем углу
        passingCarsLabel.setLayoutY(10);

        // Настройка окна
        primaryStage.setOnCloseRequest(event -> { controller.stopController(); });

        // Основной контейнер
        VBox root = new VBox(10, roadPane, controls);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 1700, 550);
        primaryStage.setTitle("Traffic Light with Cars");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/traffic-light.png")));
        primaryStage.setScene(scene);
        primaryStage.show();


        controller = new Controller(roadPane,this, cars, trafficLight);
        //carController = new CarController(roadPane, cars, trafficLightController);
        controller.start();

        carChartWindow = new CarChartWindow(controller);
    }

    private void openConfigurationWindow() {
        Stage configStage = new Stage();
        configStage.initModality(Modality.APPLICATION_MODAL);
        configStage.setTitle("Configure Phase Durations");

        // UI to control phase durations with labeled sliders
        VBox redDurationSlider = createDurationSlider("Red Duration", 5); // Red Slider + label
        VBox redYellowDurationSlider = createDurationSlider("Red-Yellow Duration", 2); // redYellow Slider + label
        VBox yellowDurationSlider1 = createDurationSlider("Yellow Duration (Before Green)", 2); // yellow Slider + label
        VBox greenDurationSlider = createDurationSlider("Green Duration", 5); // green Slider + label
        VBox flashingGreenDurationSlider = createDurationSlider("Flashing Green Duration", 3); // flashingGreen Slider + label
        VBox yellowDurationSlider2 = createDurationSlider("Yellow Duration (After Green)", 2); // yellow Slider + label
        // Field for car spawn interval
        Label spawnLabel = new Label("Car Spawn Interval (sec):");
        TextField spawnIntervalField = new TextField();
        spawnIntervalField.setText(String.valueOf(controller.getArrivalRate()));

        // Layout for car spawn interval
        HBox spawnIntervalLayout = new HBox(10, spawnLabel, spawnIntervalField);

        // Main layout with all configurations
        VBox configLayout = new VBox(10,
                redDurationSlider, redYellowDurationSlider, yellowDurationSlider1,
                greenDurationSlider, flashingGreenDurationSlider, yellowDurationSlider2,
                spawnIntervalLayout);

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            controller.setPhaseDuration(0, (int) ((Slider) redDurationSlider.getChildren().get(1)).getValue());
            controller.setPhaseDuration(1, (int) ((Slider) redYellowDurationSlider.getChildren().get(1)).getValue());
            controller.setPhaseDuration(2, (int) ((Slider) yellowDurationSlider1.getChildren().get(1)).getValue());
            controller.setPhaseDuration(3, (int) ((Slider) greenDurationSlider.getChildren().get(1)).getValue());
            controller.setPhaseDuration(4, (int) ((Slider) flashingGreenDurationSlider.getChildren().get(1)).getValue());
            controller.setPhaseDuration(5, (int) ((Slider) yellowDurationSlider2.getChildren().get(1)).getValue());

            // Validate and set car spawn interval
            String input = spawnIntervalField.getText();
            try {
                int spawnInterval = Integer.parseInt(input);
                if (spawnInterval >= 0) {
                    controller.arrivalRate(spawnInterval);
                } else {
                    showAlert("Please enter a non-negative integer for the car spawn interval.");
                }
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid integer for the car spawn interval.");
            }

            configStage.close();
        });

        configLayout.getChildren().add(applyButton);
        configStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/settingsicon.png")));
        Scene configScene = new Scene(configLayout, 350, 480);
        configStage.setScene(configScene);
        configStage.show();
    }

    // Helper method to display an error alert
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Creating a Slider and its Text
    private VBox createDurationSlider(String labelText, int defaultValue) {
        Label label = new Label(labelText);
        Slider slider = new Slider(1, 10, defaultValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setSnapToTicks(true);

        VBox vbox = new VBox(5, label, slider); // Label + Slider with spacing
        return vbox;
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Traffic Light");
        startButton.setOnAction(event -> { // The start button also updates the entered time
            if (!controller.isRunning && !controller.isPaused) { // If the traffic light is not running (turn it on)
                System.out.println("Starting Traffic Light");
                controller.startTL();
            } else if (controller.isPaused) { // If the traffic light is paused (resume it)
                controller.resumeTL();
            }
        });

        Button stopButton = new Button("Stop Traffic Light");
        stopButton.setOnAction(event -> controller.stopTL());

        Button configureButton = new Button("Configure Phases");
        configureButton.setOnAction(e -> openConfigurationWindow());

        // Создаем кнопку для открытия окна графика
        Button chartButton = new Button("Show Car Count Chart");
        chartButton.setOnAction(event -> carChartWindow.show());

        controlPanel.getChildren().addAll(startButton, stopButton, configureButton, chartButton);
        return controlPanel;
    }

    // Метод для обновления текста метки с количеством прошедших авто
    public void updatePassingCarsLabel() {
        passingCarsLabel.setText("Cars passed: " + controller.getPassingCars()); // Обновляем метку);
    }

    public static void main(String[] args) {
        launch(args);
    }
}