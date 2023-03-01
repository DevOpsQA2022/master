package com.silvercreek.wmspickingclient.controller;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.silvercreek.wmspickingclient.R;
import com.silvercreek.wmspickingclient.database.WMSDbHelper;
import com.silvercreek.wmspickingclient.model.picktasklist;
import com.silvercreek.wmspickingclient.util.Supporter;

import java.util.List;

public class PickTaskListAdapter extends ArrayAdapter<picktasklist> {

    private final List<picktasklist> list;
    private final Activity context;
    private Supporter mSupporter;
    private WMSDbHelper mDbHelper;
    private LayoutInflater inflator;

    static class ViewHolder {
        protected TextView TaskNo;
        protected TextView Route;
        protected TextView Stop;
        protected TextView Status;
    }

    public PickTaskListAdapter(Activity context, List<picktasklist> list) {
        super(context, R.layout.adapter_picktasklist, list);
        this.context = context;
        this.list = list;
        mDbHelper = new WMSDbHelper(context);
        mSupporter = new Supporter(context, mDbHelper);
        this.inflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = inflator.inflate(R.layout.adapter_picktasklist, null);

            if (position % 2 == 1) {
                view.setBackgroundColor(Color.parseColor("#d0d8e8"));
            } else {
                    view.setBackgroundColor(Color.parseColor("#e9edf4"));
            }

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.TaskNo = (TextView) view
                    .findViewById(R.id.tvTask);
            viewHolder.Route = (TextView) view
                    .findViewById(R.id.tvRoute);
            viewHolder.Stop  = (TextView) view
                    .findViewById(R.id.tvStop);
            viewHolder.Status = (TextView) view
                    .findViewById(R.id.tvStatus);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            if (position % 2 == 1) {
                view.setBackgroundColor(Color.parseColor("#d0d8e8"));
            } else {
                view.setBackgroundColor(Color.parseColor("#e9edf4"));
            }
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.TaskNo.setText(list.get(position).getTaskNo());
        holder.Route.setText(list.get(position).getRoute());
        holder.Stop.setText(list.get(position).getStop());
        holder.Status.setText(list.get(position).getStatus());
        return view;
    }
}
