package org.hcilab.projects.nlog.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.hcilab.projects.nlog.R;
import org.hcilab.projects.nlog.misc.Const;
import org.hcilab.projects.nlog.misc.DatabaseHelper;
import org.hcilab.projects.nlog.misc.Util;
import org.hcilab.projects.nlog.service.DeleteLogValueService;
import org.hcilab.projects.nlog.service.NotificationHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private BroadcastReceiver updateReceiver;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra(EXTRA_ID);
            if (id != null) {
                loadDetails(id);
            } else {
                finishWithToast();
            }
        } else {
            finishWithToast();
        }

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null &&
                        id.equals(String.valueOf(intent.getLongExtra(DatabaseHelper.PostedEntry._ID, -1))))
                    finishWithDeleteToast();
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationHandler.DELETE_BROADCAST);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.details_delete)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        deleteValue();
                        return true;
                    }
                });
        return true;
    }

    private void deleteValue() {
        DeleteLogValueService.deleteValue(DetailsActivity.this, Long.parseLong(id));
    }

    private void loadDetails(String id) {
        JSONObject json = null;
        String str = "error";
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            Cursor cursor = db.query(DatabaseHelper.PostedEntry.TABLE_NAME,
                    new String[]{
                            DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT,
                    },
                    DatabaseHelper.PostedEntry._ID + " = ?",
                    new String[]{
                            id
                    },
                    null,
                    null,
                    null,
                    "1");

            if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                try {
                    json = new JSONObject(cursor.getString(0));
                    str = json.toString(2);
                } catch (JSONException e) {
                    if (Const.DEBUG) e.printStackTrace();
                }
                cursor.close();
            }

            db.close();
            databaseHelper.close();
        } catch (Exception e) {
            if (Const.DEBUG) e.printStackTrace();
        }
        TextView tvJSON = findViewById(R.id.json);
        tvJSON.setText(str);

        CardView card = findViewById(R.id.card);
        if (json != null) {
            String titleText = json.optString("title");
            String contentText = json.optString("text");
            String text = (titleText + "\n" + contentText).trim();
            if (!"".equals(text)) {

                card.setVisibility(View.VISIBLE);
                ImageView icon = findViewById(R.id.icon);
                icon.setImageDrawable(Util.getAppIconFromPackage(this, json.optString("packageName", getPackageName())));
                TextView tvName = findViewById(R.id.name);
                tvName.setText(Util.getAppNameFromPackage(this, json.optString("packageName", "???"), false));
                TextView tvText = findViewById(R.id.text);
                tvText.setText(text);
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
                TextView tvDate = findViewById(R.id.date);
                tvDate.setText(format.format(json.optLong("systemTime")));

            } else {
                card.setVisibility(View.GONE);
            }
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void finishWithToast() {
        Toast.makeText(this, R.string.details_error, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void finishWithDeleteToast() {
        Toast.makeText(this, R.string.details_value_deleted, Toast.LENGTH_SHORT).show();
        finish();
    }


}