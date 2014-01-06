package ru.skipor.RssReader.RSSFeedReader;

/**
 * Created by Vladimir Skipor on 11/7/13.
 * Email: vladimirskipor@gmail.com
 */

import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SAXRSSReader implements RSSFeedReader {
    public static final String TAG = "SAXRSSReader";

    public SAXRSSReader() {
    }


    @Override
    public RSSFeed parse(String feedURL) throws RSSFeedReaderException {
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet httpget = new HttpGet(feedURL);
        HttpResponse response;
        RSSFeed rssFeed = null;
        try {
            response = httpclient.execute(httpget);
            Log.i(TAG, "Connection status: " + response.getStatusLine().toString());


            HttpEntity entity = response.getEntity();
            String xml = EntityUtils.toString(entity);
//                String xml = EntityUtils.toString(entity, "utf-8");
            Log.d(TAG, "Encoding can be founded:" + getEncodingFromEntity(entity));
            RSSHandler rssHandler = new RSSHandler();
            Xml.parse(xml, rssHandler);

            rssFeed = rssHandler.getRssFeed();
        } catch (SAXException e) {
            Log.e(TAG, "Error - SAXException", e);
            throw new RSSFeedReaderException(e);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Error - ClientProtocolException", e);
            throw new RSSFeedReaderException(e);


        } catch (IOException e) {
            Log.e(TAG, "Error - IOException", e);
            throw new RSSFeedReaderException(e);

        }


        return rssFeed;
    }


    private String getEncodingFromEntity(HttpEntity entity) {
        if (entity.getContentType() != null) {
            //Content-Type: text/xml; charset=ISO-8859-1
            //Content-Type: text/xml; charset=UTF-8
            for (String str : entity.getContentType().getValue().split(";")) {
                if (str.toLowerCase().contains("charset")) {
                    return str.toLowerCase().replace("charset=", "").replace(";", "").replace(" ", "");
                }
            }
        }
        return null;
    }


}

class RSSHandler extends DefaultHandler {


    private static Set<String> itemTags, titleTags, linkTags, descriptionTags, dateTags, feedTags;
    private static String rssHeadTag = "rss";
    private static String atomHeadTag = "feed";

    static {
        String[]
                items = {"item", "entry"},
                titles = {"title"},
                links = {"link"},
                descriptions = {"summary", "description", "content"},
                dates = {"pubDate", "published"},
                feeds = {"feed", "channel"};

        itemTags = new HashSet<String>(Arrays.asList(items));
        titleTags = new HashSet<String>(Arrays.asList(titles));
        linkTags = new HashSet<String>(Arrays.asList(links));
        descriptionTags = new HashSet<String>(Arrays.asList(descriptions));
        dateTags = new HashSet<String>(Arrays.asList(dates));
        feedTags = new HashSet<String>(Arrays.asList(feeds));


    }


    public static final String TAG = "RSSHandler";
    private StringBuilder elementValueBuilder;
    private RSSItem rssItem = new RSSItem();
    private RSSFeed rssFeed = new RSSFeed();
    private List<RSSItem> itemList = rssFeed.getItemList();


    final SimpleDateFormat dateFormatRSS2 = RSSItem.DATE_FORMAT;
    final SimpleDateFormat dateFormatAtom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat dateFormat;
    private boolean headTagFlag = true;
    private boolean channelOn = false;


    public RSSFeed getRssFeed() {
        return rssFeed;
    }

    /**
     * This will be called when the tags of the XML starts.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elementValueBuilder = new StringBuilder();

        if (headTagFlag) {
            headTagFlag = false;
            if (localName.equals(rssHeadTag)) {
                dateFormat = dateFormatRSS2;

            } else if (localName.equals(atomHeadTag)) {
                dateFormat = dateFormatAtom;

            } else {
                throw new SAXException("Unsupported feed format.");
            }

        }

        if (feedTags.contains(localName)) {
            channelOn = true;
        }

        if (itemTags.contains(localName)) {
            channelOn = false;
            rssItem = new RSSItem();
        }

//        Log.d(TAG, localName + " tag is opened");


    }

    /**
     * This will be called when the tags of the XML end.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        /**
         * Sets the values after retrieving the values from the XML tags
         * */
//        Log.d(TAG, localName + " tag is closed with value " + elementValueBuilder.toString());
        if (channelOn) {

            if (titleTags.contains(localName)) {
                rssFeed.setTitle(elementValueBuilder.toString());
            } else if (descriptionTags.contains(localName)) {
                rssFeed.setDescription(elementValueBuilder.toString());
            } else if (linkTags.contains(localName)) {
                rssFeed.setLink(elementValueBuilder.toString());
            }

        } else {
            if (itemTags.contains(localName)) {
                itemList.add(rssItem);
//                Log.d(TAG, "Item " + rssItem.toString() + " added");
            } else if (titleTags.contains(localName)) {
                rssItem.setTitle(elementValueBuilder.toString());
            } else if (descriptionTags.contains(localName)) {
                rssItem.setDescription(elementValueBuilder.toString());
            } else if (linkTags.contains(localName)) {
                rssItem.setLink(elementValueBuilder.toString());
            } else if (dateTags.contains(localName)) {
                try {
                    dateFormat.parse(elementValueBuilder.toString().trim());
                } catch (ParseException e) {
                    Log.e(TAG, "Invalid date format: " + elementValueBuilder.toString(), e);
                    throw new SAXException("Invalid date format: " + elementValueBuilder.toString(), e);
                }
            } else {
//                Log.d(TAG, "tag " + localName + " skipped");
            }

        }


    }

    /**
     * This is called to get the tags value
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        elementValueBuilder.append(ch, start, length);
    }


}
