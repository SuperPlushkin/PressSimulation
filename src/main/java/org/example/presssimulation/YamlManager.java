package org.example.presssimulation;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import java.io.*;
import java.util.*;

public class YamlManager {
    private static final String SETTINGS_FILE = "settings.yaml";
    private static final Yaml yaml;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
    }

    public static void saveSystemSettings(double maxPressure, double pressureIncrement, double contactArea) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            Map<String, Object> settings = new LinkedHashMap<>();

            settings.put("max_pressure", maxPressure);
            settings.put("pressure_increment", pressureIncrement);
            settings.put("contact_area", contactArea);

            data.put("system_settings", settings);

            yaml.dump(data, new FileWriter(SETTINGS_FILE));
            System.out.println("Настройки сохранены в " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    public static void createDefaultFile(double x, double y) {
        Map<String, Object> data = new LinkedHashMap<>();

        // Системные настройки по умолчанию
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("max_pressure", 150_000_000);
        settings.put("pressure_increment", 10_000_000);
        settings.put("contact_area", 0.01);
        data.put("system_settings", settings);

        String[][] defaultMaterials = {
            {"Сталь", "250", "false"},
            {"Бетон", "30", "true"},
            {"Стекло", "50", "true"},
            {"Дерево", "40", "false"}
        };

        List<Map<String, Object>> materialsList = new ArrayList<>();

        for (String[] mat : defaultMaterials) {
            Map<String, Object> matData = new LinkedHashMap<>();
            matData.put("name", mat[0]);
            matData.put("strength_mpa", Double.parseDouble(mat[1]));
            matData.put("fragile", Boolean.parseBoolean(mat[2]));
            matData.put("contact_area", 0.01);
            materialsList.add(matData);
        }
        data.put("materials", materialsList);

        try {
            yaml.dump(data, new FileWriter(SETTINGS_FILE));
            System.out.println("Создан файл с настройками по умолчанию: " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка создания файла: " + e.getMessage());
        }
    }

    public static Map<String, Double> loadSystemSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            System.out.println("Файл настроек не найден, будут созданы значения по умолчанию");
            return null;
        }

        try {
            InputStream inputStream = new FileInputStream(SETTINGS_FILE);
            Map<String, Object> data = yaml.load(inputStream);

            if (data != null && data.containsKey("system_settings")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = (Map<String, Object>) data.get("system_settings");

                Map<String, Double> result = new HashMap<>();
                result.put("max_pressure", Double.parseDouble(settings.get("max_pressure").toString()));
                result.put("pressure_increment", Double.parseDouble(settings.get("pressure_increment").toString()));
                result.put("contact_area", Double.parseDouble(settings.get("contact_area").toString()));

                return result;
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки настроек: " + e.getMessage());
        }
        return null;
    }
    public static List<Material> loadMaterials(double x, double y) {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            System.out.println("Файл настроек не найден");
            return null;
        }

        try {
            InputStream inputStream = new FileInputStream(SETTINGS_FILE);
            Map<String, Object> data = yaml.load(inputStream);

            if (data != null && data.containsKey("materials")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> materialsList = (List<Map<String, Object>>) data.get("materials");

                List<Material> materials = new ArrayList<>();
                for (Map<String, Object> matData : materialsList) {
                    String name = (String) matData.get("name");
                    double strength = Double.parseDouble(matData.get("strength_mpa").toString());
                    boolean fragile = (Boolean) matData.get("fragile");
                    double contactArea = Double.parseDouble(matData.get("contact_area").toString());

                    materials.add(new Material(x, y, name, strength, fragile, contactArea));
                }

                return materials;
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки материалов: " + e.getMessage());
        }
        return null;
    }

    public static void saveAll(double maxPressure, double pressureIncrement, double contactArea, List<Material> materials) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();

            // Системные настройки
            Map<String, Object> settings = new LinkedHashMap<>();
            settings.put("max_pressure", maxPressure);
            settings.put("pressure_increment", pressureIncrement);
            settings.put("contact_area", contactArea);
            data.put("system_settings", settings);

            // Материалы
            List<Map<String, Object>> materialsList = new ArrayList<>();
            for (Material material : materials) {
                Map<String, Object> matData = new LinkedHashMap<>();
                matData.put("name", material.getName());
                matData.put("strength_mpa", material.getCompressiveStrength() / 1_000_000);
                matData.put("fragile", material.isFragile());
                matData.put("contact_area", material.getContactArea());
                materialsList.add(matData);
            }
            data.put("materials", materialsList);

            yaml.dump(data, new FileWriter(SETTINGS_FILE));
            System.out.println("Все данные сохранены в " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
        }
    }
}