package com.flyzebra.fota.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.flyzebra.fota.R;
import com.flyzebra.fota.config.Config;
import com.flyzebra.utils.PropUtil;
import com.flyzebra.utils.SPUtil;

public class SettingsFragment extends Fragment {
    private RadioGroup rg01;
    private RadioButton rb01, rb02, rb03;
    private EditText et01;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        rg01 = view.findViewById(R.id.fm_set_rg01);
        rb01 = view.findViewById(R.id.fm_set_rb01);
        rb02 = view.findViewById(R.id.fm_set_rb02);
        rb03 = view.findViewById(R.id.fm_set_rb03);
        et01 = view.findViewById(R.id.fm_set_et01);

        String upok_model =  (String) SPUtil.get(getActivity(), Config.UPOK_MODEL,Config.UPOK_MODEL_DIALOG);
        if(upok_model.equals(Config.UPOK_MODEL_DIALOG)){
            rb01.setChecked(true);
            rb02.setChecked(false);
            rb03.setChecked(false);
        }else if(upok_model.equals(Config.UPOK_MODEL_RESTART)){
            rb01.setChecked(false);
            rb02.setChecked(true);
            rb03.setChecked(false);
        }else {
            rb01.setChecked(false);
            rb02.setChecked(false);
            rb03.setChecked(true);
        }

        rb01.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    SPUtil.set(getActivity(),Config.UPOK_MODEL,Config.UPOK_MODEL_DIALOG);
                }
            }
        });
        rb02.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    SPUtil.set(getActivity(),Config.UPOK_MODEL,Config.UPOK_MODEL_RESTART);
                }
            }
        });
        rb03.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtil.set(getActivity(),Config.UPOK_MODEL,Config.UPOK_MODEL_NORMAL);
                }
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
