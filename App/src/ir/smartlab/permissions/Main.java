package ir.smartlab.permissions;

import ir.smartlab.permissions.App.TrackerName;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Main extends Activity {
    private ListView lstCategory;
    private ListView lstApplication;
    private ListView lstPermission;

    private TextView tabCategory;
    private TextView tabApplication;
    private TextView tabPermission;

    private ImageButton settingsImageButton;
    private ImageButton reloadImageButton;

    private enum VIEWS {
        Category, Permission, Application
    };

    private VIEWS currentView;

    private final int ACTIVITY_RESULT_PREFERENCE = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Tracker t = ((App) getApplication()).getTracker(TrackerName.APP_TRACKER);
        t.send(new HitBuilders.AppViewBuilder().build());

        reloadImageButton = (ImageButton) findViewById(R.id.reloadImageButton);
        settingsImageButton = (ImageButton) findViewById(R.id.settingsImageButton);

        lstCategory = (ListView) findViewById(R.id.listviewcategory);
        lstCategory.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent(getBaseContext(), CategoryDetail.class);
                intent.putExtra("categoryId", id);
                startActivity(intent);
            }
        });

        lstApplication = (ListView) findViewById(R.id.listviewapplication);
        lstApplication.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent(getBaseContext(), ApplicationDetail.class);
                intent.putExtra("applicationId", id);
                startActivity(intent);
            }
        });

        lstPermission = (ListView) findViewById(R.id.listviewpermission);
        lstPermission.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                // Ouverture du d�tail d'une permission
                Intent intent = new Intent(getBaseContext(), PermissionDetail.class);
                intent.putExtra("permissionId", id);
                startActivity(intent);
            }
        });

        tabCategory = (TextView) findViewById(R.id.tab_category);
        tabCategory.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Category) {
                    switchTo(VIEWS.Category);
                }
            }
        });

        tabApplication = (TextView) findViewById(R.id.tab_application);
        tabApplication.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Application) {
                    switchTo(VIEWS.Application);
                }
            }
        });

        tabPermission = (TextView) findViewById(R.id.tab_permission);
        tabPermission.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Permission) {
                    switchTo(VIEWS.Permission);
                }
            }
        });

        reloadImageButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Tools.database.updateDatabase(Main.this);
            }
        });

        settingsImageButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Preference.class);
                startActivityForResult(intent, ACTIVITY_RESULT_PREFERENCE);
            }
        });

        switchTo(VIEWS.Application);

        Tools.database = new Database(this);
        Tools.database.isUpToDate();
    }

    private void switchTo(VIEWS newView) {
        currentView = newView;

        if (currentView == VIEWS.Category) {
            lstCategory.setVisibility(View.VISIBLE);
            tabCategory.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstCategory.setVisibility(View.INVISIBLE);
            tabCategory.setTextColor(getResources().getColor(R.color.text_tab));
        }

        if (currentView == VIEWS.Application) {
            lstApplication.setVisibility(View.VISIBLE);
            tabApplication.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstApplication.setVisibility(View.INVISIBLE);
            tabApplication.setTextColor(getResources().getColor(R.color.text_tab));
        }

        if (currentView == VIEWS.Permission) {
            lstPermission.setVisibility(View.VISIBLE);
            tabPermission.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstPermission.setVisibility(View.INVISIBLE);
            tabPermission.setTextColor(getResources().getColor(R.color.text_tab));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.database.database.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case ACTIVITY_RESULT_PREFERENCE:
            refreshData();
        }

    }

    private void refreshData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean categoryOrder = pref.getBoolean("category_order", false);
        boolean applicationName = pref.getBoolean("application_name", true);
        boolean applicationOrder = pref.getBoolean("application_order", false);
        boolean permissionOrder = pref.getBoolean("permission_order", false);
        boolean hideSystemApp = pref.getBoolean("hide_system_app", false);

        String systemAppWhere = "";
        if (hideSystemApp)
            systemAppWhere = "WHERE system = 0 ";

        String nameField;
        if (applicationName)
            nameField = "application.label";
        else
            nameField = "application.name";

        String orderField;
        if (applicationOrder)
            orderField = "name COLLATE NOCASE ASC";
        else
            orderField = "Count(relation_application_permission.permission) COLLATE NOCASE DESC";

        Cursor applicationCursor = Tools.database.database.rawQuery("SELECT id AS _id, " + nameField + " || ' (' || Count(permission) || ')' AS name, application.name AS package " + "FROM application "
                + "LEFT OUTER JOIN relation_application_permission ON application.id = relation_application_permission.application " + systemAppWhere + "GROUP BY application.id " + "ORDER BY " + orderField + ";", null);
        startManagingCursor(applicationCursor);

        List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();

        PackageManager pm = getPackageManager();
        try {
            for (applicationCursor.moveToFirst(); !applicationCursor.isAfterLast(); applicationCursor.moveToNext()) {
                items.add(new ApplicationListItem(applicationCursor.getLong(0), pm.getApplicationIcon(applicationCursor.getString(2)), applicationCursor.getString(1)));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);
        lstApplication.setAdapter(applicationAdapter);

        if (permissionOrder)
            orderField = "permission.name COLLATE NOCASE ASC";
        else
            orderField = "Count(relation_application_permission.application) COLLATE NOCASE DESC";

        Cursor permissionCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name || ' (' || Count(application) || ')' AS name " + "FROM permission "
                + "LEFT OUTER JOIN relation_application_permission ON permission.id = relation_application_permission.permission " + "LEFT OUTER JOIN application ON application.id = relation_application_permission.application "
                + systemAppWhere + "GROUP BY permission.id " + "HAVING Count(application) > 0 " + "ORDER BY " + orderField + ";", null);
        startManagingCursor(permissionCursor);
        ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionCursor, new String[] { "name" }, new int[] { R.id.listviewpermissiontext });
        lstPermission.setAdapter(permissionAdapter);

        if (categoryOrder)
            orderField = "category.name COLLATE NOCASE ASC";
        else
            orderField = "Count(DISTINCT relation_application_permission.application) COLLATE NOCASE DESC";

        Cursor categoryCursor = Tools.database.database.rawQuery("SELECT category.id AS _id, category.name || ' (' || Count(DISTINCT application) || ')' AS name " + "FROM category "
                + "LEFT OUTER JOIN relation_category_permission ON category.id = relation_category_permission.category "
                + "INNER JOIN relation_application_permission ON relation_category_permission.permission = relation_application_permission.permission "
                + "LEFT OUTER JOIN application ON application.id = relation_application_permission.application " + systemAppWhere + "GROUP BY category.id " + "HAVING Count(DISTINCT application) > 0 " + "ORDER BY " + orderField + ";", null);
        startManagingCursor(categoryCursor);
        ListAdapter categoryAdapter = new SimpleCursorAdapter(this, R.layout.category_list_item, categoryCursor, new String[] { "name" }, new int[] { R.id.listviewcategorytext });
        lstCategory.setAdapter(categoryAdapter);
    }

    public void databaseUpdated() {
        refreshData();
    }

    public void isUpToDateResult(boolean upToDate) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Tools.database.updateDatabase(Main.this);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    refreshData();
                    break;
                }
            }
        };

        if (upToDate) {
            refreshData();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(getString(R.string.alert_database_nottodate_text));
            alert.setPositiveButton(getString(R.string.alert_database_nottodate_yes), dialogClickListener);
            alert.setNegativeButton(getString(R.string.alert_database_nottodate_no), dialogClickListener);
            alert.show();
        }
    }
}