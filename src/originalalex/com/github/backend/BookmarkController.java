package originalalex.com.github.backend;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import originalalex.com.github.display.Main;

import java.util.Map;

/**
 * Created by Alex on 08/07/2017.
 */
public class BookmarkController {

    @FXML
    private TextArea name;

    @FXML
    private TextArea url;

    @FXML
    private Text status;

    private Map<Text, String> textAndUrl;

    @FXML
    public void add() { // called when the button is hit
        String urlText = url.getText();
        urlText = (urlText.contains("http://wwww.") || urlText.contains("http://www.")) ? urlText : "http:/www." + urlText;
        String nameText = name.getText();
        if (HelperClass.getInstance().isValidURL(urlText)) {
            VBox bookmarks = Controller.getInstance().getBookmarks();
            Text entry = new Text(nameText);
            entry.setFont(Font.font("Arial Black", FontWeight.SEMI_BOLD, 14D)); // add style
            entry.setFill(Color.FUCHSIA);
            addListeners(entry, urlText); // add all the required listeners.
            bookmarks.getChildren().add(bookmarks.getChildren().size()-3, entry);
            status.setText("Status: SUCCESS (closing)");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((Stage) name.getScene().getWindow()).close();
        } else {
            status.setText("Status: INVALID URL");
        }
    }

    private void addListeners(Text entry, String urlText) {
        entry.setOnMouseEntered(event -> {
            Main.getInstance().getScene().setCursor(Cursor.HAND);
        });
        entry.setOnMouseExited(event -> {
            Main.getInstance().getScene().setCursor(Cursor.DEFAULT);
        });
        entry.setOnMouseClicked(event -> {
            TabPane tabs = BrowserTab.getTabs();
            Tab selected = tabs.getSelectionModel().getSelectedItem();
            Platform.runLater(() -> { // thread safety!
                BorderPane bp = (BorderPane) selected.getContent();
                WebView web = null;
                if (bp.getCenter() instanceof WebView) {
                    web = (WebView) bp.getCenter();
                } else {
                    web = new WebView();
                }
                web.getEngine().load(urlText); // Load the URL to the currently viewed tab
            });
        });
    }

}
