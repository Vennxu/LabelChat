package com.ekuater.labelchat.ui.fragment.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/2.
 *
 * @author LinYong
 */
public class LabelPlayFragment extends Fragment {

    private static final String FUNCTION_GET_GAME = "get_game";
    private static final String FUNCTION_THROW_PHOTO = "throw_photo";

    private ContactsManager mContactsManager;
    private LabelPlayAdapter mMethodAdapter;

    private final ContactsListItem.FunctionItemListener mFunctionItemListener
            = new ContactsListItem.FunctionItemListener() {
        @Override
        public void onClick(String function) {
            launchFunctionItemUI(function);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        actionBar.hide();


        mContactsManager = ContactsManager.getInstance(activity);
        mMethodAdapter = new LabelPlayAdapter(activity);
        mMethodAdapter.updateItems(newMethodItemList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_label_play, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        title.setText(getResources().getString(R.string.label_play));
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ListView methodListView = (ListView) rootView.findViewById(R.id.method_list);
        methodListView.setAdapter(mMethodAdapter);
        methodListView.setOnItemClickListener(mMethodAdapter);
        return rootView;
    }

    private void launchFunctionItemUI(String function) {
        if (FUNCTION_GET_GAME.equals(function)) {
            if (mContactsManager.isInGuestMode()) {
                UILauncher.launchLoginPromptUI(getFragmentManager());
            } else {
                UILauncher.launchGetViewPager(getActivity());
            }
        } else if (FUNCTION_THROW_PHOTO.equals(function)) {
            UILauncher.launchThrowPhotoUI(getActivity());
        }
    }

    private List<ContactsListItem.Item> newMethodItemList() {
        List<ContactsListItem.Item> items = new ArrayList<ContactsListItem.Item>();

        items.add(new ContactsListItem.FunctionItem(
                getString(R.string.get_game),
                R.drawable.ic_get_get,
                FUNCTION_GET_GAME,
                mFunctionItemListener));
        items.add(new ContactsListItem.FunctionItem(
                getString(R.string.throw_photo),
                R.drawable.ic_photo_throw,
                FUNCTION_THROW_PHOTO,
                mFunctionItemListener));

        return items;
    }

    private static class LabelPlayAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {

        private final LayoutInflater mInflater;
        private List<ContactsListItem.Item> mItemList;

        public LabelPlayAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<ContactsListItem.Item>();
        }

        public synchronized void updateItems(List<ContactsListItem.Item> items) {
            if (items != null) {
                mItemList = items;
            } else {
                mItemList.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public ContactsListItem.Item getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return ContactsListItem.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ContactsListItem.Item item = getItem(position);
            if (view == null) {
                view = item.newView(mInflater, parent);
            }
            item.bindView(view);

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            getItem(position).onClick();
        }
    }
}
