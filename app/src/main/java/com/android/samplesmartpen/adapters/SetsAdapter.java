package com.android.samplesmartpen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.samplesmartpen.R;
import com.android.samplesmartpen.models.Set;

import java.util.ArrayList;

public class SetsAdapter extends BaseAdapter {
    private ArrayList<Set> mCategories;
    private Context context;
    public static int PICK_SETS = -1;

    public SetsAdapter(Context context, ArrayList<Set> category) {
        this.context = context;
        this.mCategories = category;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mCategories.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mCategories.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_layout, null);
            holder = new ViewHolder();
            holder.tvText = convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvText.setText(mCategories.get(position).getFsName());

        return convertView;
    }

    class ViewHolder {
        TextView tvText;
    }
}
