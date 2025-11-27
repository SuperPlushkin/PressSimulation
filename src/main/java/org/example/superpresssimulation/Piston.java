package org.example.superpresssimulation;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Piston extends Pane {
    private static final double BASE_WIDTH = 40;
    private static final double HEIGHT = 100;
    private static final double PRESS_WIDTH = 60;
    private static final double MAX_EXTENSION = 210;
    private static final double PISTON_AREA = 0.01; // площадь поршня в м² (10 см²)

    private Rectangle base;       // Основная часть поршня
    private Rectangle extension;  // Выдвижная часть
    private Rectangle press;      // Пресс на конце

    private double currentExtension = 0;
    private final double startX;
    private final double startY;
    private double hydraulicPressure = 0; // давление в Паскалях
    private double currentForce = 0; // сила в Ньютонах

    public Piston(double x, double y) {
        this.startX = x;
        this.startY = y;

        createBase(Color.DARKGRAY);
        createExtension(Color.DARKBLUE);
        createPress(Color.DARKBLUE);

        updatePosition();
    }

    private void createBase(Color color) {
        base = new Rectangle(0, 0, BASE_WIDTH, HEIGHT);
        base.setFill(color);
        base.setStroke(Color.BLACK);
        getChildren().add(base);
    }
    private void createExtension(Color color) {
        extension = new Rectangle(BASE_WIDTH, 10, 0, HEIGHT - 20);
        extension.setFill(color);
        extension.setStroke(Color.BLACK);
        getChildren().add(extension);
    }
    private void createPress(Color color) {
        press = new Rectangle(BASE_WIDTH, 0, PRESS_WIDTH / 2, HEIGHT);
        press.setFill(color);
        press.setStroke(Color.BLACK);
        getChildren().add(press);
    }

    private void updatePosition() {
        setLayoutX(startX);
        setLayoutY(startY);

        extension.setWidth(currentExtension);
        press.setX(BASE_WIDTH + currentExtension);
    }

    public void extend(double amount) {
        if (currentExtension + amount <= MAX_EXTENSION) {
            currentExtension += amount;
            updatePosition();
        }
    }
    public void retract(double amount) {
        if (currentExtension - amount >= 0) {
            currentExtension -= amount;
            updatePosition();
        }
        else {
            currentExtension = 0;
            updatePosition();
        }
    }

    public void setHydraulicPressure(double pressure) {
        this.hydraulicPressure = pressure;
        // Рассчитываем силу: F = P × A
        this.currentForce = pressure * PISTON_AREA;
    }

    public double getCurrentExtension() {
        return currentExtension;
    }
    public double getTotalWidth() {
        return BASE_WIDTH + currentExtension + PRESS_WIDTH;
    }
    public double getCurrentForce() {
        return currentForce;
    }
    public double getHydraulicPressure() {
        return hydraulicPressure;
    }
}