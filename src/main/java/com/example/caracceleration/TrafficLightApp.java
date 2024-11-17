package com.example.caracceleration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightApp extends Application implements IObserver {
    private List<Car> cars;
    private Pane roadPane;
    private TrafficLight trafficLight;
    private Controller controller;
    private Label passingCarsLabel;
    private Timeline carCounting;
    private ToggleGroup modeToggleGroup;
    private RadioButton normalModeButton;
    private RadioButton synchronousModeButton;
    private int goTime;
    private int passingCars;

    public short subStatus;

    @Override
    public void start(Stage primaryStage) {
        trafficLight = new TrafficLight();
        cars = new ArrayList<>();

        // Label для общего количества пройденных авто
        passingCarsLabel = new Label("Cars passed: 0   Time: 0");
        passingCarsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

        // Создание кнопок и слайдеров для управления светофором
        VBox controls = createControlPanel();

        // Дорожное полотно
        roadPane = new Pane();
        roadPane.setPrefSize(1600, 100);
        roadPane.setTranslateY(300);
        roadPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        roadPane.getChildren().addAll(trafficLight); // Добавление светофора в правую часть дороги
        trafficLight.setLayoutX(1500);  // Позиция светофора ближе к правому краю
        trafficLight.setLayoutY(-300);
        //passingCarsLabel.setLayoutX(10); // Позиция в левом верхнем углу
        //passingCarsLabel.setLayoutY(10);

        // Настройка окна
        primaryStage.setOnCloseRequest(event -> { controller.stopController(); });
        // Основной контейнер
        VBox root = new VBox(10, passingCarsLabel, roadPane, controls);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 1700, 550);
        primaryStage.setTitle("Traffic Light with Cars");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/traffic-light.png")));
        primaryStage.setScene(scene);
        primaryStage.show();


        controller = new Controller(roadPane, cars, trafficLight);
        controller.start();
        goTime = 0;
        subStatus = 1;
        controller.register(this);// Подписка
        updatePassingCarsLabel();
        controller.startTL();
    }

    @Override
    public short getSubStatus() { return subStatus; }

    @Override
    public void update(int passingCars, int numberOfCars) {
        this.passingCars = passingCars;
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
        chartButton.setOnAction(event -> {
            CarChartWindow cCW = new CarChartWindow(goTime);
            controller.register(cCW);
            cCW.show();
        });

        // Радиокнопки для выбора режима
        normalModeButton = new RadioButton("Normal Mode");
        synchronousModeButton = new RadioButton("Synchronous Mode");
        modeToggleGroup = new ToggleGroup();
        normalModeButton.setToggleGroup(modeToggleGroup);
        synchronousModeButton.setToggleGroup(modeToggleGroup);
        normalModeButton.setSelected(true); // По умолчанию выбран обычный режим

        // Обработчик переключения режимов
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> switchMode());

        HBox modeSelectionBox = new HBox(10, normalModeButton, synchronousModeButton);
        modeSelectionBox.setAlignment(Pos.CENTER);

        controlPanel.getChildren().addAll(startButton, stopButton, configureButton, chartButton, modeSelectionBox);
        return controlPanel;
    }

    // Метод для переключения режима
    private void switchMode() {
        // Очистка дороги от всех автомобилей
        roadPane.getChildren().removeAll(cars);
        cars.clear();
        controller.resetCars();
        updatePassingCarsLabel();
        goTime = 0;
        controller.setCurrentPhaseIndex(0);
        // Переключение режима
        if (normalModeButton.isSelected()) {
            controller.setMode(Controller.Mode.NORMAL);
        } else if (synchronousModeButton.isSelected()) {
            controller.setMode(Controller.Mode.SYNCHRONOUS);
        }
        controller.startTL();
    }

    // Метод для обновления текста метки с количеством прошедших авто
    public void updatePassingCarsLabel() {
        if (carCounting != null) {
            carCounting.stop();
        }
        // Таймер для обновления графика каждую секунду
        carCounting = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            goTime++;
            passingCarsLabel.setText("Cars passed: " + passingCars + "   Time: " + goTime); // Обновляем метку;
        }));
        carCounting.setCycleCount(Timeline.INDEFINITE);
        carCounting.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}