package com.example;

import java.util.Random;

public class CellFactory {

    private final int PROBABILITY_DEAD_HIGH = 15; // Higher probability for Dead in the middle, the lower the number, the higher the probability
    private final int PROBABILITY_DEAD_LOW = 60; // Lower probability for Dead elsewhere, the higher the number the lower the probability

    private Cell[][] cellsArray;

    private int rows;
    private int cols;
    private int cellMarkedZero = 0; // means the cell is far from a heated cell or a border, highest probability of a dead cell appearing
    private int cellMarkedOne = 1; // means the cell is close to a heated cell or to the borders, lower probability of dead cell generation

    private Boolean isDeadCellsRandomlyGenerated = true; //Set this to false if you don't want any generated dead cells

    public CellFactory (Cell[][] cellsArray, int rows, int cols){
        this.cellsArray = cellsArray;
        this.rows = rows;
        this.cols = cols;
    }

    public Cell createCell(String type){ // the factory
        return new Cell(type, GlobalSystem.getCellTemperature()); // Normal cell will get the external temperature from the controller
        // dead cells will get 0 and they will be taken out of the equation
        // the first 4-5 heated cells will have their temperature modified in the Cell class 
    }

    // the logic
    public void generateCells(){
        //cellsArray = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (isOnCorner(i,j)){
                    cellsArray[i][j] = createCell(GlobalSystem.getCellHeated());
                    GlobalSystem.getView().drawActivatedCells(i, j, cellsArray[i][j].getIdHeatedCell());

                } else {
                    cellsArray[i][j] = createCell(GlobalSystem.getCellNormal());
                }
            }
        }

        if (hasCenter()){
            cellsArray[cols/2][rows/2] = createCell(GlobalSystem.getCellHeated()); // create a heated cell in the center
            GlobalSystem.getView().drawActivatedCells(cols/2, rows/2, cellsArray[cols/2][rows/2].getIdHeatedCell());
        }
        if (isDeadCellsRandomlyGenerated){
            generateRandomDeadcells(); // once we finish generating the cells, we can now calculate the probability to generate the dead cells
        }
    }

    private boolean isOnCorner(int i, int j) { // client request, to generate heated cells on the corners
        if ((i == 0 && j == 0) || (i == rows -1 && j == cols -1) || (i == 0 && j == cols -1) || (i == rows -1 && j == 0)){
            return true;
        }
        return false;
    }

    private boolean hasCenter() { // Client request to generate a heated cell if the array has a center
        return rows % 2 != 0 && cols % 2 != 0; // if both rows and cols are uneven, it means that it has a center, so we can generate a heated cell in the center
    }

    private void generateRandomDeadcells(){ 
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cellsArray[i][j].getType().equals(GlobalSystem.getCellHeated())){
                    setAdjacentCellsToNearHeated(i, j); // methods that sets all the adjacent cells to a heated cell to 1, decreasing the probability of a deal cell later on
                }
                if (isOnBorder(i, j)){
                    // less chance of a dead cell to appear but still possible
                    // Lower probability if it is close to border as well, for this, we set the adjacent cells to the heated cells to 1
                    cellsArray[i][j].setMarkCell(cellMarkedOne); // set mark to 1
                }
            }
        }

        // set the dead cells probability, re loop in the array!
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int randomNumber = 0;
                Random rand = new Random();
                if (cellsArray[i][j].getMarkCell() == cellMarkedZero ){ // if that cell was not market as close to the border or to a heated cell
                    randomNumber = rand.nextInt(PROBABILITY_DEAD_HIGH); // higher probability for a dead cell
                } else if (cellsArray[i][j].getMarkCell() == cellMarkedOne ){ // if that cell was marked with 1
                    randomNumber = rand.nextInt(PROBABILITY_DEAD_LOW) ; // it means it has a much lower probability to be a dead cell but still possible
                }
                
                if (randomNumber == PROBABILITY_DEAD_HIGH -1 && !cellsArray[i][j].getType().equals(GlobalSystem.getCellHeated())){ // make sure it does not go over a heated cell
                    cellsArray[i][j].setType(GlobalSystem.getCellDead());
                }
            }
        }
    }

    private boolean isOnBorder(int row, int col) {
        return row == 0 || row == this.rows - 1 || col == 0 || col == this.cols - 1;
    }

    private void setAdjacentCellsToNearHeated(int rowIndex, int colIndex) {
        // Check adjacent cells 
        for (int i = rowIndex - 1; i <= rowIndex + 1; i++) {
            for (int j = colIndex - 1; j <= colIndex + 1; j++) {
                // Check if the current cell is within the bounds of the array
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    cellsArray[i][j].setMarkCell(cellMarkedOne);
                }
            }
        }
    }
}