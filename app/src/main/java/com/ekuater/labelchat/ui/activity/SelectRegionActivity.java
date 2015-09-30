package com.ekuater.labelchat.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.L;

public class SelectRegionActivity extends BackIconActivity {

    private static final String TAG = SelectRegionActivity.class.getSimpleName();

    private Spinner mProvinceSpinner;
    private Spinner mCitySpinner;
    private String mSelectedProvince;
    private String mSelectedCity;
    private int[] mCities;

    private final OnItemSelectedListener mProvinceSelectedListener
            = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedProvince = mProvinceSpinner.getSelectedItem().toString();
            bindSpinnerData(mCitySpinner, mCities[mProvinceSpinner.getSelectedItemPosition()]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private final OnItemSelectedListener mCitySelectedListener
            = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedCity = mCitySpinner.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_region);
        View cancelBtn = findViewById(R.id.btn_cancel);
        View submitBtn = findViewById(R.id.btn_ok);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("info", mSelectedProvince + "-" + mSelectedCity);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mCities = getCityArrays();
        if (getResources().getStringArray(R.array.provinces).length == mCities.length) {
            initSpinners();
        } else {
            finish();
            L.w(TAG, "onCreate(), province city not match, finish.");
        }
    }

    private void initSpinners() {
        mProvinceSpinner = (Spinner) findViewById(R.id.province_spinner);
        mCitySpinner = (Spinner) findViewById(R.id.city_spinner);
        mProvinceSpinner.setPrompt(getString(R.string.select_province));
        mCitySpinner.setPrompt(getString(R.string.select_city));
        mProvinceSpinner.setOnItemSelectedListener(mProvinceSelectedListener);
        mCitySpinner.setOnItemSelectedListener(mCitySelectedListener);
        bindSpinnerData(mProvinceSpinner, R.array.provinces);
    }

    private void bindSpinnerData(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, R.layout.province_item);
        adapter.setDropDownViewResource(R.layout.province_item);
        spinner.setAdapter(adapter);
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
}
