package com.example;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyEvent;

public class CellMenuView {

    private int width = 240;
    private int height = 140;
    private TextField cellPositionText, defineDeadCellText, defineHeatedCellText, defineCellTemperatureWhenActivatedText, temperatureUserEntryText;
    private Button confirmButton, cancelButton;
    private Stage newWindow;
    private RadioButton yesNoDefineDeadCellButton, yesNoDefineHeatedCellButton;

    private static Boolean isOpen = false;

    public CellMenuView (String cellPosition){

        if (isOpen) { // if it's open, exit the constructor immediately, because the client wants only one menu open at a time
            return; 
        }

        isOpen = true;
        newWindow = new Stage(); 
        newWindow.initStyle(StageStyle.UNDECORATED);
        show(cellPosition);

        // set an exit listener, to reset the boolean isOpen to false
        newWindow.setOnCloseRequest(event -> {
            isOpen = false;
        });
    }

    public RadioButton getYesNoDefineDeadCellButton () {
        return this.yesNoDefineDeadCellButton;
    }

    public RadioButton getYesNoDefineHeatedCellButton (){
        return this.yesNoDefineHeatedCellButton;
    }

    public Button getConfirmButton(){
        return this.confirmButton;
    }

    public Button getCancelButton(){
        return this.cancelButton;
    }

    public TextField getTemperatureUserEntryText(){
        return this.temperatureUserEntryText;
    }

    public void show(String cellPosition){
        //VBox that contains all 
        VBox parent = new VBox(); 
        cellPositionText = new TextField(cellPosition);
        cellPositionText.setEditable(false);
        cellPositionText.setAlignment(Pos.CENTER);

        HBox deadCellBox = new HBox();
        defineDeadCellText = new TextField("Define as a dead cell");
        defineDeadCellText.setEditable(false);
        defineDeadCellText.setAlignment(Pos.CENTER);

        yesNoDefineDeadCellButton = new RadioButton("Check"); 

        deadCellBox.getChildren().addAll(defineDeadCellText, yesNoDefineDeadCellButton);

        HBox heatedCellBox = new HBox();
        defineHeatedCellText = new TextField("Define as a heated cell");
        defineHeatedCellText.setEditable(false);
        defineHeatedCellText.setAlignment(Pos.CENTER);

        yesNoDefineHeatedCellButton = new RadioButton("Check"); 

        heatedCellBox.getChildren().addAll(defineHeatedCellText, yesNoDefineHeatedCellButton);

        HBox setTemperatureBox = new HBox();
        defineCellTemperatureWhenActivatedText = new TextField("Set cell temp");
        defineCellTemperatureWhenActivatedText.setEditable(false);
        defineCellTemperatureWhenActivatedText.setAlignment(Pos.CENTER);

        temperatureUserEntryText = new TextField("20");
        temperatureUserEntryText.setDisable(true);
        temperatureUserEntryText.setAlignment(Pos.CENTER);
        temperatureUserEntryText.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });

        setTemperatureBox.getChildren().addAll(defineCellTemperatureWhenActivatedText, temperatureUserEntryText);

        HBox confirmCancelBox = new HBox();
        confirmButton = new Button("Confirm");
        cancelButton = new Button("Cancel");

        confirmCancelBox.getChildren().addAll(confirmButton,cancelButton);

        parent.getChildren().addAll(cellPositionText, deadCellBox, heatedCellBox, setTemperatureBox, confirmCancelBox);

        newWindow.setTitle("Menu");
        
        newWindow.setScene(new Scene(parent, width, height));
        newWindow.show();
    }

    public void close(){ 
        isOpen = false;
        newWindow.close();
    }

    public void changeButtonStatus(RadioButton clickedButton, RadioButton otherButton){
        clickedButton.setSelected(true);
        otherButton.setSelected(false);   
    }

    public void disableTemperatureUserEntry(Boolean status){ // enable only when heated cell is selected
        temperatureUserEntryText.setDisable(status);
    }
}
