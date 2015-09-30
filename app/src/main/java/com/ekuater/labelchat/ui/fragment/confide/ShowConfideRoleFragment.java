package com.ekuater.labelchat.ui.fragment.confide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConfideRole;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.ACache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/4/7.
 *
 * @author FanChong
 */
public class ShowConfideRoleFragment extends BackIconActivity {


    private static final int QUERY_CONFIDE_ROLE_RESULT = 101;
    private static final String ROLE_ID = "role_id";
    private static final String ROLE_NAME = "role_name";

    private ConfideManager mConfideManager;
    private ListView mListView;
    private ConfideRoleAdapter adapter;
    private AutoCompleteTextView mInputView;
    private List<String> hintMessage;
    private ConfideRole confideRole;
    private ConfideRole[] confideRoles;
    private ArrayAdapter<String> hintAdapter;
    private TextView mSelectedRole;
    private TextView mConfirmSelected;

    JSONArray cache;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QUERY_CONFIDE_ROLE_RESULT:
                    handlerConfideRoleResult((ConfideRole[]) msg.obj);
                    break;
            }
        }
    };

    private void queryConfideRoleResult() {
        mConfideManager.queryRoles(new ConfideManager.RoleObserver() {
            @Override
            public void onQueryResult(int result, ConfideRole[] roles) {
                Message message = mHandler.obtainMessage(QUERY_CONFIDE_ROLE_RESULT, roles);
                mHandler.sendMessage(message);

            }
        });
    }

    private JSONArray jsonArray = new JSONArray();
    private JSONObject jsonObject = new JSONObject();

    private void handlerConfideRoleResult(ConfideRole[] confideRoles) {
        adapter.updateData(confideRoles);
        if (confideRoles != null) {
            for (int i = 0; i < confideRoles.length; i++) {
                hintMessage.add(confideRoles[i].getName());
                try {
                    jsonObject.put(i + ROLE_ID, confideRoles[i].getId());
                    jsonObject.put(i + ROLE_NAME, confideRoles[i].getName());
                    jsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            aCache.put("ConfideRoles", jsonArray, 24 * 60 * 60);
            hintAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hintMessage);
            mInputView.setAdapter(hintAdapter);
        }
    }

    private ACache aCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_show_confide_role_list);
        getActionBar().hide();
        aCache = ACache.get(this);
        mSelectedRole = (TextView) findViewById(R.id.title);
        mSelectedRole.setText("");
        mSelectedRole.setTextSize(20);
        mConfirmSelected = (TextView) findViewById(R.id.right_title);
        mConfirmSelected.setTextColor(getResources().getColor(R.color.colorLabelTextDark));
        mConfirmSelected.setVisibility(View.VISIBLE);
        mConfirmSelected.setEnabled(false);
        mListView = (ListView) findViewById(R.id.confide_role_list);
        mInputView = (AutoCompleteTextView) findViewById(R.id.keyword);
        mConfideManager = ConfideManager.getInstance(this);
        hintMessage = new ArrayList<>();
        adapter = new ConfideRoleAdapter(this);
        cache = aCache.getAsJSONArray("ConfideRoles");
        if (cache != null && cache.length() > 0) {
            confideRoles = new ConfideRole[cache.length()];
            for (int i = 0; i < cache.length(); i++) {
                try {
                    JSONObject object = (JSONObject) cache.get(i);
                    confideRole = new ConfideRole();
                    int id = object.getInt(i + ROLE_ID);
                    String name = object.getString(i + ROLE_NAME);
                    confideRole.setId(id);
                    confideRole.setName(name);
                    confideRoles[i] = confideRole;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (ConfideRole roleName : confideRoles) {
                hintMessage.add(roleName.getName());
            }
            hintAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hintMessage);
            mInputView.setAdapter(hintAdapter);
            adapter.updateData(confideRoles);
        } else {
            queryConfideRoleResult();
        }
        findViewById(R.id.icon).setOnClickListener(onClickListener);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);
        mInputView.setOnFocusChangeListener(onFocusChangeListener);
        mInputView.addTextChangedListener(inputTextChangerListener);
        mSelectedRole.addTextChangedListener(roleTextChangerListener);
        mConfirmSelected.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            String tempData = mSelectedRole.getText().toString();
            if (tempData != null && tempData.length() > 0) {
                String roleName = tempData.substring(1, tempData.length() - 1);
                intent.putExtra("ConfideRole", roleName);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                intent.putExtra("ConfideRole", "");
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };

    private TextWatcher roleTextChangerListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() > 0) {
                mConfirmSelected.setTextColor(getResources().getColor(R.color.white));
            } else {
                mConfirmSelected.setTextColor(getResources().getColor(R.color.colorLabelTextDark));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher inputTextChangerListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != "" && s.length() > 0) {
                mSelectedRole.setText("#" + s + "#");
                mConfirmSelected.setEnabled(true);
            } else {
                mSelectedRole.setText(s);
                mInputView.setHint(getResources().getString(R.string.role_search_hint));
                mConfirmSelected.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mInputView.setHint("");
            }
        }
    };
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ConfideRole confideRole;
            Object object = parent.getItemAtPosition(position);
            if (object instanceof ConfideRole) {
                confideRole = (ConfideRole) object;
                if (confideRole.getName() != null && confideRole.getName().length() > 0) {
                    mSelectedRole.setText("#" + confideRole.getName() + "#");
                    mConfirmSelected.setEnabled(true);
                } else {
                    mSelectedRole.setText(confideRole.getName());
                    mConfirmSelected.setEnabled(false);
                }
            }
        }
    };

    private static class ConfideRoleAdapter extends BaseAdapter {
        private ConfideRole[] mConfideRoles;
        private LayoutInflater mInflater;

        public ConfideRoleAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public synchronized void updateData(ConfideRole[] confideRoles) {
            mConfideRoles = confideRoles;
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return mConfideRoles == null ? 0 : mConfideRoles.length;
        }

        @Override
        public ConfideRole getItem(int position) {
            return mConfideRoles == null ? null : mConfideRoles[position];
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
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = mInflater.inflate(R.layout.fragment_show_confide_role_item, parent, false);
            holder.confideRoleName = (TextView) view.findViewById(R.id.confide_role_name);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            ConfideRole confideRole = mConfideRoles[position];
            holder.confideRoleName.setText(confideRole.getName());
        }

        private static class ViewHolder {
            TextView confideRoleName;
        }
    }

}
