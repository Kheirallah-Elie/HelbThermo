package com.example;

public class ManualHeatingMode implements IHeatingModeStrategy {

    private static volatile ManualHeatingMode instance = null;

    private ManualHeatingMode(){};

    public static ManualHeatingMode getInstance() {
        if (instance == null){
            synchronized(ManualHeatingMode.class){ // verify no other threads are instantiating it either
                if (instance == null){ // instantiate only once
                    instance = new ManualHeatingMode();
                }
            }
        }
        return instance; 
    }

    @Override
    public void heat() {
        GlobalSystem.getView().deactivateLeftButtons(false);
    }   
}