package com.example;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataSaver {
    
    String data;

    public DataSaver (String data){
        this.data = data;
    }

    public void saveData(){
        try {
            Date currentDate = new Date();
            // source: https://www.digitalocean.com/community/tutorials/java-simpledateformat-java-date-format
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy");
            String formattedDate = simpleDateFormat.format(currentDate);
    
            // Format for time in HHMMSS format
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HHmmss");
            String formattedTime = simpleTimeFormat.format(currentDate);
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(formattedDate+"_"+formattedTime+".log"));
            writer.write(data);
            writer.close();
        } catch (IOException ioe) {
            System.out.println("Couldn't write to file");
        }
    }
}
