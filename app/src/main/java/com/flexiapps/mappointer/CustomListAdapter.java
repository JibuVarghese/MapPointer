package com.flexiapps.mappointer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Flexiapps on 22-Dec-16.
 */

public class CustomListAdapter extends ArrayAdapter<String> {

    private List<String> toponymName;
    private List<String> names;
    private List<String> lat;
    private List<String> lng;
    private List<String> countryCode;
    private Context context;
    LayoutInflater inflater;

    public CustomListAdapter(Context context, int resource, List<String> toponymName, List<String> names, List<String> lat, List<String> lng, List<String> countryCode) {
        super(context, resource, toponymName);
        this.context = context;
        this.toponymName = toponymName;
        this.names = names;
        this.lat = lat;
        this.lng = lng;
        this.countryCode = countryCode;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listViewItem = inflater.inflate(R.layout.list_item_view, null, true);
        TextView textView1 = (TextView) listViewItem.findViewById(R.id.textView1);
        TextView textView2 = (TextView) listViewItem.findViewById(R.id.textView2);
        TextView textView3 = (TextView) listViewItem.findViewById(R.id.textView3);

        textView1.setText(toponymName.get(position) +" ("+ countryCode.get(position) +")");
        textView2.setText(names.get(position));
        textView3.setText("Lat : "+ lat.get(position) + "& Long : "+ lng.get(position));

        return listViewItem;
    }
}
