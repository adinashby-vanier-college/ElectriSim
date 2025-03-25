package app;

import components.testComponent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class  Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/start_menu.fxml"));
            Parent root = loader.load();

            // screen dimensions for scene width and height
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();

            // new scene
            Scene scene = new Scene(root, screenWidth, screenHeight);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

            // changing stage properties
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        saveLoadExtender sl = new saveLoadExtender();
        String filename = "src/main/resources/csv/data.csv";
        String filename2 = "src/main/resources/json/data.json";
        ArrayList<String[]> allComponents = new ArrayList<>();
        String[] component1 = {"nameOfComponents, 10,10,10"};
        String[] component2 = {"nameOfComponents, 20,10,10"};
        allComponents.add(component1);
        allComponents.add(component2);
        sl.csvWriter(filename, allComponents);
        ArrayList<String[]> currentData = sl.csvReader(filename);
        for (String[] data : currentData) {
            System.out.println(Arrays.toString(data));
        }
        sl.jsonWriter(filename2);
        ArrayList<testComponent> result = sl.jsonReader(filename2);
        for (testComponent data : result) {
            System.out.println(data.toString());
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
