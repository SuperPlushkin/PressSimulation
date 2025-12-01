package org.example.presssimulation;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Material extends Pane {

    private static final Color DEFAULT_COLOR = Color.GOLD;
    private static final Color BROKEN_COLOR = Color.DARKGRAY;

    private Rectangle shape;
    private final String name;
    private final double size;
    private final double compressiveStrength; // предел прочности на сжатие в Паскалях
    private final double destructionForce; // разрушающая сила в Ньютонах (рассчитывается автоматически)
    private final double contactArea; // площадь контакта в м²
    private final boolean isFragile;
    private boolean isBroken = false;
    private double currentForce = 0;

    public Material(double x, double y, double size, String name, double compressiveStrengthMPa, boolean isFragile, double contactArea) {
        this.name = name;
        this.compressiveStrength = compressiveStrengthMPa * 1_000_000; // Конвертируем МПа в Па
        this.destructionForce = this.compressiveStrength * contactArea; // F = σ × A
        this.isFragile = isFragile;
        this.contactArea = contactArea;
        this.size = size;

        createShape(x, y, size);
        createLabel();
    }

    public static Material createSteel(double x, double y, double contactArea, double size) {
        return new Material(x, y, size, "Сталь", 250, false, contactArea);
    }
    public static Material createConcrete(double x, double y, double contactArea, double size) {
        return new Material(x, y, size, "Бетон", 30, true, contactArea);
    }
    public static Material createGlass(double x, double y, double contactArea, double size) {
        return new Material(x, y, size, "Стекло", 50, true, contactArea);
    }
    public static Material createWood(double x, double y, double contactArea, double size) {
        return new Material(x, y, size, "Дерево", 40, false, contactArea);
    }

    private void createShape(double x, double y, double size) {
        shape = new Rectangle(0, 0, size, size);
        shape.setFill(DEFAULT_COLOR);
        shape.setStroke(Color.BLACK);
        getChildren().add(shape);

        setLayoutX(x);
        setLayoutY(y);
    }

    private void createLabel() {
        updateLabel();
    }
    private void updateVisualFeedback() {
        double forceRatio = currentForce / destructionForce;

        if (forceRatio > 0.9) {
            shape.setFill(Color.RED);
        } else if (forceRatio > 0.7) {
            shape.setFill(Color.ORANGERED);
        } else if (forceRatio > 0.5) {
            shape.setFill(Color.DARKORANGE);
        } else if (forceRatio > 0.3) {
            shape.setFill(Color.ORANGE);
        } else {
            shape.setFill(Color.GOLD);
        }
    }
    private void updateLabel() {
        getChildren().removeIf(node -> node instanceof Text);

        double gap = 5;

        // Название материала
        Text label = new Text(gap, 15, name);
        label.setFill(Color.BLACK);
        label.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        getChildren().add(label);

        // Предел прочности
        Text strengthText = new Text(gap, 30, String.format("σ=%.0f МПа", compressiveStrength / 1_000_000));
        strengthText.setFill(Color.BLACK);
        strengthText.setStyle("-fx-font-size: 10;");
        getChildren().add(strengthText);

        // Разрушающая сила
        Text forceText = new Text(gap, 45, String.format("Fразр=%.0f кН", destructionForce / 1000));
        forceText.setFill(Color.BLACK);
        forceText.setStyle("-fx-font-size: 9;");
        getChildren().add(forceText);

        // Тип материала
        Text fragileText = new Text(gap, 60, isFragile ? "Хрупкий" : "Пластичный");
        fragileText.setFill(Color.BLACK);
        fragileText.setStyle("-fx-font-size: 9;");
        getChildren().add(fragileText);

        // Текущее состояние
        if (isBroken) {
            Text brokenText = new Text(gap, 75, "РАЗРУШЕН!");
            brokenText.setFill(Color.BLACK);
            brokenText.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
            getChildren().add(brokenText);
        } else {
            Text currentForceText = new Text(gap, 75, String.format("F=%.0f кН", currentForce / 1000));
            currentForceText.setFill(Color.BLACK);
            currentForceText.setStyle("-fx-font-size: 10;");
            getChildren().add(currentForceText);
        }
    }

    public void reset() {
        isBroken = false;
        currentForce = 0;
        shape.setFill(DEFAULT_COLOR);
        shape.setStroke(Color.BLACK);
        shape.setScaleX(1.0);
        shape.setScaleY(1.0);
        updateLabel();
    }

    public void applyForce(double force) {
        if (isBroken)
            return;

        currentForce = force;

        if (force >= destructionForce) // F >= σ × A
            breakMaterial();

        updateLabel();
        updateVisualFeedback();
    }
    private void breakMaterial() {
        isBroken = true;
        shape.setFill(BROKEN_COLOR);

        updateLabel();
    }

    public double getX() {
        return getLayoutX();
    }
    public double getY() {
        return getLayoutY();
    }
    public String getName() { return name; }
    public double getCompressiveStrength() { return compressiveStrength; }
    public double getDestructionForce() { return destructionForce; }
    public double getContactArea() { return contactArea; }
    public boolean isBroken() { return isBroken; }
    public boolean isFragile() { return isFragile; }
}