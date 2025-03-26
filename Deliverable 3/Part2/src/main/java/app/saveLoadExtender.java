package app;


import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import controllers.ComponentBase;
import controllers.ComponentsController.ImageComponent;
import controllers.ComponentsController.Drawable;
import com.opencsv.CSVWriter;

import java.util.ArrayList;
import java.util.List;

import components.testComponent;
import javafx.scene.image.Image;


public class saveLoadExtender {
    /*
    public void csvWriter(String filename, ArrayList<String[]> allComponents) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {

            for (String[] currentString:allComponents) {
                writer.writeNext(currentString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String[]> csvReader(String fileName) {
        ArrayList<String[]> reading = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                reading.add(nextLine);
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
        return reading;
    }
     */

    public void jsonWriter(String fileName, List<Drawable> input) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
        );
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            objectMapper.writeValue(new File(fileName),input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Drawable> jsonReader(String fileName) {
        ArrayList<Drawable> reading;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
        );

        try {
           reading = objectMapper.readValue(new File(fileName), new TypeReference<>() {
           });
        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reading;
    }
/*
    public ArrayList<testComponent> parsingStringToComponent (ArrayList<String[]> target) {
        for (String[] data:target) {
            data.
        }
    }
*/



}

