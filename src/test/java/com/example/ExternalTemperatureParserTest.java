package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExternalTemperatureParserTest {

    @Test
    public void testNextTemperatureInRange() {
        ExternalTemperatureParser parser = new ExternalTemperatureParser("simul.data");
        Integer temperature = parser.getNextTemperatureFromLineString("45"); // test not supposed to work
        Integer temperature2 = parser.getNextTemperatureFromLineString("-45"); // not supposed to work
        Integer temperature3 = parser.getNextTemperatureFromLineString("30"); // supposed to work

        assertTrue(temperature >= 0 && temperature <= 40);
        assertTrue(temperature2 >= 0 && temperature2 <= 40);
        assertTrue(temperature3 >= 0 && temperature3 <= 40);

    }
}