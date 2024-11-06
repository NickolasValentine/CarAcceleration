package com.example.caracceleration;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class TrafficLight extends VBox {
    private Circle redLight;
    private Circle yellowLight;
    private Circle greenLight;
    private Rectangle trafficBody;
    private Rectangle trafficPillar;
    private Text timerText;

    // Цвета для огней светофора
    private final Color RED_COLOR = Color.rgb(221, 46, 68);
    private final Color YELLOW_COLOR = Color.rgb(255, 204, 77);
    private final Color GREEN_COLOR = Color.rgb(119, 178, 85);
    private final Color BODY_COLOR = Color.rgb(49, 55, 61);

    public TrafficLight() {
        initializeLights();
    }

    public void initializeLights() { // Create and initialize all traffic light circles and time
        trafficBody = new Rectangle(100, 200); // Create a traffic light body (rectangle)
        trafficBody.setArcWidth(20);
        trafficBody.setArcHeight(20);
        trafficBody.setFill(BODY_COLOR);

        trafficPillar = new Rectangle(10, 200); // Create a traffic light body (rectangle)
        trafficPillar.setFill(BODY_COLOR);

        redLight = createLight(RED_COLOR);
        yellowLight = createLight(YELLOW_COLOR);
        greenLight = createLight(GREEN_COLOR);

        timerText = new Text("Time: 0");
        // Vertical container for lanterns with 15px padding between them
        VBox lightBox = new VBox(15, redLight, yellowLight, greenLight);
        lightBox.setAlignment(Pos.TOP_CENTER);

        // Wrap the lights and body in a StackPane to center them
        StackPane trafficLightBody = new StackPane();
        trafficLightBody.setMargin(lightBox, new Insets(25));
        trafficLightBody.getChildren().addAll(trafficBody, lightBox);
        trafficLightBody.setAlignment(Pos.TOP_CENTER);

        VBox trafficLightAll = new VBox();
        trafficLightAll.getChildren().addAll(trafficLightBody, trafficPillar);
        trafficLightAll.setAlignment(Pos.TOP_CENTER);

        this.getChildren().addAll(timerText, trafficLightAll);
    }

    public Circle createLight(Color color) { // Create and initialize traffic light circles
        Circle light = new Circle(20);
        light.setFill(Color.GRAY); // Initially off
        return light;
    }

    public void setColor(Circle circle, Color color) { circle.setFill(color); }
    public void setTimerText(String timerText) { this.timerText.setText(timerText); }
    public Circle getRedLight() { return redLight; }
    public Circle getYellowLight() { return yellowLight; }
    public Circle getGreenLight() { return greenLight; }
    public Color getRED_COLOR() { return RED_COLOR; }
    public Color getYELLOW_COLOR() { return YELLOW_COLOR; }
    public Color getGREEN_COLOR() { return GREEN_COLOR; }

    public void resetLights() { // Reset all colors
        redLight.setFill(Color.GRAY);
        yellowLight.setFill(Color.GRAY);
        greenLight.setFill(Color.GRAY);
    }
}
