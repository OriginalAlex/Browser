package originalalex.com.github.backend;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

import java.io.IOException;

public class Controller {

    @FXML
    private TabPane tabs;

    private static Controller instance;
    private Tab addTab;

    @FXML
    public void initialize() {
        instance = this;
        tabs.getTabs().clear();
        BrowserTab.setTabs(tabs);
        createAddNewTabButton();
        createNewTab();
    }

    public void createNewTab() {
        try {
            BorderPane bp = (BorderPane) FXMLLoader.load(getClass().getResource("/originalalex/com/github/display/DefaultTab.fxml")); // Get all the information for a new tab and create it!:
            HBox hbox = (HBox) bp.getTop();
            ImageView forwardArrow = (ImageView) hbox.getChildren().get(2), backArrow = (ImageView) hbox.getChildren().get(0);
            TextArea urlBar = (TextArea) hbox.getChildren().get(4);
            WebView browser = new WebView();
            ((StackPane) bp.getCenter()).getChildren().add(browser);
            new BrowserTab(bp, urlBar, forwardArrow, backArrow, browser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Tab getAddTab() {
        return this.addTab;
    }

    private void createAddNewTabButton() {
        Tab add = new Tab(" + ");
        addTab = add;
        addTab.setClosable(false);
        this.addTab = add;
        tabs.getTabs().add(add);
    }

    public static Controller getInstance() {
        return instance;
    }

}