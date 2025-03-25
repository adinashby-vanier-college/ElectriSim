package app;


import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import com.opencsv.CSVWriter;

import java.util.ArrayList;
import components.testComponent;



public class saveLoadExtender {
    testComponent tc = new testComponent("componentName",10,10,20,20);
    testComponent t2 = new testComponent("componentName",20,20,20,20);
    ArrayList<testComponent> input = new ArrayList<>();
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

    public void jsonWriter(String fileName) {
        input.add(tc);
        input.add(t2);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
                objectMapper.writeValue(new File(fileName),input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<testComponent> jsonReader(String fileName) {
        ArrayList<testComponent> reading = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
           reading = objectMapper.readValue(new File(fileName), new TypeReference<ArrayList<testComponent>>() {});

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

