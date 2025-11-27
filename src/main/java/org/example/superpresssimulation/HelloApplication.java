package org.example.superpresssimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private final double SCENE_WIDTH = 600;
    private final double SCENE_HEIGHT = 300;
    private final double TUBE_WIDTH = 400;
    private final double TUBE_HEIGHT = 120;

    private final Color TUBE_COLOR = Color.GRAY;

    private Piston piston;
    private Material material;
    private boolean isPressing = false;
    private double currentPressure = 0; // давление в Паскалях
    private final double MAX_PRESSURE = 15_000_000; // 15 МПа максимальное давление
    private final double PRESSURE_INCREMENT = 2_000_000; // 2 МПа в секунду

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        double centerX = SCENE_WIDTH / 2;
        double centerY = SCENE_HEIGHT / 2;

        Rectangle tube = createTube(centerX, centerY);

        // Создаем материал (по умолчанию сталь)
        material = Material.createSteel(
                centerX + TUBE_WIDTH / 2 - 20 - Material.SIZE,
                centerY - Material.SIZE / 2
        );

        piston = new Piston(centerX - TUBE_WIDTH / 2 + 20, centerY - 50);

        root.getChildren().addAll(tube, material, piston);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        setupKeyboardControls(scene);
        setupAnimationTimer();

        primaryStage.setTitle("Супер пресс симулятор - Управление: 1-5 материалы, R сброс");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Rectangle createTube(double centerX, double centerY) {
        Rectangle tube = new Rectangle(
                centerX - TUBE_WIDTH / 2,
                centerY - TUBE_HEIGHT / 2,
                TUBE_WIDTH,
                TUBE_HEIGHT
        );
        tube.setFill(TUBE_COLOR);
        tube.setStroke(Color.BLACK);
        return tube;
    }

    private void setupKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            double moveStep = 5.0;

            if (event.getCode() == KeyCode.RIGHT) {
                piston.extend(moveStep);
                checkContact();
            } else if (event.getCode() == KeyCode.LEFT) {
                piston.retract(moveStep);
                stopPressing();
            } else if (event.getCode() == KeyCode.R) {
                resetSimulation();
            } else if (event.getCode() == KeyCode.SPACE) {
                if (isContact() && !material.isBroken()) {
                    startPressing();
                }
            } else if (event.getCode() == KeyCode.DIGIT1) {
                switchMaterial(Material.createSteel(material.getX(), material.getY()));
            } else if (event.getCode() == KeyCode.DIGIT2) {
                switchMaterial(Material.createConcrete(material.getX(), material.getY()));
            } else if (event.getCode() == KeyCode.DIGIT3) {
                switchMaterial(Material.createGlass(material.getX(), material.getY()));
            } else if (event.getCode() == KeyCode.DIGIT4) {
                switchMaterial(Material.createWood(material.getX(), material.getY()));
            } else if (event.getCode() == KeyCode.DIGIT5) {
                switchMaterial(Material.createPlastic(material.getX(), material.getY()));
            }
        });
    }

    private void switchMaterial(Material newMaterial) {
        // Сохраняем текущую позицию в Pane
        int index = ((Pane)material.getParent()).getChildren().indexOf(material);
        ((Pane)material.getParent()).getChildren().set(index, newMaterial);
        material = newMaterial;
        resetSimulation();
    }

    private void setupAnimationTimer() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;

                if (isPressing && !material.isBroken()) {
                    // Увеличиваем давление в системе
                    currentPressure += PRESSURE_INCREMENT * elapsedSeconds;
                    if (currentPressure > MAX_PRESSURE) {
                        currentPressure = MAX_PRESSURE;
                    }

                    // Передаем давление поршню
                    piston.setHydraulicPressure(currentPressure);

                    // Прикладываем силу к материалу
                    material.applyForce(piston.getCurrentForce());

                    if (material.isBroken()) {
                        stopPressing();
                    }
                }

                lastUpdate = now;
            }
        };
        timer.start();
    }

    private void checkContact() {
        if (isContact() && !isPressing && !material.isBroken()) {
            startPressing();
        } else if (!isContact()) {
            stopPressing();
        }
    }

    private boolean isContact() {
        double pistonRightEdge = piston.getLayoutX() + piston.getTotalWidth();
        double materialLeftEdge = material.getX();
        return pistonRightEdge >= materialLeftEdge;
    }

    private void startPressing() {
        isPressing = true;
        currentPressure = 0;
        piston.setHydraulicPressure(0);
    }

    private void stopPressing() {
        isPressing = false;
    }

    private void resetSimulation() {
        material.reset();
        piston.retract(piston.getCurrentExtension());
        currentPressure = 0;
        piston.setHydraulicPressure(0);
        isPressing = false;
    }
}
