package originalalex.com.github.backend;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HelperClass {

    private static HelperClass instance = new HelperClass();
    private static final String GOOGLE_URL = "https://www.google.co.uk/search?q="; // After the q= goes the search term (spaces replaced by +)
    private static final String BING_URL = "https://www.bing.com/search?q="; // Same as above

    private List<SiteInfo> googleSites;
    private List<SiteInfo> bingSites; // Both are global variables so that other threads are able to access and modify them.

    /*
    How the amalgamation works:
    for google:
                All the search entires are under a div, with class name 'g'
                Then the URL is under a div class called "f kv +sWb"
                A description is under a span class called "st"
     for bing:
                All the search entires under an li, with class name "b_algo"
                Then the title is inside an a href
                And the URL/Desc are contained inside a div with class name "b_attribution"
                URL: is contained in a site; Description: is contained in a <p>
     */

    public enum SearchEngine {
        GOOGLE, BING;
    }

    private List<SiteInfo> getSeacrchResults(SearchEngine se, String keyword, String cssQuerySite, String cssQueryUrl, String cssQueryDescription, String cssQueryTitle) {
        List<SiteInfo> websites = new ArrayList<SiteInfo>();
        try {
            Document doc = null;
            switch (se) {
                case GOOGLE: doc = Jsoup.connect(GOOGLE_URL + keyword).userAgent("Mozilla/5.0").get(); break;
                case BING: doc = Jsoup.connect(BING_URL + keyword).userAgent("Mozilla/5.0").get(); break;
            }
            Elements sites = doc.select(cssQuerySite);
            for (Element e : sites) { // go through each site
                Element url = e.select(cssQueryUrl).first();
                if (url == null) {
                    continue;
                }
                String recalculatedUrl = url.text();
                recalculatedUrl = (recalculatedUrl.contains("http")) ? url.text() : "http://www." + url.text(); // Edge case
                Element desc = e.select(cssQueryDescription).first();
                Element title = e.select(cssQueryTitle).first();
                if (isValidURL(recalculatedUrl)) {
                    try {
                        SiteInfo si = new SiteInfo(recalculatedUrl, desc.text(), title.text());
                        websites.add(si);
                    } catch (NullPointerException ex) {
                        continue;
                    }
                }

            }
        } catch (IOException io) {
            io.printStackTrace();

        }
        return websites;
    }

    /*
    All the search entires under an li, with class name "b_algo"
                Then the title is inside an a href
                And the URL/Desc are contained inside a div with class name "b_attribution"
                URL: is contained in a site; Description: is contained in a <p>
     */

    public Set<SiteInfo> amalgamate(final String keyword) { // Final for thread modification
        long start = System.currentTimeMillis();
        googleSites = new ArrayList<SiteInfo>();
        bingSites = new ArrayList<SiteInfo>(); // Must be reset after each usage.
        Thread getGoogle = new Thread(() -> {
            googleSites = getSeacrchResults(SearchEngine.GOOGLE, keyword, "div.g", "cite", "span.st", "a[href]");
        });
        Thread getBing = new Thread(() -> {
           bingSites =  getSeacrchResults(SearchEngine.BING, keyword, "li.b_algo", "cite", "p", "a[href]");
        });
        getGoogle.start();
        getBing.start();
        try {
            getGoogle.join();
            getBing.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Took " + (System.currentTimeMillis()-start) + "ms"); // Took 1800ms on average with thread method
        Set<SiteInfo> sites = new LinkedHashSet<SiteInfo>(); // Linked because insertion order must be kept (relevant pages at top), and set to avoid two pages coming up at once.
        for (int i = 0; i < 11; i++) { // Only want 10 elements...
            if (sites.size() < 11 && i < googleSites.size()) {
                sites.add(googleSites.get(i));
            } if (sites.size() < 11 && i < bingSites.size()) {
                sites.add(bingSites.get(i));
            } if (sites.size() > 10) {
                return sites;
            }
        }
        return sites;
    }

    private HelperClass() {}; // Singleton design

    public static synchronized HelperClass getInstance() {
        return instance;
    }

    public void changeForwardArrow(boolean shouldBeFilled, ImageView img, String resourcesPath) { // hasNext denotes whether there is a site "in front of" the site the user is currently viewing
        if (shouldBeFilled) {
            img.setImage(new Image(getClass().getResourceAsStream(resourcesPath + "forwardArrowNormal.png")));
        } else {
            img.setImage(new Image(getClass().getResourceAsStream(resourcesPath + "fwdArrowNothing.png")));
        }
    }

    public void changeBackArrow(boolean shouldBeFilled, ImageView img, String resourcesPath) { // hasNext denotes whether there is a site "in front of" the site the user is currently viewing
        if (shouldBeFilled) {
            img.setImage(new Image(getClass().getResourceAsStream(resourcesPath + "backArrowNormal.png")));
        } else {
            img.setImage(new Image(getClass().getResourceAsStream(resourcesPath + "backArrowNothing.png")));
        }
    }



    public boolean isValidURL(String url) {
        try {
            URL test = new URL(url);
            test.toURI();
            test.openConnection().connect();
        } catch (URISyntaxException | IOException e) {
            return false;
        }
        return true;
    }

}