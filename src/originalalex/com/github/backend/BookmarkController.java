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

import java.util.LinkedHashMap;
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

    private static Map<String, String> textAndUrl = new LinkedHashMap<String, String>();

    @FXML
    public void add() { // called when the button is hit
        String urlText = url.getText();
        urlText = (urlText.contains("http://wwww.") || urlText.contains("http://www.")) ? urlText : "http://www." + urlText;
        String nameText = name.getText();
        if (HelperClass.getInstance().isValidURL(urlText)) {
            Controller.getInstance().addBookmark(nameText, urlText);
            status.setText("Status: SUCCESS (closing)");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((Stage) name.getScene().getWindow()).close();
            textAndUrl.put(nameText, urlText);
        } else {
            status.setText("Status: INVALID URL");
        }
    }


    public static Map<String, String> getTextAndUrl() {
        return textAndUrl;
    }
}
