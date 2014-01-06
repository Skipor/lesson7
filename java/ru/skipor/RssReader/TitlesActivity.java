package ru.skipor.RssReader;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import ru.skipor.RssReader.FeedsDatabase.FeedsDatabaseHelper;

public class TitlesActivity extends ListActivity implements AppResultReceiver.Receiver {


    public static final String EXTRA_FEED_URL = "ru.skipor.RssReader.TitlesActivity Feed URL";
    public static final String EXTRA_FEED_NAME = "ru.skipor.RssReader.TitlesActivity Feed Name";
    public static final String UPDATE_MESSAGE = "Feed is up to date";


    public static final String TAG = "TitlesActivity";
    private FeedsDatabaseHelper myDatabaseHelper;
    private String feedURL;
    private String feedName;
    private Cursor currentCursor;
    private AppResultReceiver appResultReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        feedURL = intent.getStringExtra(EXTRA_FEED_URL);
        feedName = intent.getStringExtra(EXTRA_FEED_NAME);
        setTitle(feedName);
        appResultReceiver = new AppResultReceiver(new Handler());
        appResultReceiver.setReceiver(this);



        setContentView(R.layout.titles_list);
        myDatabaseHelper = FeedsDatabaseHelper.getInstance(this);
        myDatabaseHelper.open();
        //fill data
        currentCursor = myDatabaseHelper.fetchAllTitles(feedURL);

        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[]{FeedsDatabaseHelper.FEED_KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter feeds =
                new SimpleCursorAdapter(this, R.layout.title_row, null, from, to, 0);
        setListAdapter(feeds);

        if (currentCursor == null || currentCursor.getCount() == 0) {
            updateFeed();
        } else {
            feeds.swapCursor(currentCursor);
        }

        //fill data end


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.titles_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateFeed();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void refillData() {
        currentCursor = myDatabaseHelper.fetchAllTitles(feedURL);
        Cursor oldCursor = ((SimpleCursorAdapter) getListAdapter()).swapCursor(currentCursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private void updateFeed() {

            final Intent intent = new Intent(RSSUpdateService.ACTION_UPDATE_ONE, null, this, RSSUpdateService.class);
            intent.putExtra(RSSUpdateService.EXTRA_FEED_URL, feedURL);
            intent.putExtra(RSSUpdateService.EXTRA_INFORM_ABOUT_UPDATE, true);
            intent.putExtra(RSSUpdateService.EXTRA_RECEIVER, appResultReceiver);
            startService(intent);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (currentCursor.isClosed()) { //if base close cursors because of inserting
            refillData();
            return;

        }
        currentCursor.moveToPosition((int) id - 1);
        Cursor cursor = null;
        try {
            cursor = myDatabaseHelper.fetchTitle(feedURL, id);
            String body = cursor.getString(cursor.getColumnIndexOrThrow(FeedsDatabaseHelper.FEED_KEY_BODY));
            Intent intent = new Intent(this, DescriptionActivity.class);
            intent.putExtra(DescriptionActivity.EXTRA_DESCRIPTION, body);
            intent.putExtra(DescriptionActivity.EXTRA_FEED_NAME, feedName);
            startActivity(intent);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }


    @Override
    protected void onResume() {
//        refillData(); //refill only when result received
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        myDatabaseHelper.close();
        super.onDestroy();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        if (resultCode == RSSUpdateService.RESULT_CODE_UPTODATE) {
            refillData();
            Toast.makeText(this, UPDATE_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }
//    private void closeCursor() {
//        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
//        if (cursor != null) {
//            cursor.close();
//        }
//    }
}

