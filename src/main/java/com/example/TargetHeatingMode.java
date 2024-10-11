package com.example;

public class TargetHeatingMode implements IHeatingModeStrategy {

    private static volatile TargetHeatingMode instance = null; // Singleton pattern
    int targetTemperature = 20; // set average target temperature here for this heating mode

    private TargetHeatingMode(){};

    public static TargetHeatingMode getInstance() {
        if (instance == null){
            synchronized(TargetHeatingMode.class){
                if (instance == null){ // instantiate only once
                    instance = new TargetHeatingMode();
                }
            }
        }
        return instance; 
    }

    @Override
    public void heat() {
        
        GlobalSystem.getView().deactivateLeftButtons(true);

        if (GlobalSystem.getAverageInternalTemperature() >= targetTemperature){
            // stop heating (stop all heated cells)
            setActive(false);
        } else if (GlobalSystem.getAverageInternalTemperature() < targetTemperature) {
            // heat again
            setActive(true);
        }
    }

    private void setActive(Boolean state){
        for (int i = 0; i < GlobalSystem.getCellsArray().length; i++) { // look for all the heated cells and deactivate them
            for (int j = 0; j < GlobalSystem.getCellsArray()[0].length; j++) {
                if (GlobalSystem.getCellsArray()[i][j].getType().equals(GlobalSystem.getCellHeated())){
                    GlobalSystem.getCellsArray()[i][j].setIsHeatedCellActive(state);
                }
            }
        }
    }
}