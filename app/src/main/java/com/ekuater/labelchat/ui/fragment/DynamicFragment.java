package com.ekuater.labelchat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.widget.LabelFlow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicFragment extends ListFragment {

    private DynamicAdapter mDynamicAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDynamicAdapter = new DynamicAdapter(getActivity(), new Handler());
        loadData();
        setListAdapter(mDynamicAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dynamic_fragment_layout, container, false);
    }

    private void loadData() {
        try {
            JSONObject json = new JSONObject(getString(R.string.today_recommended_sample));
            Stranger[] strangers = ContactCmdUtils.toStrangerArray(
                    json.getJSONArray(CommandFields.Stranger.STRANGERS));
            List<Stranger> strangerList = new ArrayList<Stranger>();

            Collections.addAll(strangerList, strangers);
            mDynamicAdapter.updateStrangers(strangerList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static class DynamicAdapter extends BaseAdapter {

        private final Handler mUiHandler;
        private final LayoutInflater mInflater;
        private final List<String> mOwnerLabels = new ArrayList<String>();
        private List<Stranger> mStrangers = new ArrayList<Stranger>();

        public DynamicAdapter(Context context, Handler uiHandler) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mUiHandler = uiHandler;

            UserLabel[] userLabels = UserLabelManager.getInstance(context).getAllLabels();
            if (userLabels != null) {
                for (UserLabel label : userLabels) {
                    mOwnerLabels.add(label.getName());
                }
            }
        }

        public synchronized void updateStrangers(List<Stranger> strangers) {
            if (strangers != null) {
                mStrangers = strangers;
            } else {
                mStrangers.clear();
            }
            notifyDataSetChangedInUI();
        }

        private void notifyDataSetChangedInUI() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mStrangers.size();
        }

        @Override
        public Stranger getItem(int position) {
            return mStrangers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(convertView, position);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.dynamic_item, parent, false);
            LabelFlow labelFlow
                    = (LabelFlow) view.findViewById(R.id.user_labels);
            labelFlow.setOwnerLabels(mOwnerLabels);
            return view;
        }

        private void bindView(View view, int position) {
            final Stranger stranger = getItem(position);

            TextView nameText = (TextView) view.findViewById(R.id.nickname);
            nameText.setText(stranger.getShowName());
            LabelFlow labelFlow
                    = (LabelFlow) view.findViewById(R.id.user_labels);
            labelFlow.removeAllViews();
            UserLabel[] labels = stranger.getLabels();
            if (labels != null && labels.length > 0) {
                for (UserLabel label : labels) {
                    labelFlow.addLabel(label.getName());
                }
            }
        }
    }
}
