package com.ekuater.labelchat.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.settings.Settings;

/**
 * Created by Administrator on 2015/4/16.
 *
 * @author FanChong
 */
public class MessageNotifySettingFragment extends Fragment {
    SettingHelper settingHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        settingHelper = SettingHelper.getInstance(getActivity());
    }

    private Switch shakeNotify, voiceNotify;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_notify_setting, container, false);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.message_notify);
        initView(view);
        shakeNotify.setOnCheckedChangeListener(onCheckChangeListener);
        voiceNotify.setOnCheckedChangeListener(onCheckChangeListener);
        shakeNotify.setChecked(settingHelper.getShakeSetting());
        voiceNotify.setChecked(settingHelper.getVoiceSetting());
        return view;
    }

    private void initView(View view) {
        shakeNotify = (Switch) view.findViewById(R.id.message_shake_notify);
        voiceNotify = (Switch) view.findViewById(R.id.message_voice_notify);
    }

    private CompoundButton.OnCheckedChangeListener onCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()) {
                case R.id.message_shake_notify:
                    settingHelper.setShakeSetting(isChecked);
                    break;
                case R.id.message_voice_notify:
                    settingHelper.setVoiceSetting(isChecked);
                    break;

            }

        }
    };

}
