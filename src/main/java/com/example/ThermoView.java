package com.example;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class ThermoView implements IThermoObserver {

    private int width = 1000;
    private int height = 750;
    private int rows;
    private int cols;
    private int resizedButtons = 60;
    private int heatedCellCounter;
    private int redLevel = 255; //the lower this value is, the higher the red will be, the logic is calculate in the Cell model

    // Set UI items
    private Button buttonStart, buttonPause, buttonReset;
    private TextField textFieldTime, textFieldCost, textFieldExternalTemperature, textFieldAverageTemperature;
    private ComboBox<String> heatingMode;
    private List<String> heatingModes;
    private List<Button> leftHeatedCellsButtonsList;

    private GridPane rightGrid, leftGrid;

    //Set initial texts
    private String time = "0s";
    private String cost = "0€";
    private String externalTemperature = "Extrnl T: ";
    private String averageTemperature = "Avg T: ";
    private String filePath = "file:src/Icons/";
    private String startButtonImagePath = "play.png";
    private String pauseButtonImagePath = "pause.png";
    private String resetButtonImagePath = "refresh.png";

    private Button cellButton;
    private Button [][] cellButtonArray;

    //private Color greyLevelColor = Color.GREY; // used below for inactivated heated cells

    private BackgroundFill inactiveCellColor = new BackgroundFill(Color.GREY, null,null);
    private BackgroundFill boardColor = new BackgroundFill(Color.rgb(200, 215, 220), null, null); // Light blue color for fun
    private BackgroundFill deadCellColor = new BackgroundFill(Color.rgb(45, 45, 45), null, null); // Smoother black, Color.Black for full black
    private BackgroundFill cellTemperatureColor = new BackgroundFill(Color.rgb(255,redLevel,redLevel), null, null); // red level will lower G and B to make the cell more red when it's hotter

    private Background greyBackground = new Background(inactiveCellColor);
    private Background blackBackground = new Background(deadCellColor);
    private Background cellColor = new Background(cellTemperatureColor);
    private Background boardBackground = new Background(boardColor);
    
    // Constructor takes the number of cells, rows and rooms to study from the controller
    public ThermoView (Stage primaryStage, int numberOfRows, int numberOfRooms) {

        this.rows = numberOfRows;
        this.cols = numberOfRooms;

        cellButtonArray = new Button[numberOfRows][numberOfRooms];
        leftHeatedCellsButtonsList = new ArrayList<>();

        VBox parent = new VBox();// Vertical box that contains everything
        parent.setBackground(boardBackground);
        HBox informationBox = new HBox();// Horizontal box for the inforamtions: Temps, cout, temperature exterieur et moyenne, et chauffe mode (combo box)
        informationBox.setAlignment(Pos.TOP_CENTER);
        // Add text areas for temperature, cost, external temperature, average temperature and the Heating Mode Combo Box
        textFieldTime = new TextField(time);
        addToInformationBox(informationBox, textFieldTime); // method created to avoid code duplication
    
        textFieldCost = new TextField(cost);
        addToInformationBox(informationBox, textFieldCost);
    
        textFieldExternalTemperature = new TextField(externalTemperature + GlobalSystem.getExternalTemperature() + "*C");
        addToInformationBox(informationBox, textFieldExternalTemperature);
    
        textFieldAverageTemperature = new TextField(averageTemperature + GlobalSystem.getAverageInternalTemperature()+ "*C");
        addToInformationBox(informationBox, textFieldAverageTemperature);

        heatingMode = new ComboBox<>();
        heatingMode.setPromptText("Heating Mode");

        heatingModes = new ArrayList<>();
        heatingModes.add(GlobalSystem.getModeManual());
        heatingModes.add(GlobalSystem.getModeTarget());
        
        heatingMode.getItems().addAll(heatingModes);
        informationBox.getChildren().add(heatingMode); 
        // ****************************************************************************************************************//

        HBox buttonBox = new HBox(); // Horizontal box for the 3 buttons
        buttonBox.setAlignment(Pos.TOP_LEFT);
    
        // ******************************************************* //
        //Add buttons for start, pause, and reset to the buttonBox with images
        ImageView imageViewStart = createButtonImageView(startButtonImagePath); // Method created below to downsize the images respectfully and avoid code duplication
        ImageView imageViewPause = createButtonImageView(pauseButtonImagePath);
        ImageView imageViewRestart = createButtonImageView(resetButtonImagePath);

        buttonStart = new Button();
        buttonStart.setGraphic(imageViewStart);
        
        buttonPause = new Button();
        buttonPause.setGraphic(imageViewPause);
        
        buttonReset = new Button();
        buttonReset.setGraphic(imageViewRestart);

        buttonBox.getChildren().addAll(buttonStart, buttonPause, buttonReset);
        // ****************************************************** //

        // Below grids that contain the cells
        HBox leftAndRightGridsBox = new HBox(); // THose are horizontally scaled
        leftAndRightGridsBox.setAlignment(Pos.TOP_CENTER);
        leftAndRightGridsBox.setSpacing(20);

        ScrollPane leftScrollableGrid = new ScrollPane(); // making the left grid scrollable individually
        leftScrollableGrid.setFitToHeight(true);

        leftGrid = new GridPane(); // that grid will contain the elements (S1, S2, S3, etc....)
        leftGrid.setVgap(10);

        leftScrollableGrid.setContent(leftGrid); // only left grid is scrollable

        rightGrid = new GridPane(); // This grid will contain all the cells and will be generated dynamically
        rightGrid.setAlignment(Pos.CENTER);
        rightGrid.setHgap(10); // horizontal gap
        rightGrid.setVgap(10); // vertical gap

        // CSS styling to contour the right Grid
        rightGrid.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 5px;");

        leftAndRightGridsBox.getChildren().addAll(leftScrollableGrid,rightGrid);

        // Add all the previous horizontal boxes to the parent vertical box: parent
        parent.getChildren().addAll(informationBox, buttonBox, leftAndRightGridsBox);

        primaryStage.setTitle("HELB Thermo - Kheirallah Elie");
        primaryStage.setScene(new Scene(parent, width, height)); 
        primaryStage.show();
    }

    //Getters from the UI
    public Button getButtonStart() {
        return buttonStart;
    }
    public Button getButtonPause() {
        return buttonPause;
    }
    public Button getButtonReset() {
        return buttonReset;
    }
    public ComboBox<String> getHeatingMode(){
        return heatingMode;
    }
    public String getTime(){
        return this.textFieldTime.getText(); // Gets the current time in the Text Field (format 25s for 25 seconds, the 's' will be handled in the ThermoModel)
    }
    public Button[][] getCellButtonArray(){
        return this.cellButtonArray;
    }

    @Override // called every second the timeline is running, notified by the observable "GlobalSystem"
    public void update(){
        textFieldTime.setText(GlobalSystem.getTimeString());
        textFieldCost.setText(GlobalSystem.getTotalCostInEuros() + "€");
        textFieldExternalTemperature.setText(externalTemperature + GlobalSystem.getExternalTemperature() + "*C");
        textFieldAverageTemperature.setText(averageTemperature + GlobalSystem.getAverageInternalTemperature()+ "*C");
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (GlobalSystem.getCellsArray()[i][j].getType().equals(GlobalSystem.getCellHeated())){
                    cellButtonArray[i][j].setText("S:"+GlobalSystem.getCellsArray()[i][j].getIdHeatedCell()+"\n"+GlobalSystem.getCellsArray()[i][j].getTemperature()); //Put the S and number for heated cells
                    updateCellColor(i, j, GlobalSystem.getCellsArray()[i][j].getType(), GlobalSystem.getCellsArray()[i][j].getRedLevel());
                } else{
                    if (!GlobalSystem.getCellsArray()[i][j].getType().equals(GlobalSystem.getCellDead())){ // don't draw temp for dead cells
                        cellButtonArray[i][j].setText(""+GlobalSystem.getCellsArray()[i][j].getTemperature());
                    } else {
                        cellButtonArray[i][j].setText("");
                    }
                }
            }
        }
    }

    // Methods created to avoid code duplication 
    public void addToInformationBox(HBox horizontalBox, TextField textField) {
        textField.setAlignment(Pos.CENTER);
        textField.setEditable(false); // prevent the user from modifying the text
        horizontalBox.getChildren().add(textField);
    }

    public void drawGridCells(Cell[][] generatedCells){
        //draw all the cells in the right grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cellButton = new Button();
                if (generatedCells[i][j].getType().equals(GlobalSystem.getCellDead())) {
                    cellButton.setBackground(blackBackground);
                }
                cellButton.setPrefSize(resizedButtons, resizedButtons); // resize buttons
                rightGrid.add(cellButton, j, i);
                cellButtonArray[i][j] = cellButton;
                if (!GlobalSystem.getCellsArray()[i][j].getType().equals(GlobalSystem.getCellDead())){ // don't draw temp for dead cells
                    cellButtonArray[i][j].setText(""+GlobalSystem.getCellsArray()[i][j].getTemperature());
                }
            }
        } 
    }

    public void drawActivatedCells(int row, int col, int heatedCellId){
        //Draw activated cells on left grid
        Button leftHeatedCellsButton = new Button("S"+heatedCellId);
        leftHeatedCellsButton.setBackground(greyBackground); 
        leftGrid.add(leftHeatedCellsButton, 0,heatedCellCounter++); // One on each line of the grid
        leftHeatedCellsButton.setPrefSize(resizedButtons, resizedButtons); // resize buttons
        leftHeatedCellsButton.setOnAction(e -> { // set their event handler and link their color with the cells on the right grid
            GlobalSystem.activateCell(row, col);
            leftHeatedCellsButton.setBackground(cellButtonArray[row][col].getBackground()); // get the button color, but only works in this handler, will stay put if using target mode
        }); 
        leftHeatedCellsButtonsList.add(leftHeatedCellsButton);
    }

    public void updateCellColor(int row, int col, String type, int redLevel) {
        cellTemperatureColor = new BackgroundFill(Color.rgb(255,redLevel,redLevel), null, null);
        cellColor = new Background(cellTemperatureColor);

        if (type.equals(GlobalSystem.getCellDead())) {
            cellButtonArray[row][col].setBackground(blackBackground);
        } else if (type.equals(GlobalSystem.getCellHeated()) && GlobalSystem.getCellsArray()[row][col].getIsHeatedCellActive()) {
            cellButtonArray[row][col].setBackground(cellColor);
        } else if (type.equals(GlobalSystem.getCellHeated()) && !GlobalSystem.getCellsArray()[row][col].getIsHeatedCellActive()){
            cellButtonArray[row][col].setBackground(greyBackground); // set color to grey for deactivated cell
        }else {
            cellButtonArray[row][col].setBackground(cellColor);
        }
    }

    //Downsizing images method to ImageView
    public ImageView createButtonImageView(String imagePath) {
        ImageView imageView = new ImageView(new Image(filePath + ""+ imagePath));
        imageView.setFitWidth(35);
        imageView.setFitHeight(35);
        return imageView;
    }

    public void deactivateLeftButtons(Boolean state){
        for (Button button : leftHeatedCellsButtonsList){
            button.setDisable(state);
            if (state == true){ // can be optimised further
                button.setBackground(greyBackground);
            }
        }
    }

    public void disableView(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // prevents the user from exiting the first stage before exiting the second one
        });
        updateViewElements(primaryStage, true);
    }
    
    public void enableView(Stage primaryStage) {
        primaryStage.setOnCloseRequest(null); // to enable the exit back to the user
        updateViewElements(primaryStage, false);
    }
    
    private void updateViewElements(Stage primaryStage, boolean state) {
        buttonStart.setDisable(state);
        buttonPause.setDisable(state);
        buttonReset.setDisable(state);
        heatingMode.setDisable(state);
    
        for (Button[] row : cellButtonArray) {
            for (Button col : row) {
                col.setDisable(state);
            }
        }
        deactivateLeftButtons(state);
    }    
}