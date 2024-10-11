package com.example;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class AlertView {

    private Alert alert;
    // code found here https://stackoverflow.com/questions/65372510/javafx-alert-dialog
    public AlertView () { 
        alert = new Alert(AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText("Number of rows or number of rooms MUST BE larger than "
        +GlobalSystem.getMinimumCells()+" or smaller than "
        +GlobalSystem.getMaximumCells());
        openAlert();
    }

    public void openAlert(){
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().equals(ButtonType.OK)) {
            alert.close();
        }
    }
}
