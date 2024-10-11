package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


// This code was taken from Mr Riggio from the Cinema exercice and adapted to this project, some of his comments were left

/**
 * Classe centralisant les fonctionnalités permettant le parsing 
 * du fichier .data contenant les requetes de reservation. 
 * Les lignes du fichiers .data étant construites de la manière suivante  : John;Smith;jsmith@mail.com;A;5
 * Le parser agit également comme un conteneur de requetes de reservations, pouvant être itérées à la suite. 
 */
public class ExternalTemperatureParser {
    
    //private ArrayList<ReservationRequest> reservationRequestList = new ArrayList<ReservationRequest>();
    private ArrayList<Integer> temperatureList = new ArrayList<>();
    private int currentIndex = 0;
    private int maxIndex = 0;
    private int previousTemperature = 0;
    
    public ExternalTemperatureParser(String filename){
        parse(filename);
        maxIndex = temperatureList.size();
    }

    public ArrayList<Integer> getTemperatureList(){
        return this.temperatureList;
    }

    
    //true si une nouvelle request est disponible
    public boolean hasNextTemperature(){
        return (currentIndex < maxIndex);
    }
    
    //renvoie une nouvelle request si celle ci est disponible sinon renvoie une exception
    public Integer getNextTemperature(){
        if (hasNextTemperature()){
            return temperatureList.get(currentIndex++);
        } else {
            currentIndex = 0; // rewind to the beginning of the list
            return temperatureList.get(currentIndex++);
        }
    }
    
    public Integer getNextTemperatureFromLineString(String line) {
        String[] parts = line.split("\n");
        Integer numberRequest = 0;
        String temperature = parts[0];
        if (isNumeric(temperature)){ // checks if the text is a valid number before parsing
            if (Integer.parseInt(temperature) >= 0 && Integer.parseInt(temperature) <= 40){
                numberRequest = Integer.parseInt(temperature);
                previousTemperature = numberRequest;
            } else {
                numberRequest = previousTemperature; 
            }
        } 
        return numberRequest;
    }

    // this code snippet was found here https://www.baeldung.com/java-check-string-number
    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    /*  
        Parse le fichier filename et pour chaque ligne du fichier, 
        verifie si la ligne est correcte 
        et si celle ci est correcte, 
        ajoute dans la liste une ReservationRequest construite à partir de cette ligne.
    */ 
    private void parse(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(isNumeric(line)){
                    int request = getNextTemperatureFromLineString(line);
                    temperatureList.add(request);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
