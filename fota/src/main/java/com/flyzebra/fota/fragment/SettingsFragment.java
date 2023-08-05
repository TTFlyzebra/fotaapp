package com.flyzebra.fota.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.flyzebra.fota.Config;
import com.flyzebra.fota.R;
import com.flyzebra.utils.PropUtil;
import com.flyzebra.utils.SPUtil;

public class SettingsFragment extends Fragment {
    private RadioButton rb_upck_auto, rb_upck_hand;
    private RadioButton rb_upok_restart, rb_upok_normal;
    private EditText et01;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        rb_upck_auto = view.findViewById(R.id.fm_set_upck_auto);
        rb_upck_hand = view.findViewById(R.id.fm_set_upck_hand);
        rb_upok_restart = view.findViewById(R.id.fm_set_upok_restart);
        rb_upok_normal = view.findViewById(R.id.fm_set_upok_normal);
        et01 = view.findViewById(R.id.fm_set_et01);

        String upck_model =  (String) SPUtil.get(getActivity(), Config.UPCK_MODEL,Config.UPCK_HAND);
        if(upck_model.equals(Config.UPCK_HAND)){
            rb_upck_auto.setChecked(false);
            rb_upck_hand.setChecked(true);
        }else {
            rb_upck_auto.setChecked(true);
            rb_upck_hand.setChecked(false);
        }

        rb_upck_auto.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) {
                SPUtil.set(getActivity(),Config.UPCK_MODEL,Config.UPCK_AUTO);
            }
        });
        rb_upck_hand.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                SPUtil.set(getActivity(),Config.UPCK_MODEL,Config.UPCK_HAND);
            }
        });

        String upok_model =  (String) SPUtil.get(getActivity(), Config.UPOK_MODEL,Config.UPOK_NORMAL);
        if(upok_model.equals(Config.UPOK_NORMAL)){
            rb_upok_restart.setChecked(false);
            rb_upok_normal.setChecked(true);
        }else {
            rb_upok_restart.setChecked(true);
            rb_upok_normal.setChecked(false);
        }

        rb_upok_restart.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) {
                SPUtil.set(getActivity(),Config.UPOK_MODEL,Config.UPOK_RESTART);
            }
        });
        rb_upok_normal.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                SPUtil.set(getActivity(),Config.UPOK_MODEL,Config.UPOK_NORMAL);
            }
        });

        et01.setText(PropUtil.get(Config.PROP_API_BASE_URL, ""));
    }

    @Override
    public void onStop() {
        super.onStop();
        PropUtil.set(Config.PROP_API_BASE_URL, et01.getText().toString());
    }
}
