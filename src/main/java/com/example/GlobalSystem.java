package com.example;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;

public class GlobalSystem implements IThermoObservable
{    
    private final int ROWS = 9;
    private final int COLS = 9;
    private final String simulationDataFilename = "src/main/java/com/example/simul.data";

    private static ThermoView view; // static because I need to access it from the CellFactory class

    private static int minimumCells = 3;    // static because used in the Alert view without instanciating the controller
    private static int maximumCells = 12;
    private static int totalCostInEuros = 0;
    
    private int numberOfInfluence = 9; // this is the number of influence each cell has (either from outside temperature, either from other cells)

    private static double externalTemperature; // external temperature in degrees Celsius
    private static double cellTemperature = externalTemperature; // the temperature that the use sets for a heated cell, but initially, it is a normal cell that has the external temperature
    private static double averageInternalTemperature; // average temperature inside the golbal system

    private static String cellNormal = "Normal";// accessed without instantiation
    private static String cellHeated = "Heated";// accessed without instantiation
    private static String cellDead = "Dead";// accessed without instantiation
    private static String modeManual = "Manual mode";
    private static String modeTarget = "Target mode";

    private static String timeString = "0s";

    private String logEntry = "";

    private static Cell[][] cellsArray; // accessed without instantiation

    private List<ThermoView> observers; // Observable list, contains only the ThermoView in this project, not necessarily useful to have a list unless you need multiple views later
    private List<Cell> heatedCells;
    private HeatingMode mode;
    private CellMenuView openMenu;
    private ExternalTemperatureParser parser;

    private CellFactory cellFactory;
    private double[][]previousTemperatures; // to get the previous temperature to avoid inconsistency in the calculations

    private Boolean isRunning = false;
    private Boolean isActive = false;

    // Constructor
    public GlobalSystem (Stage primaryStage){
        if (ROWS >= minimumCells && COLS >= minimumCells && ROWS <= maximumCells && COLS <= maximumCells ){
            observers = new ArrayList<>();
            
            view = new ThermoView(primaryStage, ROWS, COLS);
            attachObserver(view);

            cellsArray = new Cell[ROWS][COLS];
            cellFactory = new CellFactory(cellsArray, ROWS, COLS);
            cellFactory.generateCells();

            view.drawGridCells(cellsArray);

            activateController(primaryStage);

            mode = new HeatingMode(ManualHeatingMode.getInstance());

            parser = new ExternalTemperatureParser(simulationDataFilename);
            externalTemperature = parser.getNextTemperature();

            logEntry = timeString+";"+totalCostInEuros+";"+averageInternalTemperature+";"+externalTemperature+"\n"; // initialise Log string

            previousTemperatures = copyPreviousTemperatures(cellsArray); 
            notifyObserver(); // to immediately have the colors visible on the start of the application

        } else { 
            AlertView alert = new AlertView(); // created a new view that will open an alert if the grid is larger than 12 or smaller than 3
        }
    }

    // Getters
    public static ThermoView getView() {
        return view;
    }

    public static int getMinimumCells() {
        return minimumCells;
    }

    public static int getMaximumCells() {
        return maximumCells;
    }

    public static double getCellTemperature() {
        return cellTemperature;
    }

    public static double getExternalTemperature() {
        return externalTemperature;
    }

    public static double getAverageInternalTemperature() {
        return averageInternalTemperature;
    }

    public static String getCellNormal() {
        return cellNormal;
    }

    public static String getCellHeated() {
        return cellHeated;
    }

    public static String getCellDead() {
        return cellDead;
    }

    public static String getModeManual() {
        return modeManual;
    }

    public static String getModeTarget() {
        return modeTarget;
    }

    public static Cell[][] getCellsArray() {
        return cellsArray;
    }

    public List<Cell> getHeatedCells() {
        return heatedCells;
    }

    public static int getTotalCostInEuros() {
        return totalCostInEuros;
    }

    public static String getTimeString() {
        return timeString;
    }


    // Observable implemented methods//
    @Override
    public void attachObserver(ThermoView observer) {
        observers.add(observer);
    }

