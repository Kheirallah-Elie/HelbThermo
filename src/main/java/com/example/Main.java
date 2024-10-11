package com.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    GlobalSystem controller;

    public static void main(String[] args){
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new GlobalSystem(primaryStage);
    }
}