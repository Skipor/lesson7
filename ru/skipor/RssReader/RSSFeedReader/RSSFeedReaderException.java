package ru.skipor.RssReader.RSSFeedReader;

/**
 * Created by Vladimir Skipor on 10/17/13.
 * Email: vladimirskipor@gmail.com
 */
public class RSSFeedReaderException extends Exception {
    public RSSFeedReaderException() {
        super();
    }

    public RSSFeedReaderException(String detailMessage) {
        super(detailMessage);
    }

    public RSSFeedReaderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RSSFeedReaderException(Throwable throwable) {
        super(throwable);
    }
}
