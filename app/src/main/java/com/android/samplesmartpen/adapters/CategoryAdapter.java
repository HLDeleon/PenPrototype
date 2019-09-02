package com.android.samplesmartpen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.samplesmartpen.R;
import com.android.samplesmartpen.models.Category;

import java.util.ArrayList;

public class CategoryAdapter extends BaseAdapter {

    private Context mcontext;
    private ArrayList<Category> mfalCategories;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
        this.mcontext = context;
        this.mfalCategories = categories;
    }

    @Override
    public int getCount() {
        return mfalCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return mfalCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mcontext).inflate(R.layout.listview_layout, null);
            holder = new ViewHolder();
            holder.mtvTitle = convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.mtvTitle.setText(mfalCategories.get(position).getFsName());

        return convertView;
    }

    class ViewHolder {
        TextView mtvTitle;
    }
}
