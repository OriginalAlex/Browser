package originalalex.com.github.backend;

import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import originalalex.com.github.display.Main;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

public class BrowserTab {



    /**
     * Created by Alex on 07/07/2017.
     */

    private ImageView forwardArrow;
    private ImageView backArrow;
    private WebView browser;
    private TextArea urlBar;

    private HelperClass hc;
    private String resourcesPath;
    private WebEngine webEngine;
    private LinkedList<String> pagesBack = new LinkedList<String>(); // Using linked list over array because I'm only adding to the tail, and only peeking the taiil (ie. a Queue)
    private Stack<String> pagesForward = new Stack<String>();
    private volatile boolean shouldBeAdded = false;
    private String searchTerm;
    private final HelperClass helperClass = HelperClass.getInstance();
    private final Main main = Main.getInstance();
    private final Controller controller = Controller.getInstance();

    private Tab thisTabInstance;
    private static TabPane tabs;

    public BrowserTab(BorderPane original, TextArea urlBar, ImageView forwardArrow,
                      ImageView backArrow, WebView browser) {
        Tab newTab = new Tab();
        newTab.setContent(original);
        this.thisTabInstance = newTab;
        tabs.getTabs().add(newTab);
        tabs.getTabs().remove(controller.getAddTab());
        tabs.getTabs().add(tabs.getTabs().size(), controller.getAddTab());
        this.urlBar = urlBar;
        this.forwardArrow = forwardArrow;
        this.backArrow = backArrow;
        this.browser = browser;
        newTab.setText("Google");
        initialize();
    }

    public static void setTabs(TabPane tp) {
        tabs = tp;
    }

    private void initialize() {
        resourcesPath = "/originalalex/com/github/resources/";
        helperClass.changeForwardArrow(false, forwardArrow, resourcesPath);

        webEngine = browser.getEngine();
        shouldBeAdded = false;
        updateWebPage("http://www.google.com/", true);
        helperClass.changeBackArrow(false, backArrow, resourcesPath);
        addListeners();
    }

    private void addListeners() {
        urlBar.setOnKeyPressed(e -> { // Fired whenever they press a key
            if (e.getCode().equals(KeyCode.ENTER)) { // They pressed the enter key (indicating they want to search for something
                String input = urlBar.getText().trim();
                e.consume();
                if (input.trim().equals("")) {
                    return;
                }
                String potentialUrl = (input.contains("http://www.") || input.contains("https://www.")) ? input : "http://www." + input;
                if (helperClass.isValidURL(potentialUrl)) {  // It was able to validate the URL
                    updateWebPage(potentialUrl, true);
                    forwardArrow.requestFocus(); // Remove the focus from the URL bar (just to make it look cleaner upon loading a page)
                    this.urlBar.setText(potentialUrl.trim());
                } else {
                    searchTermEntered(input, true);
                }
            }
        });

        tabs.getSelectionModel().selectedItemProperty().addListener((x, y, z) -> { // This event if fired when a TabPane is closed/changed (z is the new tab, y is the old one)
            if (tabs.getTabs().size() == 1) { // We consider it empty if it has 1 because of the new tab button
                controller.createNewTab();
            }
            else if (z.equals(controller.getAddTab())) {
                controller.createNewTab();
                tabs.getSelectionModel().select(tabs.getTabs().size()-2);
            }
        });
        initializeArrows();
        initializeWebEngine();
    }

