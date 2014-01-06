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
package ru.skipor.RssReader;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.skipor.RssReader.FeedsDatabase.FeedsDatabaseHelper;

public class FeedEdit extends Activity {

    private EditText mNameText;
    private EditText mUrlText;
    private Long mRowId;
    private FeedsDatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = FeedsDatabaseHelper.getInstance(this);

        setContentView(R.layout.feed_edit);
        setTitle(R.string.edit_feed);

        mNameText = (EditText) findViewById(R.id.title);
        mUrlText = (EditText) findViewById(R.id.body);

//        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(FeedsDatabaseHelper.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(FeedsDatabaseHelper.KEY_ROWID)
									: null;
		}

		populateFields();

//        confirmButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                setResult(RESULT_OK);
//                finish();
//            }
//
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                setResult(RESULT_OK);
                finish();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    private void populateFields() {
        if (mRowId != null) {
            Cursor feed = mDatabaseHelper.fetchFeed(mRowId);
            startManagingCursor(feed);
            mNameText.setText(feed.getString(
                    feed.getColumnIndexOrThrow(FeedsDatabaseHelper.KEY_NAME)));
            mUrlText.setText(feed.getString(
                    feed.getColumnIndexOrThrow(FeedsDatabaseHelper.KEY_URL)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(FeedsDatabaseHelper.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mNameText.getText().toString();
        String body = mUrlText.getText().toString();

        if (mRowId == null) {
            long id = mDatabaseHelper.createFeed(title, body);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDatabaseHelper.updateFeed(mRowId, title, body);
        }
    }

}
