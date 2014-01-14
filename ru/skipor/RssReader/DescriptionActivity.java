package ru.skipor.RssReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class DescriptionActivity extends Activity {
    public static final String EXTRA_DESCRIPTION = "ru.skipor.RssReader.DescriptionActivity description";
    public static final String EXTRA_FEED_NAME = "ru.skipor.RssReader.DescriptionActivity description feed name";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        WebView webView = (WebView) findViewById(R.id.item_description);
        Intent intent = getIntent();
        String message = intent.getStringExtra(EXTRA_DESCRIPTION);
        setTitle(intent.getStringExtra(EXTRA_FEED_NAME));
        webView.loadData(message, "text/html; charset=utf-8", null);





    }



}
