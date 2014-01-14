package ru.skipor.RssReader.RSSFeedReader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Vladimir Skipor on 10/17/13.
 * Email: vladimirskipor@gmail.com
 */
public class RSSItem {


    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
    private String title;
    private String link;
    private String description;
    private Date date;


    public void setLink(String link) {
        this.link = link;

    }

    public String getStringDate() {
        return DATE_FORMAT.format(this.date);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RSSItem)) return false;

        RSSItem item = (RSSItem) o;

        if (date != null ? !date.equals(item.date) : item.date != null) return false;
        if (description != null ? !description.equals(item.description) : item.description != null)
            return false;
        if (link != null ? !link.equals(item.link) : item.link != null) return false;
        if (title != null ? !title.equals(item.title) : item.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RSSItem{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }
}
