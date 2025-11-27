package org.example.superpresssimulation;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Material extends Pane {
    public static final double SIZE = 80;
    private static final Color DEFAULT_COLOR = Color.DARKRED;
    private static final Color BROKEN_COLOR = Color.DARKGRAY;

    private Rectangle shape;
    private final String name;
    private final double strength; // прочность в Ньютонах
    private final boolean isFragile; // хрупкость
    private boolean isBroken = false;
    private double currentForce = 0; // текущая сила

    public Material(double x, double y, String name, double strength, boolean isFragile) {
        this.name = name;
        this.strength = strength;
        this.isFragile = isFragile;

        createShape(x, y);
        createLabel();
    }

    // Конструкторы для разных материалов
    public static Material createSteel(double x, double y) {
        return new Material(x, y, "Сталь", 100000, false);
    }

    public static Material createConcrete(double x, double y) {
        return new Material(x, y, "Бетон", 50000, true);
    }

    public static Material createGlass(double x, double y) {
        return new Material(x, y, "Стекло", 15000, true);
    }

    public static Material createWood(double x, double y) {
        return new Material(x, y, "Дерево", 30000, false);
    }

    public static Material createPlastic(double x, double y) {
        return new Material(x, y, "Пластик", 20000, false);
    }

    private void createShape(double x, double y) {
        shape = new Rectangle(x, y, SIZE, SIZE);
        shape.setFill(DEFAULT_COLOR);
        shape.setStroke(Color.BLACK);
        getChildren().add(shape);
    }

    private void createLabel() {
        updateLabel();
    }

    private void updateLabel() {
        getChildren().removeIf(node -> node instanceof Text);

        Text label = new Text(shape.getX() + 5, shape.getY() + 15, name);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        getChildren().add(label);

        Text strengthText = new Text(shape.getX() + 5, shape.getY() + 30,
                String.format("S=%.0f Н", strength));
        strengthText.setFill(Color.WHITE);
        strengthText.setStyle("-fx-font-size: 10;");
        getChildren().add(strengthText);

        Text fragileText = new Text(shape.getX() + 5, shape.getY() + 45,
                isFragile ? "Хрупкий" : "Прочный");
        fragileText.setFill(isFragile ? Color.ORANGE : Color.LIGHTGREEN);
        fragileText.setStyle("-fx-font-size: 9;");
        getChildren().add(fragileText);

        if (isBroken) {
            Text brokenText = new Text(shape.getX() + 5, shape.getY() + 60, "СЛОМАН!");
            brokenText.setFill(Color.RED);
            brokenText.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
            getChildren().add(brokenText);
        } else {
            Text forceText = new Text(shape.getX() + 5, shape.getY() + 60,
                    String.format("F=%.0f Н", currentForce));
            forceText.setFill(getForceColor());
            forceText.setStyle("-fx-font-size: 10;");
            getChildren().add(forceText);
        }
    }

    private Color getForceColor() {
        double forceRatio = currentForce / strength;
        if (forceRatio > 0.8) return Color.RED;
        if (forceRatio > 0.6) return Color.ORANGE;
        if (forceRatio > 0.3) return Color.YELLOW;
        return Color.WHITE;
    }

    public void applyForce(double force) {
        if (isBroken) return;

        currentForce = force;

        if (force >= strength) {
            breakMaterial();
        }

        updateLabel();
        updateVisualFeedback();
    }

    private void updateVisualFeedback() {
        double forceRatio = currentForce / strength;

        if (forceRatio > 0.8) {
            shape.setFill(Color.RED);
        } else if (forceRatio > 0.6) {
            shape.setFill(Color.ORANGERED);
        } else if (forceRatio > 0.4) {
            shape.setFill(Color.ORANGE);
        } else if (forceRatio > 0.2) {
            shape.setFill(Color.YELLOW);
        } else {
            shape.setFill(DEFAULT_COLOR);
        }

        // Визуальная деформация для хрупких материалов
        if (isFragile && forceRatio > 0.5) {
            double deformation = forceRatio * 0.15; // до 15% деформации
            shape.setScaleX(1.0 - deformation);
        }
    }

    private void breakMaterial() {
        isBroken = true;
        shape.setFill(BROKEN_COLOR);
        shape.setStroke(Color.RED);

        // Эффект разрушения для хрупких материалов
        if (isFragile) {
            shape.setScaleX(0.7);
            shape.setScaleY(0.7);
        }

        updateLabel();
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

    // Геттеры
    public double getStrength() { return strength; }
    public boolean isBroken() { return isBroken; }
    public double getX() { return shape.getX(); }
    public double getCurrentForce() { return currentForce; }
    public String getName() { return name; }
    public boolean isFragile() { return isFragile; }
}