package com.ekuater.labelchat.ui.fragment.labels;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserLabel;

/**
 * @author LinYong
 */
public class RelativeLabelsFragment extends Fragment {

    public static final String EXTRA_USER_LABEL = "user_label";

    private UserLabel mUserLabel;
    private LabelAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgument();
        mAdapter = new LabelAdapter(getActivity());
        // set the title
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mUserLabel.getName());
        }
        loadRelativeLabels();
    }

    private void parseArgument() {
        Bundle args = getArguments();

        if (args != null) {
            mUserLabel = args.getParcelable(EXTRA_USER_LABEL);
        } else {
            mUserLabel = null;
        }
    }

    private void loadRelativeLabels() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_label_relative, container, false);
        ListView listView = (ListView) view.findViewById(R.id.label_list);
        listView.setAdapter(mAdapter);
        return view;
    }

    private static class LabelAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public LabelAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.relative_label_item, parent, false);
            }
            bindView();
            return convertView;
        }

        private void bindView() {

        }
    }
}
