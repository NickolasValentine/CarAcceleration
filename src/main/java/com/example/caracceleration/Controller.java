package com.example.caracceleration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

public class Controller extends Thread implements ISubscription, IArrivalRate, IPhaseDuration {
    enum Mode { NORMAL, SYNCHRONOUS }
    private Mode currentMode = Mode.NORMAL;
    private volatile boolean running = true;
    private Timeline carGeneration;
    private List<Car> cars;
    private Pane roadPane;
    private int arrivalRate;



    private TrafficLight trafficLight;
    private String[] phases = {"Red", "RedYellow", "Yellow", "Green", "FlashingGreen", "Yellow"}; // Phase array
    private int[] phaseDurations = {5, 2, 2, 5, 3, 2}; // Default phase durations
    private int currentPhaseIndex = 0; // Current Phase
    boolean isRunning = false; // Startup state
    boolean isPaused = false;  // Flag for tracking pause state
    private Timeline timeline; // Timer for phases
    private int timeRemaining; // Current phase time
    private Timeline flashingGreenTimeline; // Timer for flashing green
    private Timeline flashingYellowTimeline; // Flashing Yellow Timer
    private int passingCars;

    public Controller(Pane roadPane, List<Car> cars, TrafficLight trafficLight) {
        this.cars = cars;
        this.roadPane = roadPane;
        arrivalRate = 2;

        this.trafficLight = trafficLight;

        setPhase(0); // Set the initial position
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public void resetCars() {
        cars.clear();
        passingCars = 0;
    }

    @Override
    public void onPhaseChange(String newPhase, int remainingTime) {
        System.out.println("Phase changed to: " + newPhase + ", Remaining time: " + remainingTime);
    }
    @Override
    public int getNumberOfCars() { return cars.size(); }

    @Override
    public int[] getPhaseDurations(){ return phaseDurations; }

    @Override
    public String[] getPhases() { return phases; }

    @Override
    public int getPassingCars(){ return passingCars; }


    ///////////////////
    // CarController //
    ///////////////////

    public void stopController() {
        running = false;
    }

    @Override
    public void run() {
        startCarGeneration();
        startCarMovement();
        initializeTimer();
        while (running) {;
            try {
                Thread.sleep(1); // Обновляем состояние каждую секунду
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startCarGeneration() {
        carGeneration = new Timeline(new KeyFrame(Duration.seconds(arrivalRate), event -> {
            double startPosition = 0;
            double carWidth = 100; // Ширина машины
            double safeDistance = 120; // Безопасное расстояние

            // Проверяем, не занимает ли какая-либо машина интервал от -100 до 100
            boolean isSpaceOccupied = cars.stream()
                    .anyMatch(car -> car.getLayoutX() > -carWidth && car.getLayoutX() < carWidth);

            Car newCar;

            if (isSpaceOccupied) {
                // Если пространство занято, ставим машину позади последней
                Car lastCar = cars.get(cars.size() - 1);
                startPosition = lastCar.getLayoutX() - safeDistance;
            }

            newCar = new Car(startPosition);

            roadPane.getChildren().add(newCar);
            cars.add(newCar);
        }));
        carGeneration.setCycleCount(Timeline.INDEFINITE);
        carGeneration.play();
    }

    private void startCarMovement() {
        Timeline movement = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            if(currentMode == Mode.NORMAL) {
                for (int i = 0; i < cars.size(); i++) {
                    Car car = cars.get(i);
                    Car previousCar = i > 0 ? cars.get(i - 1) : null;
                    updateCarsIndividually(car, previousCar);
                }
            }
            else {
                for (Car car : cars) { updateCarsSynchronously(car); }
            }

            // Удаляем машины, которые проехали предел экрана (например, 1650 по X)
            cars.removeIf(car -> {
                boolean isOutOfBounds = car.getLayoutX() > 1650;
                if (isOutOfBounds) {
                    roadPane.getChildren().remove(car); // Удаляем машину с панели
                    passingCars++;
                }
                return isOutOfBounds;
            });
        }));
        movement.setCycleCount(Timeline.INDEFINITE);
        movement.play();
    }

    private void updateCarsSynchronously(Car car) {
        double targetSpeed = car.getMaxSpeed();
        // Учитываем торможение перед светофором
        double distanceToTrafficLight = getTrafficLight().getLayoutX() - car.getLayoutX();
        if (!isGreen() && !isFlashingGreen()) {
            // Начинаем замедление перед светофором, если он не зелёный
            targetSpeed = 0;
        }

        // Регулируем скорость автомобиля
        if (car.getSpeed() < targetSpeed) {
            car.setSpeed(Math.min(targetSpeed, car.getSpeed() + car.getAcceleration())); // Ускорение
        } else {
            car.setSpeed(Math.max(0, car.getSpeed() - car.getDeceleration())); // Торможение
        }

        // Обновляем позицию машины
        car.setLayoutX(car.getLayoutX() + car.getSpeed());
    }

    public void updateCarsIndividually(Car car, Car previousCar) {
        double distanceToPrevious = previousCar != null ? previousCar.getLayoutX() - car.getLayoutX() : Double.MAX_VALUE;
        distanceToPrevious-= 100;
        double targetSpeed = car.getMaxSpeed();

        // Учитываем торможение перед светофором
        double distanceToTrafficLight = getTrafficLight().getLayoutX() - car.getLayoutX();
        if (distanceToTrafficLight < car.getBrakeDistance() && distanceToTrafficLight > 0 && !isGreen() && !isFlashingGreen()) {
            // Начинаем замедление перед светофором, если он не зелёный
            targetSpeed = 0;
        }

        // Проверяем, нужно ли замедлиться из-за близости к предыдущему автомобилю
        if (distanceToPrevious < car.getSafeDistance()) {
            // Замедление для поддержания безопасной дистанции
            targetSpeed = Math.min(targetSpeed, previousCar.getSpeed());
        }
        if (distanceToPrevious < 0) {
            // Замедление для поддержания безопасной дистанции
            targetSpeed = 0;
        }

        // Регулируем скорость автомобиля
        if (car.getSpeed() < targetSpeed) {
            car.setSpeed(Math.min(targetSpeed, car.getSpeed() + car.getAcceleration())); // Ускорение
        } else {
            car.setSpeed(Math.max(0, car.getSpeed() - car.getDeceleration())); // Торможение
        }

        // Обновляем позицию машины
        car.setLayoutX(car.getLayoutX() + car.getSpeed());
    }

    @Override
    public void arrivalRate(int time) {
        arrivalRate = time;
        restartCarGeneration();
    }
    private void restartCarGeneration() {
        if (carGeneration != null) {
            carGeneration.stop();
        }
        startCarGeneration(); // Запускаем заново с обновленным интервалом
    }
    public int getArrivalRate() { return arrivalRate; }

    ////////////////////////////
    // TrafficLightController //
    ////////////////////////////

    private void initializeTimer() { // Initialize the state timer
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateTimer();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTimer() { // Updating the timer
        if (timeRemaining > 0) {
            timeRemaining--;
            trafficLight.setTimerText("Time: " + timeRemaining);
//            if (listener != null) {
//                listener.onPhaseChange(phases[currentPhaseIndex], timeRemaining);
//            }
        } else {
            switchToNextPhase(); // If time is up, update color index
        }
    }

    private void switchToNextPhase() { // update the color index and change the color
        currentPhaseIndex = (currentPhaseIndex + 1) % phases.length;
        setPhase(currentPhaseIndex);
    }

    private void setPhase(int phaseIndex) {
        currentPhaseIndex = phaseIndex;
        timeRemaining = phaseDurations[currentPhaseIndex]; // Set the time of the current state
        trafficLight.resetLights(); // Reset colors
        stopFlashingGreen(); // Stop the green light from flashing if it was there

        switch (phases[phaseIndex]) {
            case "Red":
                trafficLight.setColor(trafficLight.getRedLight(), trafficLight.getRED_COLOR());
                break;
            case "RedYellow":
                trafficLight.setColor(trafficLight.getRedLight(), trafficLight.getRED_COLOR());
                trafficLight.setColor(trafficLight.getYellowLight(), trafficLight.getYELLOW_COLOR());
                break;
            case "Yellow":
                trafficLight.setColor(trafficLight.getYellowLight(), trafficLight.getYELLOW_COLOR());
                break;
            case "Green":
                trafficLight.setColor(trafficLight.getGreenLight(), trafficLight.getGREEN_COLOR());
                break;
            case "FlashingGreen":
                startFlashingGreen();
                break;
        }
    }

    private void startFlashingGreen() { // Start green blinking
        flashingGreenTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            if (trafficLight.getGreenLight().getFill() == Color.GRAY) {
                trafficLight.setColor(trafficLight.getGreenLight(), trafficLight.getGREEN_COLOR());
            } else {
                trafficLight.setColor(trafficLight.getGreenLight(), Color.GRAY);
            }
        }));
        flashingGreenTimeline.setCycleCount(Timeline.INDEFINITE);
        flashingGreenTimeline.play();
    }

    private void stopFlashingGreen() {  // Stop blinking green
        if (flashingGreenTimeline != null) {
            flashingGreenTimeline.stop();
            trafficLight.setColor(trafficLight.getGreenLight(), Color.GRAY);
        }
    }

    private void startFlashingYellow() { // Start green Yellow
        flashingYellowTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            if (trafficLight.getYellowLight().getFill() == Color.GRAY) {
                trafficLight.setColor(trafficLight.getYellowLight(), trafficLight.getYELLOW_COLOR());
            } else {
                trafficLight.setColor(trafficLight.getYellowLight(), Color.GRAY);
            }
        }));
        flashingYellowTimeline.setCycleCount(Timeline.INDEFINITE); // Бесконечный цикл мигания
        flashingYellowTimeline.play(); // Запуск анимации
    }

    private void stopFlashingYellow() { // Stop blinking Yellow
        if (flashingYellowTimeline != null) {
            flashingYellowTimeline.stop(); // Остановить таймер
            trafficLight.setColor(trafficLight.getYellowLight(), Color.GRAY);
        }
    }

    public void startTL() {
        isRunning = true;
        timeline.play(); // Start the main traffic light timer
    }

    public void resumeTL() {
        if (isPaused) {
            isRunning = true;   // Resume the traffic light
            stopFlashingYellow();
            timeline.play();    // Restarting the timer
            restoreCurrentPhaseLight(); // Restore the current light
            isPaused = false;   // Resetting the pause flag
        }
    }

    private void restoreCurrentPhaseLight() { // Restore the current light
        trafficLight.resetLights();  // Turn off all lights
        stopFlashingGreen(); // Stop the green light from flashing if it was there

        switch (phases[currentPhaseIndex]) {
            case "Red":
                trafficLight.setColor(trafficLight.getRedLight(), trafficLight.getRED_COLOR());break;
            case "RedYellow":
                trafficLight.setColor(trafficLight.getRedLight(), trafficLight.getRED_COLOR());
                trafficLight.setColor(trafficLight.getYellowLight(), trafficLight.getYELLOW_COLOR());
                break;
            case "Yellow":
                trafficLight.setColor(trafficLight.getYellowLight(), trafficLight.getYELLOW_COLOR());
                break;
            case "Green":
                trafficLight.setColor(trafficLight.getGreenLight(), trafficLight.getGREEN_COLOR());
                break;
            case "FlashingGreen":
                startFlashingGreen();
                break;
        }
    }

    public void stopTL() {
        if (isRunning) {
            isPaused = true;    // Flag that the traffic light is paused
            timeline.stop();     // Stop the main timer
            stopFlashingGreen(); // Stop green blinking if active
            trafficLight.resetLights(); // Reset colors
            startFlashingYellow(); // startFlashingYellow(); If the traffic light is off, the yellow light flashes

        }
        isRunning = false;  // Please note that the traffic light is not running
    }

    @Override
    public void setPhaseDuration(int phaseIndex, int duration) {
        phaseDurations[phaseIndex] = duration;
    }

    @Override
    public void setCurrentPhaseIndex(int phaseIndex) {
        this.currentPhaseIndex = phaseIndex;
        timeRemaining = phaseDurations[currentPhaseIndex];
        isRunning = false;
        stopFlashingGreen();
        stopFlashingYellow();
        trafficLight.resetLights();
        setPhase(currentPhaseIndex);
        timeline.stop();

    }

    public boolean isGreen() {
        return "Green".equals(phases[currentPhaseIndex]);
    }

    public boolean isFlashingGreen() {
        return "FlashingGreen".equals(phases[currentPhaseIndex]);
    }
    public TrafficLight getTrafficLight() { return trafficLight; }
}
