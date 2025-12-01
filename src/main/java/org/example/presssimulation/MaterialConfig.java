package org.example.presssimulation;

public class MaterialConfig {
    private String name;
    private double compressiveStrengthMPa;
    private boolean fragile;
    private double contactArea;

    // Конструкторы
    public MaterialConfig() {}

    public MaterialConfig(String name, double compressiveStrengthMPa, boolean fragile, double contactArea) {
        this.name = name;
        this.compressiveStrengthMPa = compressiveStrengthMPa;
        this.fragile = fragile;
        this.contactArea = contactArea;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCompressiveStrengthMPa() { return compressiveStrengthMPa; }
    public void setCompressiveStrengthMPa(double compressiveStrengthMPa) { this.compressiveStrengthMPa = compressiveStrengthMPa; }

    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }

    public double getContactArea() { return contactArea; }
    public void setContactArea(double contactArea) { this.contactArea = contactArea; }

    public Material toMaterial(double x, double y) {
        return new Material(x, y, name, compressiveStrengthMPa, fragile, contactArea);
    }
    public static MaterialConfig fromMaterial(Material material) {
        return new MaterialConfig(
            material.getName(),
            material.getCompressiveStrength() / 1_000_000,
            material.isFragile(),
            material.getContactArea()
        );
    }
}
