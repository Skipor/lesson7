/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.skipor.RssReader.FeedsDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedsDatabaseHelper {

    private static FeedsDatabaseHelper instance = null;

    public static FeedsDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FeedsDatabaseHelper(context);
        }
        return instance;
    }

    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_ROWID = "_id";
    public static final String FEED_KEY_TITLE = "title";
    public static final String FEED_KEY_BODY = "body";

    private static final String TAG = "FeedsDbAdapter";
    private final DatabaseHelper myDatabaseHelper;
    private SQLiteDatabase myDatabase;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table feeds (_id integer primary key autoincrement, "
                    + "name text not null, url text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "feeds";
    private static final int DATABASE_VERSION = 1;

    private int databaseUsers;


    private static class DatabaseHelper extends SQLiteOpenHelper {


        public DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS feeds");
            onCreate(db);
        }
    }


    private FeedsDatabaseHelper(Context context) {
        myDatabaseHelper = new DatabaseHelper(context);
        databaseUsers = 0;

    }


    synchronized public void open() throws SQLException {

        if (databaseUsers == 0) {
            myDatabase = myDatabaseHelper.getWritableDatabase();
        }
        databaseUsers++;


    }


    synchronized public void close() {
        databaseUsers--;
        if (databaseUsers == 0) {
            myDatabaseHelper.close();
            myDatabase = null;
        }
    }

    synchronized  private String getFeedTableName(String feedURL) {
        return "[" + feedURL + "]"; // valid sqlite table name
    }

    synchronized private void dropFeedTableIfExists(String feedURL) {
        myDatabase.execSQL("DROP TABLE IF EXISTS " + getFeedTableName(feedURL));
    }

    synchronized public void dropFeedTableIfExists(long rowId) {
        Cursor cursor = fetchFeed(rowId);
        dropFeedTableIfExists(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));

    }

    synchronized public void createOrRecreateFeedTable(String feedURL) {
        dropFeedTableIfExists(feedURL);
        myDatabase.execSQL("create table " + getFeedTableName(feedURL) +
                "(_id integer primary key autoincrement, "
                + FEED_KEY_TITLE + " text not null, " + FEED_KEY_BODY + " text not null);");
    }


    /**
     * Create a new feed using the name and url provided. If the feed is
     * successfully created return the new rowId for that feed, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the name of the feed
     * @param url  the url of the feed
     * @return rowId or -1 if failed
     */
    public long createFeed(String name, String url) {
        createOrRecreateFeedTable(url);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_URL, url);

        return myDatabase.insert(DATABASE_TABLE, null, initialValues);
    }

    public long createTitle(String feedURL, String title, String body) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(FEED_KEY_TITLE, title);
        initialValues.put(FEED_KEY_BODY, body);

        return myDatabase.insert(getFeedTableName(feedURL), null, initialValues);
    }

    /**
     * Delete the feed with the given rowId
     *
     * @param rowId id of feed to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFeed(long rowId) {
        dropFeedTableIfExists(rowId);

        return myDatabase.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all feeds in the database
     *
     * @return Cursor over all feeds
     */
    public Cursor fetchAllFeeds() {


        return myDatabase.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME,
                KEY_URL}, null, null, null, null, null);
    }
    public Cursor fetchAllTitles(String feedURL) {


        return myDatabase.query(getFeedTableName(feedURL), new String[]{KEY_ROWID, FEED_KEY_TITLE,
                FEED_KEY_BODY}, null, null, null, null, null);
    }


    /**
     * Return a Cursor positioned at the feed that matches the given rowId
     *
     * @param rowId id of feed to retrieve
     * @return Cursor positioned to matching feed, if found
     * @throws android.database.SQLException if feed could not be found/retrieved
     */
    public Cursor fetchFeed(long rowId) throws SQLException {

        Cursor mCursor =

                myDatabase.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,
                        KEY_NAME, KEY_URL}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;

    }




    public Cursor fetchTitle(String feedURL, long rowId) throws SQLException {

        Cursor mCursor =

                myDatabase.query(true, getFeedTableName(feedURL), new String[]{KEY_ROWID,
                       FEED_KEY_TITLE, FEED_KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;

    }




    /**
     * Update the feed using the details provided. The feed to be updated is
     * specified using the rowId, and it is altered to use the name and url
     * values passed in
     *
     * @param rowId id of feed to update
     * @param name  value to set feed name to
     * @param url   value to set feed url to
     * @return true if the feed was successfully updated, false otherwise
     */
    public boolean updateFeed(long rowId, String name, String url) {
        dropFeedTableIfExists(rowId);
        createOrRecreateFeedTable(url);

        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_URL, url);

        return myDatabase.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
