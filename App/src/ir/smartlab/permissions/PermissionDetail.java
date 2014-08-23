package ir.smartlab.permissions;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PermissionDetail extends Activity {
    ListView applicationList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.permission_detail);

        Intent thisIntent = getIntent();
        String permissionId = Long.toString(thisIntent.getExtras().getLong("permissionId"));

        Cursor data = Tools.database.database.query("permission", new String[] { "name" }, "id = ?", new String[] { permissionId }, null, null, null);
        if (data.getCount() == 1) {
            data.moveToFirst();

            ((TextView) findViewById(R.id.permission_detail_name)).setText(data.getString(0));
            TextView permission_detail_description = ((TextView) findViewById(R.id.permission_detail_description));
            try {
                permission_detail_description.setText(Tools.getStringResourceByName("permission_" + data.getString(0), getResources(), this));
            } catch (Exception ex) {
                permission_detail_description.setText(data.getString(0));
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean hideSystemApp = pref.getBoolean("hide_system_app", true);
            String systemAppWhere = "";
            if (hideSystemApp)
                systemAppWhere = " AND system = 0";

            data = Tools.database.database.rawQuery("SELECT Count(*) AS number " + "FROM relation_application_permission " + "LEFT OUTER JOIN application ON relation_application_permission.application = application.id "
                    + "WHERE permission = ?" + systemAppWhere + ";", new String[] { permissionId });
            data.moveToFirst();
            ((TextView) findViewById(R.id.permission_detail_application_count)).setText(data.getString(0));

            boolean applicationName = pref.getBoolean("application_name", true);

            String nameField;
            if (applicationName)
                nameField = "application.label";
            else
                nameField = "application.name";

            Cursor applicationListCursor = Tools.database.database.rawQuery("SELECT application.id AS _id, " + nameField + " AS name, application.name AS package " + "FROM relation_application_permission "
                    + "INNER JOIN application ON relation_application_permission.application = application.id " + "WHERE relation_application_permission.permission = ?" + systemAppWhere + " " + "ORDER BY " + nameField
                    + " COLLATE NOCASE ASC;", new String[] { permissionId });
            startManagingCursor(applicationListCursor);

            List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();

            PackageManager pm = getPackageManager();
            try {
                for (applicationListCursor.moveToFirst(); !applicationListCursor.isAfterLast(); applicationListCursor.moveToNext()) {
                    items.add(new ApplicationListItem(applicationListCursor.getLong(0), pm.getApplicationIcon(applicationListCursor.getString(2)), applicationListCursor.getString(1)));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);

            applicationList = (ListView) findViewById(R.id.permission_detail_application_list);
            applicationList.setAdapter(applicationAdapter);

            applicationList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Intent intent = new Intent(getBaseContext(), ApplicationDetail.class);
                    intent.putExtra("applicationId", id);
                    startActivity(intent);
                }
            });
        } else {
            ((TextView) findViewById(R.id.permission_detail_name)).setText(getString(R.string.permission_detail_nodata));
        }
        data.close();
    }
}
