package originalalex.com.github.backend;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import originalalex.com.github.display.Main;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class Controller {

    @FXML
    private TabPane tabs;

    @FXML
    private VBox bookmarks;

    private static Controller instance;
    private Tab addTab;

    @FXML
    public void initialize() {
        instance = this;
        BrowserTab.setTabs(tabs);
        createAddNewTabButton();
        createNewTab();
        createAddBookmarkButton();
        createSaveBookmarksButton();
        createLoadbookmarksButton();
    }

    private void createAddBookmarkButton() {
        Button add = new Button("Add Bookmark");
        add.setOnAction(event -> {
            Stage popup = new Stage();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/originalalex/com/github/display/AddBookmark.fxml"));
                popup.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
            popup.getIcons().add(new Image(getClass().getResourceAsStream("/originalalex/com/github/resources/InternetLogo.jpg")));
            popup.setResizable(false);
            popup.show();
        });
        bookmarks.getChildren().add(add);
    }

    private String getNewFileName(File selectedDirectory) {
        File[] files = selectedDirectory.listFiles();
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName(); // Create an array of names to create the new file (do not want overlap)
        }
        String filename;
        Arrays.sort(names);
        if (Arrays.binarySearch(names, "bookmark.txt") < 0) {
            filename = "bookmark";
        } else {
            int i = 1;
            while (Arrays.binarySearch(names, "bookmark" + i + ".txt") > 0) {
                System.out.println(Arrays.binarySearch(names, "bookmarks" + i + ".txt"));
                i++;
            }
            filename = "bookmark" + i;
        }
        return filename;
    }

    private void createSaveBookmarksButton() {
        Button saveButton = new Button("Save Bookmarks");
        saveButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDirectory = chooser.showDialog(Main.getInstance().getPrimaryStage());
            if (selectedDirectory != null) {
                String filename = getNewFileName(selectedDirectory);
                BufferedWriter output = null;
                try {
                    System.out.println((selectedDirectory.getAbsolutePath() + "\\" + filename + ".txt").replaceAll("\\\\", "/"));
                    File file = new File((selectedDirectory.getAbsolutePath() + "\\" + filename + ".txt").replaceAll("\\\\", "/")); // it requires 3 escape sequences for some reason lol
                    output = new BufferedWriter(new FileWriter(file));
                    Map<String, String> textAndUrl = BookmarkController.getTextAndUrl();
                    for (Map.Entry<String, String> ent : textAndUrl.entrySet()) {
                        output.write(ent.getKey() + "???" + ent.getValue() + System.lineSeparator()); // ??? is the seperator to distinguish where the name ends and the URL starts
                    }
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bookmarks.getChildren().add(saveButton);
    }

    private void createLoadbookmarksButton() {
        Button loadButton = new Button("Load Bookmarks");
        loadButton.setOnAction(event -> {
            FileChooser selector = new FileChooser();
            selector.setTitle("Choose Bookmark File");
            selector.setInitialDirectory(new File(System.getProperty("user.home"))); // Set to "home" of the CPU by default
            selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
            File chosenFile = selector.showOpenDialog(Main.getInstance().getPrimaryStage());
            if (chosenFile != null) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(chosenFile));
                    String line;
                    ObservableList<Node> children = bookmarks.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        Node n = children.get(i);
                        if (!(n instanceof Region)) { // Region seperates the critical components from the unimportant ones
                            children.remove(n);
                        } else {
                            break;
                        }
                    }
                    while ((line = reader.readLine()) != null) {
                        System.out.println("yo");
                        String[] parts = line.split("\\?\\?\\?"); // escape
                        System.out.println(parts[0]);
                        if (parts.length == 2) {
                            addBookmark(parts[0], parts[1]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bookmarks.getChildren().add(loadButton);
    }

    public void createNewTab() {
        try {
            BorderPane bp = (BorderPane) FXMLLoader.load(getClass().getResource("/originalalex/com/github/display/DefaultTab.fxml")); // Get all the information for a new tab and create it!:
            HBox hbox = (HBox) bp.getTop();
            ImageView forwardArrow = (ImageView) hbox.getChildren().get(2), backArrow = (ImageView) hbox.getChildren().get(0);
            TextArea urlBar = (TextArea) hbox.getChildren().get(4);
            WebView browser = new WebView();
            bp.setCenter(browser);
            new BrowserTab(bp, urlBar, forwardArrow, backArrow, browser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAddNewTabButton() {
        Tab add = new Tab(" + ");
        addTab = add;
        addTab.setClosable(false);
        this.addTab = add;
        tabs.getTabs().add(add);
    }

    public void addBookmark(String nameText, String urlText) {
        VBox bookmarks = Controller.getInstance().getBookmarks();
        Text entry = new Text(nameText);
        entry.setFont(Font.font("Arial Black", FontWeight.SEMI_BOLD, 14D)); // add style
        entry.setFill(Color.FUCHSIA);
        addListeners(entry, urlText); // add all the required listeners.
        bookmarks.getChildren().add(bookmarks.getChildren().size() - 4, entry);
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

    public Tab getAddTab() {
        return this.addTab;
    }

    public VBox getBookmarks() {
        return this.bookmarks;
    }

    public static Controller getInstance() {
        return instance;
    }

}