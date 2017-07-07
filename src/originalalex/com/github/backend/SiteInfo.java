package originalalex.com.github.backend;


public class SiteInfo {

    private String url;
    private String description;
    private String title;

    public SiteInfo(String url, String description, String title) {
        this.description = description;
        this.url = url;
        this.title = title;
    }

    public String getTitle () {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public String getDescription() {
        return this.description;
    }
}