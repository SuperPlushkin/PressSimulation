package org.example.presssimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Optional;

public class SimulationManager extends Application {

    private final double TUBE_WIDTH = 400;
    private final double TUBE_HEIGHT = 120;

    private double MAX_PRESSURE = 150_000_000;
    private double PRESSURE_INCREMENT = 10_000_000;
    private double CONTACT_AREA = 0.01;

    private Piston piston;
    private Material material;
    private AnimationTimer simulationTimer;
    private boolean isPressing = false;
    private boolean isAutoMode = false;
    private double currentPressure = 0;

    private Label pressureLabel;
    private Label forceLabel;
    private Label materialInfoLabel;
    private Label systemParamsLabel;
    private Pane simulationPane;
    private Label modeLabel;
    private Button modeToggleButton;
    private ComboBox<Material> materialComboBox;
    private ObservableList<Material> materialList;

    @Override
    public void start(Stage primaryStage) {

        double SCENE_WIDTH = 800;
        double SCENE_HEIGHT = 600;

        BorderPane root = new BorderPane();

        simulationPane = new Pane();
        simulationPane.setPrefSize(500, SCENE_HEIGHT);



        double centerX = 250;
        double centerY = SCENE_HEIGHT / 2;

        Rectangle tube = new Rectangle(centerX - TUBE_WIDTH / 2, centerY - TUBE_HEIGHT / 2, TUBE_WIDTH, TUBE_HEIGHT);
        tube.setFill(Color.GRAY);
        tube.setStroke(Color.BLACK);


        double materialSize = 80;
        double materialX = centerX + (TUBE_WIDTH / 2 - 20) - materialSize;
        double materialY = centerY - (materialSize / 2);

        material = Material.createGlass(materialX, materialY, 0.01, materialSize);
        piston = new Piston(centerX - TUBE_WIDTH / 2 + 20, centerY - 50, CONTACT_AREA);

        simulationPane.getChildren().addAll(tube, material, piston);




        VBox infoPanel = createInfoPanel();
        infoPanel.setPrefSize(300, SCENE_HEIGHT);
        infoPanel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");

        root.setCenter(simulationPane);
        root.setRight(infoPanel);

        initializeMaterials(materialX, materialY, materialSize);



        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        setupKeyboardControls(scene);
        setupAnimationTimer();

        primaryStage.setTitle("СИМУЛЯТОР ПРЕССА 3Д");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private VBox createInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label title = new Label("СИМУЛЯТОР ПРЕССА");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Кнопка информации
        Button infoButton = new Button("Информация");
        infoButton.setStyle("-fx-font-size: 12; -fx-padding: 5 10;");
        infoButton.setOnAction(e -> showInfoDialog());
        infoButton.setFocusTraversable(false);

        // Кнопка настроек параметров системы
        Button settingsButton = new Button("Настроить параметры системы");
        settingsButton.setStyle("-fx-font-size: 12; -fx-padding: 5 10;");
        settingsButton.setOnAction(e -> showSettingsDialog());
        settingsButton.setFocusTraversable(false);

        Label modeTitle = new Label("РЕЖИМ СИМУЛЯЦИИ:");
        modeTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        modeLabel = new Label("Мануальный режим");
        modeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #333;");

        modeToggleButton = new Button("Переключить на Авто");
        modeToggleButton.setStyle("-fx-font-size: 11; -fx-padding: 3 8;");
        modeToggleButton.setFocusTraversable(false);
        modeToggleButton.setOnAction(e -> toggleSimulationMode());


        // ComboBox для выбора материалов
        Label materialSelectLabel = new Label("Выбор материала:");
        materialSelectLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        materialComboBox = new ComboBox<>();
        materialComboBox.setPrefWidth(250);
        materialComboBox.setFocusTraversable(false);
        materialComboBox.setCellFactory(param -> new MaterialListCell());
        materialComboBox.setButtonCell(new MaterialListCell());

        // Слушатель выбора материала
        materialComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) switchMaterial(newVal);
        });

        // Кнопка добавления пользовательского материала
        Button addMaterialButton = new Button("Добавить материал");
        addMaterialButton.setStyle("-fx-font-size: 11; -fx-padding: 3 8;");
        addMaterialButton.setFocusTraversable(false);
        addMaterialButton.setOnAction(e -> addCustomMaterial());

        systemParamsLabel = new Label(String.format(
            """
            ПАРАМЕТРЫ СИСТЕМЫ:
            • Площадь контакта: %.3f м² (%.1f см²)
            • Макс. давление: %.0f МПа
            • Скорость давления: %.0f МПа/с
            """,
            CONTACT_AREA,
            CONTACT_AREA * 10_000,
            MAX_PRESSURE / 1_000_000,
            PRESSURE_INCREMENT / 1_000_000
        ));
        systemParamsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #333;");

        materialInfoLabel = new Label();
        updateMaterialInfo();

        pressureLabel = new Label("Давление: 0 МПа");
        pressureLabel.setStyle("-fx-font-size: 12;");

        forceLabel = new Label("Сила: 0 кН");
        forceLabel.setStyle("-fx-font-size: 12;");

        // Краткая справка по управлению
        Label quickControls = new Label("Управление:\n→ - вперед (только мануал)\n← - назад (только мануал)\nR - сброс");
        quickControls.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        panel.getChildren().addAll(
            title, infoButton, settingsButton, modeTitle, modeLabel, modeToggleButton,
            materialSelectLabel, materialComboBox, addMaterialButton, systemParamsLabel,
            materialInfoLabel, pressureLabel, forceLabel, quickControls
        );
        return panel;
    }

    private void showInfoDialog() {
        pauseSimulation();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о симуляторе");
        alert.setHeaderText("Физика процесса и управление");

        String infoText = """
            ФИЗИЧЕСКАЯ ФОРМУЛА:
            F(разр) = σ × A
            Где:
            - σ - предел прочности материала [Па]
            - A - площадь контакта [м²]
            - F - разрушающая сила [Н]

            УПРАВЛЕНИЕ:
            → - движение поршня вперед
            ← - движение поршня назад
            R - полный сброс симуляции

            ВЫБОР МАТЕРИАЛОВ:
            Используйте выпадающий список для выбора
            стандартных материалов или создайте свой
            с помощью кнопки "Добавить материал"

            ПРИНЦИП РАБОТЫ:
            При контакте поршня с материалом автоматически
            начинается давление. Когда сила (F)
            достигает разрушающей силы материала (Fразр) -
            происходит разрушение.
            """;

        alert.setContentText(infoText);
        alert.showAndWait();
        resumeSimulation();
    }
    private void showSettingsDialog() {
        pauseSimulation();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Настройки параметров системы");
        dialog.setHeaderText("Настройте параметры пресса");

        // Создаем ползунки
        javafx.scene.control.Slider areaSlider = new javafx.scene.control.Slider(0.001, 0.1, CONTACT_AREA);
        areaSlider.setShowTickLabels(true);
        areaSlider.setShowTickMarks(true);
        areaSlider.setMajorTickUnit(0.01);
        areaSlider.setBlockIncrement(0.001);

        javafx.scene.control.Slider pressureSlider = new javafx.scene.control.Slider(50, 300, MAX_PRESSURE / 1_000_000);
        pressureSlider.setShowTickLabels(true);
        pressureSlider.setShowTickMarks(true);
        pressureSlider.setMajorTickUnit(50);
        pressureSlider.setBlockIncrement(10);

        javafx.scene.control.Slider speedSlider = new javafx.scene.control.Slider(1, 50, PRESSURE_INCREMENT / 1_000_000);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(10);
        speedSlider.setBlockIncrement(1);

        // Labels для отображения значений
        Label areaValue = new Label(String.format("%.3f м²", CONTACT_AREA));
        Label pressureValue = new Label(String.format("%.0f МПа", MAX_PRESSURE / 1_000_000));
        Label speedValue = new Label(String.format("%.0f МПа/с", PRESSURE_INCREMENT / 1_000_000));

        // Слушатели изменений ползунков
        areaSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            CONTACT_AREA = newVal.doubleValue();
            areaValue.setText(String.format("%.3f м²", CONTACT_AREA));
        });

        pressureSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            MAX_PRESSURE = newVal.doubleValue() * 1_000_000;
            pressureValue.setText(String.format("%.0f МПа", newVal.doubleValue()));
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            PRESSURE_INCREMENT = newVal.doubleValue() * 1_000_000;
            speedValue.setText(String.format("%.0f МПа/с", newVal.doubleValue()));
        });

        CheckBox resetSimulationCheckbox = new CheckBox("Сбросить симуляцию после применения настроек");
        resetSimulationCheckbox.setSelected(true); // По умолчанию включено

        // Сетка для расположения элементов
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Площадь контакта:"), 0, 0);
        grid.add(areaSlider, 1, 0);
        grid.add(areaValue, 2, 0);

        grid.add(new Label("Макс. давление:"), 0, 1);
        grid.add(pressureSlider, 1, 1);
        grid.add(pressureValue, 2, 1);

        grid.add(new Label("Скорость давления:"), 0, 2);
        grid.add(speedSlider, 1, 2);
        grid.add(speedValue, 2, 2);

        grid.add(resetSimulationCheckbox, 0, 3, 3, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateSystemParameters();
            if (resetSimulationCheckbox.isSelected())
                resetSimulation();
        }
        resumeSimulation();
    }
    private void updateMaterialInfo() {
        materialInfoLabel.setText(String.format(
            "Материал: %s\nПрочность: %.0f МПа\nF(разр): %.0f кН\nТип: %s",
            material.getName(),
            material.getCompressiveStrength() / 1_000_000,
            material.getDestructionForce() / 1000,
            material.isFragile() ? "Хрупкий" : "Пластичный"
        ));
    }
    private void updateSystemParameters() {
        systemParamsLabel.setText(String.format(
            """
            ПАРАМЕТРЫ СИСТЕМЫ:
            • Площадь контакта: %.3f м² (%.1f см²)
            • Макс. давление: %.0f МПа
            • Скорость давления: %.0f МПа/с
            """,
            CONTACT_AREA,
            CONTACT_AREA * 10_000,
            MAX_PRESSURE / 1_000_000,
            PRESSURE_INCREMENT / 1_000_000
        ));

        piston.setPistonArea(CONTACT_AREA);

        int pistonIndex = simulationPane.getChildren().indexOf(piston);
        if (pistonIndex >= 0) {
            simulationPane.getChildren().set(pistonIndex, piston);
        }
    }

    private void initializeMaterials(double x, double y, double size) {
        double contactArea = 0.01;

        materialList = FXCollections.observableArrayList(
                Material.createSteel(x, y, contactArea, size),
                Material.createConcrete(x, y, contactArea, size),
                Material.createGlass(x, y, contactArea, size),
                Material.createWood(x, y, contactArea, size)
        );

        materialComboBox.setItems(materialList);
        materialComboBox.getSelectionModel().select(0);
    }
    private void switchMaterial(Material selectedMaterial) {
        int index = simulationPane.getChildren().indexOf(material);

        if (index >= 0) {
            selectedMaterial.setLayoutX(material.getLayoutX());
            selectedMaterial.setLayoutY(material.getLayoutY());

            simulationPane.getChildren().set(index, selectedMaterial);
            material = selectedMaterial;
            updateMaterialInfo();
            resetSimulation();
        }
    }
    private void addCustomMaterial() {
        // Диалог для ввода названия
        TextInputDialog nameDialog = new TextInputDialog("Мой материал");
        nameDialog.setTitle("Создание материала");
        nameDialog.setHeaderText("Введите название материала");
        nameDialog.setContentText("Название:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty() || nameResult.get().trim().isEmpty()) {
            return;
        }
        String name = nameResult.get().trim();

        // Диалог для ввода прочности
        TextInputDialog strengthDialog = new TextInputDialog("50");
        strengthDialog.setTitle("Создание материала");
        strengthDialog.setHeaderText("Введите предел прочности (МПа)");
        strengthDialog.setContentText("Прочность (МПа):");

        Optional<String> strengthResult = strengthDialog.showAndWait();
        if (strengthResult.isEmpty()) {
            return;
        }

        try {
            double strength = Double.parseDouble(strengthResult.get());
            if (strength <= 0) {
                throw new NumberFormatException();
            }

            // Диалог для выбора типа материала
            Alert typeDialog = new Alert(Alert.AlertType.CONFIRMATION);
            typeDialog.setTitle("Создание материала");
            typeDialog.setHeaderText("Выберите тип материала");
            typeDialog.setContentText("Материал хрупкий или пластичный?");

            ButtonType fragileButton = new ButtonType("Хрупкий");
            ButtonType plasticButton = new ButtonType("Пластичный");
            ButtonType cancelButton = new ButtonType("Отмена", ButtonType.CANCEL.getButtonData());

            typeDialog.getButtonTypes().setAll(fragileButton, plasticButton, cancelButton);

            Optional<ButtonType> typeResult = typeDialog.showAndWait();
            if (typeResult.isEmpty() || typeResult.get() == cancelButton) {
                return;
            }

            boolean isFragile = typeResult.get() == fragileButton;

            // Создание нового материала
            Material customMaterial = new Material(
                    material.getX(), material.getY(), 80,
                    name, strength, isFragile, material.getContactArea()
            );

            // Добавление в список и выбор
            materialList.add(customMaterial);
            materialComboBox.getSelectionModel().select(customMaterial);
            switchMaterial(customMaterial);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Успех");
            successAlert.setHeaderText("Материал создан");
            successAlert.setContentText(String.format(
                    "Материал '%s' успешно создан!\nПрочность: %.0f МПа\nТип: %s",
                    name, strength, isFragile ? "хрупкий" : "пластичный"
            ));
            successAlert.showAndWait();

        } catch (NumberFormatException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Ошибка");
            errorAlert.setHeaderText("Неверный ввод");
            errorAlert.setContentText("Пожалуйста, введите корректное положительное число для прочности.");
            errorAlert.showAndWait();
        }
    }
    private static class MaterialListCell extends ListCell<Material> {
        @Override
        protected void updateItem(Material item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            }
            else {
                setText(String.format("%s (σ=%.0f МПа, %s)", item.getName(), item.getCompressiveStrength() / 1_000_000, item.isFragile() ? "хрупкий" : "пластичный"));
            }
        }
    } // Класс для отображения материалов в ComboBox

    private void setupKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();

            if (isAutoMode && (key == KeyCode.LEFT || key == KeyCode.RIGHT))
                return;

            if (key == KeyCode.LEFT) {
                piston.retract(15);
                handleNeedOfPressing();
            } else if (key == KeyCode.RIGHT) {
                piston.extend(15);
                handleNeedOfPressing();
            } else if (key == KeyCode.R) {
                resetSimulation();
            }
        });
    }
    private void setupAnimationTimer() {
        simulationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void stop(){
                super.stop();
                lastUpdate = 0;
            }

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;

                if (isAutoMode && !material.isBroken()) {
                    piston.extend(100 * elapsedSeconds);

                    if (isContact() && !isPressing)
                        startPressing();
                }

                if (isPressing && !material.isBroken()) {
                    currentPressure += PRESSURE_INCREMENT * elapsedSeconds;

                    if (currentPressure > MAX_PRESSURE)
                        currentPressure = MAX_PRESSURE;

                    piston.setHydraulicPressure(currentPressure);
                    material.applyForce(piston.getCurrentForce());

                    pressureLabel.setText(String.format("Давление: %.1f МПа", currentPressure / 1_000_000));
                    forceLabel.setText(String.format("Сила: %.0f кН", piston.getCurrentForce() / 1000));

                    if (material.isBroken()) {
                        pressureLabel.setStyle("-fx-font-size: 12; -fx-text-fill: red;");
                        forceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: red;");
                    }
                }

                lastUpdate = now;
            }
        };
        simulationTimer.start();
    }

    private void handleNeedOfPressing() {
        if(material.isBroken())
            return;

        boolean isContact = isContact();

        if (isContact && !isPressing) {
            startPressing();
        } else if (!isContact) {
            stopPressing();
        }
    }
    private boolean isContact() {
        double pistonRightEdge = piston.getLayoutX() + piston.getPistonTotalWidth();
        double materialLeftEdge = material.getX();
        return pistonRightEdge >= materialLeftEdge;
    }

    private void pauseSimulation() {
        simulationTimer.stop();
    }
    private void toggleSimulationMode() {
        isAutoMode = !isAutoMode;

        if (isAutoMode) {
            modeLabel.setText("Автоматический режим");
            modeToggleButton.setText("Переключить на Мануал");
            startAutoSimulation();
        } else {
            modeLabel.setText("Мануальный режим");
            modeToggleButton.setText("Переключить на Авто");
            stopAutoSimulation();
        }

        resetSimulation();
    }
    private void resumeSimulation() {
        simulationTimer.start();
    }

    private void startAutoSimulation() {
        isAutoMode = true;
    }
    private void stopAutoSimulation() {
        isAutoMode = false;
    }

    private void startPressing() {
        isPressing = true;
        currentPressure = 0;
        piston.setHydraulicPressure(0);
        pressureLabel.setStyle("-fx-font-size: 12; -fx-text-fill: black;");
        forceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: black;");
    }
    private void stopPressing() {
        isPressing = false;
        material.reset();
        pressureLabel.setText("Давление: 0 МПа");
        forceLabel.setText("Сила: 0 кН");
    }

    private void resetSimulation() {
        material.reset();
        piston.reset();

        isPressing = false;
        currentPressure = 0;

        pressureLabel.setText("Давление: 0 МПа");
        pressureLabel.setStyle("-fx-font-size: 12; -fx-text-fill: black;");

        forceLabel.setText("Сила: 0 кН");
        forceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: black;");
    }
}