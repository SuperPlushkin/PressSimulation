package org.example.presssimulation;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Piston extends Pane {

    private final double HEIGHT = 100;
    private final double BASE_WIDTH = 40;
    private final double PRESS_WIDTH = 60;

    private final double maxExtension = 210;
    private double pistonArea; // такая же площадь как у материала

    private Rectangle base;
    private Rectangle extension;
    private Rectangle press;

    private double currentExtension = 0;
    private final double startX;
    private final double startY;
    private double currentForce = 0;

    public Piston(double x, double y, double area) {
        this.startX = x;
        this.startY = y;
        this.pistonArea = area;

        createBase();
        createExtension();
        createPress();

        updatePosition();
    }

    private void createBase() {
        base = new Rectangle(0, 0, BASE_WIDTH, HEIGHT);
        base.setFill(Color.DARKGRAY);
        base.setStroke(Color.BLACK);
        getChildren().add(base);
    }
    private void createExtension() {
        extension = new Rectangle(BASE_WIDTH, 10, 0, HEIGHT - 20);
        extension.setFill(Color.DARKBLUE);
        extension.setStroke(Color.BLACK);
        getChildren().add(extension);
    }
    private void createPress() {
        press = new Rectangle(BASE_WIDTH, 0, PRESS_WIDTH / 2, HEIGHT);
        press.setFill(Color.DARKBLUE);
        press.setStroke(Color.BLACK);
        getChildren().add(press);
    }

    private void updatePosition() {
        setLayoutX(startX);
        setLayoutY(startY);

        extension.setWidth(currentExtension);
        press.setX(BASE_WIDTH + currentExtension);
    }
    public void reset(){
        this.currentExtension = 0;
        setHydraulicPressure(0);
        updatePosition();
    }

    public void extend(double amount) {
        if(currentExtension == maxExtension)
            return;

        if (currentExtension + amount > maxExtension)
            amount = maxExtension - currentExtension;

        currentExtension += amount;
        updatePosition();
    }
    public void retract(double amount) {
        if(currentExtension == 0)
            return;

        currentExtension -= currentExtension - amount >= 0 ? amount : currentExtension;
        updatePosition();
    }

    public void setHydraulicPressure(double pressure) {
        this.currentForce = pressure * pistonArea; // F = P × A
    }
    public double getPistonTotalWidth() {
        return currentExtension + PRESS_WIDTH + 10;
    }
    public double getCurrentForce() {
        return currentForce;
    }

    public void setPistonArea(double pistonArea) {
        this.pistonArea = pistonArea;
    }
}