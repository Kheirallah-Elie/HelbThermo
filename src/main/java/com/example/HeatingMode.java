package com.example;

public class HeatingMode {
    IHeatingModeStrategy heatingMode;

    public HeatingMode(IHeatingModeStrategy heatingMode){
        this.heatingMode = heatingMode;
    }

    public void setStrategy(IHeatingModeStrategy newStrategy){
        this.heatingMode = newStrategy;
    }

    public void heat(){
        heatingMode.heat();
    }
}