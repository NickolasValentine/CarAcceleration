package com.example.caracceleration;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Car extends ImageView {
    private double speed = 0; // Скорость
    private static final double MAX_SPEED = 8; // Макс. скорость
    private static final double ACCELERATION = 0.1; // Ускорение
    private static final double DECELERATION = 0.3; // Замедление
    private static final double SAFE_DISTANCE = 120; // Минимальное расстояние между автомобилями
    private static final double BRAKE_DISTANCE = 110; // Дистанция начала торможения перед светофором
    //cars/Porsche911turbos.png
    private static final String IMAGE_PATH = "cars/Porsche911turbos.png";
    public Car(double x) {

        super(loadCarImage());
        setFitWidth(100); // Устанавливаем ширину изображения
        setFitHeight(40); // Устанавливаем высоту изображения
        setLayoutX(x);
        setLayoutY(300);
    }

    private static Image loadCarImage() {
        return new Image(Car.class.getResource(IMAGE_PATH).toExternalForm());
    }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getMaxSpeed() { return MAX_SPEED; }
    public double getAcceleration() { return ACCELERATION; }
    public double getDeceleration() { return DECELERATION; }
    public double getSafeDistance() { return SAFE_DISTANCE; }
    public double getBrakeDistance() { return BRAKE_DISTANCE; }
}
