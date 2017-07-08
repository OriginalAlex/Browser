package originalalex.com.github.display;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Main instance;
    private BorderPane bp;
    private Scene scene;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/originalalex/com/github/display/WebBrowser.fxml"));
            Scene scene = new Scene(root, 1030, 650);
            this.bp = (BorderPane) root;
            this.scene = scene;
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Alex Browser v: 0.1");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/originalalex/com/github/resources/InternetLogo.jpg")));
        primaryStage.show();
    }

    // Getters:

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public Scene getScene() { return this.scene; }

    public static Main getInstance() {
        return instance;
    }

    public BorderPane getRootOfApplication() {
        return this.bp;
    }

}