    private void initializeArrows() {
        this.forwardArrow.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !pagesForward.isEmpty()) { // If they left clicked
                String url = pagesForward.pop(); // Remove the first element forward
                if (helperClass.isValidURL(url)) { // Last page was a URL
                    if (searchTerm == null) {
                        updateWebPage(url, false);
                    } else {
                        BorderPane bp = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
                        recreateWebView(bp);
                        updateWebPage(url, false);
                    }
                }
                else {
                    if (searchTerm == null) {
                        searchTermEntered(url, false);
                    } else {
                        searchTermEntered(url, false);
                    }
                    if (pagesForward.isEmpty()) {
                        helperClass.changeForwardArrow(false, forwardArrow, resourcesPath);
                    }
                }
                this.urlBar.setText(url);
            }
        });

        this.backArrow.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !pagesBack.isEmpty()) {
                String temp;
                temp = (searchTerm == null) ? webEngine.getLocation() : searchTerm;
                pagesForward.push(temp);
                String url = pagesBack.pop(); // Remove the last element on the back
                if (searchTerm == null) { // If the webview is loaded and it is displaying the webview
                    if (helperClass.isValidURL(url)) {
                        shouldBeAdded = false;
                        updateWebPage(url, false);
                    } else {
                        searchTermEntered(url, false);
                    }
                } else {
                    if (helperClass.isValidURL(url)) { // The webview is NOT displayed so we need to make it so
                        shouldBeAdded = false; // No need to add it to the list of back pages, because else a loop will happen!
                        BorderPane bp = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
                        recreateWebView(bp);
                        updateWebPage(url, false);
                    } else {
                        searchTermEntered(url, false);
                    }
                }
                helperClass.changeForwardArrow(true, forwardArrow, resourcesPath); // guranteed page forward now
                if (pagesBack.isEmpty()) {
                    helperClass.changeBackArrow(false, backArrow, resourcesPath);
                }
                this.urlBar.setText(temp);
            }
        });
    }

    private void searchTermEntered(String term, boolean addPageToBackList) {
        if (searchTerm == null && addPageToBackList) {
            ObservableList<WebHistory.Entry> history = webEngine.getHistory().getEntries();
            pagesBack.push(history.get(history.size()-1).getUrl());
        } else if (addPageToBackList) {
            pagesBack.push(searchTerm);
        }
        pagesForward.clear();
        searchTerm = term;
        helperClass.changeForwardArrow(false, forwardArrow, resourcesPath);
        helperClass.changeBackArrow(true, backArrow, resourcesPath);
        Set<SiteInfo> websites = helperClass.amalgamate(term.replaceAll(" ", "+"));
        ScrollPane container = new ScrollPane();
        VBox holder = new VBox();
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        BorderPane bp = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
        Text showingResults = new Text("Showing Results For: " + term);
        showingResults.setFont(Font.font("Bell MT", FontWeight.EXTRA_BOLD, 21));
        Region seperation = new Region();
        seperation.setPrefHeight(20);
        holder.getChildren().addAll(showingResults);
        selected.setText(term);
        for (SiteInfo site : websites) { // loop through all websites in set
            Text name = new Text(site.getTitle());
            name.setOnMouseEntered(event -> {
                main.getScene().setCursor(javafx.scene.Cursor.HAND);
            });
            name.setOnMouseExited(event -> {
                main.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            });
            name.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    recreateWebView(bp);
                    updateWebPage(site.getUrl(), true);
                }
            });
            Region urlAndDescSeperator = new Region();
            urlAndDescSeperator.setPrefSize(5, 5);
            Region siteSeperator = new Region();
            siteSeperator.setPrefSize(5, 35);
            name.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            Text description = new Text(site.getDescription());
            description.setFont(Font.font("Arial", 13));
            name.setWrappingWidth(500);
            name.setFill(Color.BLUE);
            description.setFill(Color.GRAY);
            description.setWrappingWidth(800);
            holder.getChildren().addAll(name, urlAndDescSeperator, description, siteSeperator);
        }
        holder.setPadding(new javafx.geometry.Insets(15, 15, 5, 20));
        container.setContent(holder);
        bp.setCenter(container);
    }

    private void recreateWebView(BorderPane bp) {
        browser = new WebView();
        webEngine = browser.getEngine();
        initializeWebEngine();
        bp.setCenter(browser);
    }

    private void initializeWebEngine() {
        webEngine.getLoadWorker().stateProperty().addListener((x, y, z) -> { // Wait until the Web Engine has finished loading the page (z is the state of the operation) [Also called whenever the webview changes pages]
            if (z == Worker.State.SUCCEEDED) {
                System.out.println(shouldBeAdded);
                if (shouldBeAdded) {
                    ObservableList<WebHistory.Entry> history = webEngine.getHistory().getEntries();
                    String lastPage;
                    if (searchTerm != null) { // Default entries are loaded
                        lastPage = searchTerm;
                    } else {
                        lastPage = history.get(history.size()-2).getUrl();
                    }
                    if (pagesBack.isEmpty() || !pagesBack.peek().equals(lastPage)) { // In case they reload the page
                        pagesBack.push(lastPage); // add the last page to the back list
                    }
                    helperClass.changeBackArrow(true, backArrow, resourcesPath);
                }
                thisTabInstance.setText(webEngine.getTitle());
                shouldBeAdded = true;
                searchTerm = null; // This will be used as a test for whether it is displaying a URL or not
            }
        });
    }

    private void updateWebPage(String newUrl, boolean clearForward) {
        webEngine.load(newUrl);
        helperClass.changeForwardArrow(false, forwardArrow, resourcesPath);
        if (clearForward) {
            pagesForward.clear();
        }
    }

    public static TabPane getTabs() {
        return tabs;
    }
}

