package com.flyzebra.fota.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.flyzebra.fota.R;
import com.flyzebra.fota.config.HttpApi;
import com.flyzebra.utils.SystemPropUtils;

import static com.flyzebra.fota.config.HttpApi.PROP_API_BASE_URL;

public class SettingsFragment extends Fragment {

    private Button bt_save;
    private EditText et_otaurl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bt_save = view.findViewById(R.id.bt_save);
        et_otaurl = view.findViewById(R.id.et_otaurl);

        et_otaurl.setText(SystemPropUtils.get(PROP_API_BASE_URL, HttpApi.API_BASE_URL));

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SystemPropUtils.set(PROP_API_BASE_URL, et_otaurl.getText().toString());
            }
        });
    }
}
