package com.example;

public class Cell {

    private static int heatedCellCounter = 0;// to count heated cells

    private String type; // Dead cell? Heated cell? Normal cell?

    private int idHeatedCell = 0; // give the heated cells an ID
    private int redLevel = 255; // starts at 255 for the blue and the green in the view, the lower this value is, the higher the red is
    private int markCell = 0;  // 2 is for heated cells, 1 is for close to heated, and 0 is for furthest away from heated cells, this is used to mark the cells to calculate the probability of the dead cells generation
    private int maxCellTemperature = 100;
    private int minCellTemperature = 0;

    private double temperature;
    private double heatedCellTargetTemperature = 100; 

    private Boolean isHeatedCellActive;

    public Cell(String type, double temperature) {
        this.type = type;
        this.temperature = temperature;

        if (type.equals("Heated")) {
            this.temperature = heatedCellTargetTemperature;
            markCell = 2; // marked heated
            updateRedLevel();
        }
        incrementHeatedCellId(); // if heated, increment the Id
    }

    public Boolean getIsHeatedCellActive() {
        return isHeatedCellActive;
    }

    public void setIsHeatedCellActive(Boolean isHeatedCellActive) {
        this.temperature = heatedCellTargetTemperature;
        updateRedLevel();
        this.isHeatedCellActive = isHeatedCellActive;
    }

    public void setHeatedCellTargetTemperature(double heatedCellTargetTemperature) {
        this.heatedCellTargetTemperature = clampTemperature(heatedCellTargetTemperature); // this method clamps temperatures between 0 and 100
    }
    
    public void setTemperature(double temperature) {
        this.temperature = clampTemperature(temperature); // this method clamps temperatures between 0 and 100
    }
    

    public int getIdHeatedCell() {
        return idHeatedCell;
    }

    public String getType(){
        return this.type;
    }

    public int getRedLevel() {
        return this.redLevel;
    }

    public int getMarkCell(){
        return this.markCell;
    }

    public void setMarkCell(int markHeated) {
        this.markCell = markHeated;
    }

    public void setType(String type){
        this.type = type;
        if (type.equals("Dead")) {
            this.temperature = 0;
        } // set dead cells to 0 so they no longer affect other cells

        incrementHeatedCellId(); // if heated
    }

    public double getTemperature(){
        return this.temperature;
    }

    public void updateRedLevel() {// not sure if I have to put this color logic in the View instead
        int tempInInt = (int)temperature;
        int rgbMaxLevel = 255;
        int rgbMinLevel = 0;
        int redInfluence = 3;

        if (tempInInt >= (rgbMaxLevel/redInfluence)) { // Max temperature 255 will stop at 0, dividing by redInfluence, because below I am multiplying by redInfluence, this prevents a crash
            redLevel = rgbMinLevel;
        } else if (tempInInt < rgbMinLevel) {
            redLevel = rgbMaxLevel;
        } else {
            redLevel = rgbMaxLevel - (tempInInt*redInfluence); // calculate distance between 255 (max usable RGB values) and the temperature
        }
    }

    public void incrementHeatedCellId() {
        if (type.equals("Heated")) {
            heatedCellCounter++; // Increment the counter for each heated cell
            idHeatedCell = heatedCellCounter; // Assign the current value of the counter as the ID for the heated cell
            isHeatedCellActive = false;
        }
    }

    private double clampTemperature(double temperature) {  // this method clamps temperatures of cells between 0 and 100
        if (temperature < minCellTemperature) { // prevents cells to go below 0
            return minCellTemperature;
        } else if (temperature > maxCellTemperature) { // prevents cells to go above 100
            return maxCellTemperature;
        } else {
            return temperature;
        }
    }
}