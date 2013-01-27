/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */

package at.ac.tuwien.detlef.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;

public class LicensesActivity extends ExpandableListActivity {
    private ExpandableListAdapter adapter;
    private final List<License> licenses = new ArrayList<License>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        licenses.clear();
        licenses.add(new License("Detlef", "https://github.com/gpodder/detlef",
                                 readRawText(R.raw.license_gpl2)));
        licenses.add(new License("CWAC MergeAdapter & SackOfViewsAdapter",
                                 "https://github.com/commonsguy", readRawText(R.raw.license_apache)));
        licenses.add(new License("google-gson", "http://code.google.com/p/google-gson/",
                                 readRawText(R.raw.license_apache)));
        licenses.add(new License("mygpoclient-java", "https://github.com/Dragontek/mygpoclient-java",
                                 readRawText(R.raw.license_gpl3)));
        licenses.add(new License("drag-sort-listview", "https://github.com/bauerca/drag-sort-listview",
                                 readRawText(R.raw.license_apache)));

        adapter = new LicensesExpandableListAdapter(this, licenses);
        setListAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String readRawText(int id) {
        InputStream is = null;
        BufferedReader br = null;

        try {
            is = getResources().openRawResource(id);
            br = new BufferedReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();

            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }

                sb.append(s);
                sb.append('\n');
            }

            return sb.toString();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) { }
            try {
                if (is != null) is.close();
            } catch (IOException e) { }
        }
    }

    /**
     * This adapter shows a general summary of each license as an overview, and
     * can be expanded to show the full license text.
     *
     * The group represents a single license overview, and each group has a single child
     * containing the license text.
     */
    private static class LicensesExpandableListAdapter extends BaseExpandableListAdapter {

        private final Context context;
        private final List<License> licenses;

        public LicensesExpandableListAdapter(Context context, List<License> licenses) {
            this.context = context;
            this.licenses = licenses;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(
                                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.license_list_child_layout, null);
            }

            License l = licenses.get(groupPosition);

            TextView name = (TextView)v.findViewById(R.id.license);
            name.setText(l.getLicense());

            return v;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return licenses.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return licenses.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(
                                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.license_list_group_layout, null);
            }

            License l = licenses.get(groupPosition);

            TextView name = (TextView)v.findViewById(R.id.name);
            name.setText(l.getName());

            TextView url = (TextView)v.findViewById(R.id.url);
            url.setText(l.getUrl());

            return v;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    private static class License {

        private final String name;
        private final String url;
        private final String license;

        public License(String name, String url, String license) {
            this.name = name;
            this.url = url;
            this.license = license;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getLicense() {
            return license;
        }
    }
}
