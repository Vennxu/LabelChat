package com.ekuater.labelchat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class SelectRegionByListActivity extends BackIconActivity {

    private static final String TAG = SelectRegionByListActivity.class.getSimpleName();

    private ListView mProvinceListView;
    private ListView mCityListView;
    private String mSelectedProvince;
    private String mSelectedCity;
    private int[] mCities;

    private final AdapterView.OnItemClickListener mProvinceClickListener
            = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectProvince(position);
        }
    };

    private final AdapterView.OnItemClickListener mCityClickListener
            = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedCity = mCityListView.getAdapter().getItem(position).toString();
            finishSelect();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_region_by_list);
        mCities = getCityArrays();
        if (getResources().getStringArray(R.array.provinces).length == mCities.length) {
            initListViews();
        } else {
            finish();
            L.w(TAG, "onCreate(), province city not match, finish.");
        }
    }

    private void initListViews() {
        mProvinceListView = (ListView) findViewById(R.id.province_list);
        mCityListView = (ListView) findViewById(R.id.city_list);
        mProvinceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mProvinceListView.setOnItemClickListener(mProvinceClickListener);
        mCityListView.setOnItemClickListener(mCityClickListener);
        mProvinceListView.setAdapter(new ProvinceAdapter(this));
        selectProvince(0);
    }

    private void bindCityData(ListView listView, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, R.layout.city_item);
        listView.setAdapter(adapter);
    }

    private int[] getCityArrays() {
        final TypedArray ar = getResources().obtainTypedArray(R.array.province_cities);
        final int length = ar.length();
        final int[] array = new int[length];

        for (int i = 0; i < length; ++i) {
            array[i] = ar.getResourceId(i, 0);
        }

        return array;
    }

    private void finishSelect() {
        if (!TextUtils.isEmpty(mSelectedProvince)
                && !TextUtils.isEmpty(mSelectedCity)) {
            Intent intent = new Intent();
            intent.putExtra("info", mSelectedProvince + "-" + mSelectedCity);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void selectProvince(int position) {
        ProvinceAdapter adapter = (ProvinceAdapter) mProvinceListView.getAdapter();
        mSelectedProvince = adapter.getItem(position).toString();
        adapter.setClickPosition(position);
        bindCityData(mCityListView, mCities[position]);
    }

    private static class ProvinceAdapter extends BaseAdapter {

        private CharSequence[] mProvinces;
        private LayoutInflater mInflater;
        private int mClickedPosition;
        private int mClickedColor;
        private int mNormalColor;

        public ProvinceAdapter(Context context) {
            Resources resources = context.getResources();
            mProvinces = resources.getTextArray(R.array.provinces);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mClickedPosition = -1;
            mClickedColor = resources.getColor(R.color.colorRemarks);
            mNormalColor = resources.getColor(R.color.transparent);
        }

        public void setClickPosition(int position) {
            mClickedPosition = position;
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mProvinces.length;
        }

        @Override
        public CharSequence getItem(int position) {
            return mProvinces[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = mInflater.inflate(R.layout.province_item, parent, false);
            }
            TextView textView = (TextView) view;
            textView.setText(getItem(position));
            if (position == mClickedPosition) {
                textView.setBackgroundColor(mClickedColor);
            } else {
                textView.setBackgroundColor(mNormalColor);
            }
            return view;
        }
    }
}