    @Override
    public void detachObserver(ThermoView observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver() {
        for (ThermoView observer : observers) {
            observer.update();
        }
    }
    ////////////////////////////////////
    // The only public method that is used in the ThermoView to manipulate the left buttons
    public static void activateCell(int x, int y){
        cellsArray[x][y].setIsHeatedCellActive(!cellsArray[x][y].getIsHeatedCellActive()); //activate / deactivate heated cell
        view.updateCellColor(x, y, cellHeated, cellsArray[x][y].getRedLevel());
    }

    // Private
    private void activateController(Stage primaryStage){
        // Instanciating the Timeline here, so that it is instanciated only once
        Timeline timeLine = new Timeline(new KeyFrame(Duration.millis(1000), e -> run()));
        timeLine.setCycleCount(Animation.INDEFINITE);

        view.getButtonStart().setOnAction(action -> {
            isRunning = true;
            timeLine.play();
            setTimerState(true);
        });
        view.getButtonPause().setOnAction(action -> {
            timeLine.stop();
            setTimerState(false);
        });
        view.getButtonReset().setOnAction(action -> {
            reset();
        });
        
        // Loop necessary to set a click listener on all the cells inside the grid
        for (int i = 0; i < view.getCellButtonArray().length; i++) {
            for (int j = 0; j < view.getCellButtonArray()[0].length; j++) {
                int selectedRow = i; // saving the coordinates inside the array of cells to change their status if necessary
                int selectedCol = j;
                String cellPosition = "Row: "+i+"      Col: "+j+"";
                view.getCellButtonArray()[i][j].setOnAction(action -> {
                    view.getButtonPause().fire(); // javaFX method to automatically fire a button for the user
                    view.disableView(primaryStage); // Disabling the view, the user can no longer click on anything that is on the first view (check method in the ThermoView class)
                    
                    openMenu = new CellMenuView((cellPosition)); // Instantiating the new View

                    openMenu.getYesNoDefineDeadCellButton().setOnAction(e -> {
                        openMenu.changeButtonStatus(openMenu.getYesNoDefineDeadCellButton(), openMenu.getYesNoDefineHeatedCellButton());
                        openMenu.disableTemperatureUserEntry(true); // user can no longer enter a temperature, useless anyway as a dead cell will be set automatically to 0
                    });
                    openMenu.getYesNoDefineHeatedCellButton().setOnAction(e -> {
                        openMenu.changeButtonStatus(openMenu.getYesNoDefineHeatedCellButton(), openMenu.getYesNoDefineDeadCellButton());
                        openMenu.disableTemperatureUserEntry(false); // user can now enter a temperature
                    });

                    openMenu.getConfirmButton().setOnAction(e-> {    
                        if (openMenu.getYesNoDefineHeatedCellButton().isSelected()) {
                            cellTemperature = Integer.parseInt(openMenu.getTemperatureUserEntryText().getText());
                            if (!cellsArray[selectedRow][selectedCol].getType().equals(cellHeated)) {
                                cellTemperature = Integer.parseInt(openMenu.getTemperatureUserEntryText().getText());
                                cellsArray[selectedRow][selectedCol].setType(cellHeated); // set the actual object to Heated
                                cellsArray[selectedRow][selectedCol].setHeatedCellTargetTemperature(cellTemperature);
                                view.updateCellColor(selectedRow, selectedCol, cellHeated, cellsArray[selectedRow][selectedCol].getRedLevel()); // set it to heated in the View
                                view.drawActivatedCells(selectedRow, selectedCol, cellsArray[selectedRow][selectedCol].getIdHeatedCell()); // add the new heated cell to the left grid
                            } else {
                                // if it was already heated, just set its new defined temperature
                                cellsArray[selectedRow][selectedCol].setHeatedCellTargetTemperature(cellTemperature); 
                                cellsArray[selectedRow][selectedCol].setIsHeatedCellActive(false);
                                cellsArray[selectedRow][selectedCol].setIsHeatedCellActive(true); // refresh cell
                            }
                            cellsArray[selectedRow][selectedCol].updateRedLevel();
                        } else if (openMenu.getYesNoDefineDeadCellButton().isSelected()){
                            if (!cellsArray[selectedRow][selectedCol].getType().equals(cellHeated)){//cant turn heated cell to a dead cell
                                cellsArray[selectedRow][selectedCol].setType(cellDead);
                                view.updateCellColor(selectedRow, selectedCol, cellDead, cellsArray[selectedRow][selectedCol].getRedLevel());
                            } 
                        }
                        notifyObserver(); // to immediately set the text
                        openMenu.close();
                        
                        view.enableView(primaryStage);

                        if (isRunning){ // not ideal, but it is to prevent the timer to start when it has never been asked to start in the first place
                            view.getButtonStart().fire();
                        }   
                    });

                    openMenu.getCancelButton().setOnAction(e-> {
                        openMenu.close();
                        view.enableView(primaryStage);
                        if (isRunning){ // not idea, but it is to prevent the timer to start when it has never been asked to start in the first place
                            view.getButtonStart().fire();
                        }   
                    });
                });
            }
            // Save data to log file when closing the application
            primaryStage.setOnCloseRequest(action -> {
                DataSaver saver = new DataSaver(logEntry);
                saver.saveData();
            });
        }

        view.getHeatingMode().setOnAction(action -> {
            String selectedMode = view.getHeatingMode().getSelectionModel().getSelectedItem();
            if (selectedMode.equals(modeManual)) {
                mode.setStrategy(ManualHeatingMode.getInstance()); // change the heating mode's strategy without re-instantiating it
                mode.heat();
            } else if (selectedMode.equals(modeTarget)){
                mode.setStrategy(TargetHeatingMode.getInstance());
                mode.heat();
            }
        });
    }

    private void run(){
        updateBoard();
    }

    // Thermo model methods
    private void setTimerState(boolean value){
        isActive = value;
    }

    private void reset(){
        timeString = "0s";
        totalCostInEuros = 0;
        notifyObserver(); // this will immediately notify the observer
    }


    private void updateBoard(){
        if (isActive){
            externalTemperature = parser.getNextTemperature();
            for (ThermoView thermoView : observers) {
                Integer timeInInt = Integer.parseInt(thermoView.getTime().substring(0,timeString.length()-1)); // removing the "s"
                timeInInt++;
                timeString = String.valueOf(timeInInt)+"s"; // putting the "s" back
            }
            
            int totalCells = 0;
            double totalTemperature = 0.0;
            // Note: the system will crash if the number or rows or the number of rooms is smaller than 1, the verification was done here earlier
            // but is now already done in the constructor of this class, We can only make arrays of cell bigger than 3 or smaller than 12 as requested by the client

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (cellsArray[i][j].getType().equals(cellNormal) || cellsArray[i][j].getType().equals(cellHeated)) {
                        totalTemperature += cellsArray[i][j].getTemperature();
                        totalCells++;
                    }
                    if (cellsArray[i][j].getType().equals(cellHeated) && cellsArray[i][j].getIsHeatedCellActive()){
                        totalCostInEuros += cellsArray[i][j].getTemperature()*cellsArray[i][j].getTemperature(); // calculate the cost
                    }
                    if (cellsArray[i][j].getType().equals(cellNormal) || (cellsArray[i][j].getType().equals(cellHeated) && !cellsArray[i][j].getIsHeatedCellActive())){
                        numberOfInfluence = 9; // default influence is 9 cells, will be decreased in the getCellTemperaturesAndCountDeadCells() method if there are dead cells around
                        double calculatedTemperature = (
                            getSurroundingCellTemperatures(i,j) + 
                            getSurroundingCellTemperatures(i,j+1) +
                            getSurroundingCellTemperatures(i+1,j+1) +
                            getSurroundingCellTemperatures(i+1,j) +
                            getSurroundingCellTemperatures(i+1,j-1) +
                            getSurroundingCellTemperatures(i,j-1) +
                            getSurroundingCellTemperatures(i-1,j-1) +
                            getSurroundingCellTemperatures(i-1,j) +
                            getSurroundingCellTemperatures(i-1,j+1)
                        ) / numberOfInfluence;
                        cellsArray[i][j].setTemperature(Math.round(calculatedTemperature * 100.0) / 100.0); // round to 2 decimals
                        cellsArray[i][j].updateRedLevel();
                        view.updateCellColor(i, j, cellNormal, cellsArray[i][j].getRedLevel()); // updating the view for each cell, not using the observer pattern?
                    }
                }
            }

            previousTemperatures = copyPreviousTemperatures(cellsArray); // recopy all the previous temperatures, now all the calculations are consistent with each others

            averageInternalTemperature = Math.round((totalTemperature / totalCells)*100.0)/100.0; // round to 2 decimals
            notifyObserver();

            logEntry += timeString+";"+totalCostInEuros+";"+averageInternalTemperature+";"+externalTemperature+"\n"; // appending string for the log file
            mode.heat(); // for the heating modes, not idea, because it is being called every second.. could use the observer pattern but how?
        }
    }

    private double getSurroundingCellTemperatures(int x, int y){
        if (isOutOfBorder(x, y)){
            return externalTemperature; // If it is outside
        } else if (cellsArray[x][y].getType().equals(cellDead) || (cellsArray[x][y].getType().equals(cellHeated) && !cellsArray[x][y].getIsHeatedCellActive())){
            // it is absolutely necessary to check wether the cell is heated BEFORE checking the boolean attribute, otherwise a null value will crash the program, otherwise, set the attribute to False by default in the Cell model
            numberOfInfluence--; // if there is a dead cell, number of influence decreases by 1, Deactivated heated cell will act like a dead cell, it no longer influences other cells either
            return 0; //Dead cell, retun 0
        }
        return previousTemperatures[x][y]; // if it's another cell, return its temperature
    }

    private boolean isOutOfBorder(int x, int y){
        if (x < 0 || y < 0 || x >= cellsArray.length || y >= cellsArray[0].length){
            return true;
        }
        return false;
    }

    // Method to create a deep copy of the cellsArray
    private double[][] copyPreviousTemperatures(Cell[][] original) {
        double [][] previousTemperaturesArray = new double[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                previousTemperaturesArray[i][j] = original[i][j].getTemperature();
            }
        }
        return previousTemperaturesArray;
    }
}