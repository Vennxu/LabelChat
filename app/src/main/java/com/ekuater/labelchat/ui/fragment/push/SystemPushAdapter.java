package com.ekuater.labelchat.ui.fragment.push;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/4/30.
 */
public class SystemPushAdapter extends BaseAdapter{

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<SystemPushListItem.PushItem> systemPushs;

    public SystemPushAdapter(Context context){
        this.context = context;
        systemPushs = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public void updateAdapter(ArrayList<SystemPushListItem.PushItem> systemPush){
            systemPushs.clear();
            systemPushs.addAll(systemPush);
            notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return systemPushs.size();
    }

    @Override
    public SystemPushListItem.PushItem getItem(int position) {
        return systemPushs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return SystemPushListItem.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return systemPushs.get(position).getShowViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SystemPushListItem.PushItem pushItem = getItem(position);
        if (convertView == null){
            convertView = pushItem.newView(inflater, parent);
        }
        pushItem.bindView(convertView);
        return convertView;
    }
}
