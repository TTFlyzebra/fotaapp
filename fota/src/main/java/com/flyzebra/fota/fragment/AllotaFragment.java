package com.flyzebra.fota.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.flyzebra.fota.MainActivity;
import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.fota.model.Flyup;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class AllotaFragment extends Fragment implements AllotaAdapter.CallbackOnItemClick {
    private TextView tv_version;
    private ListView listView;

    private AllotaAdapter mAdapter;
    private final List<OtaPackage> vOtaList = new ArrayList<>();

    private ApiAction apiAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allota, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tv_version = view.findViewById(R.id.tv_version);
        tv_version.setText("当前版本：\n" + IDUtils.getVersion(getActivity()) + "\n"
                + "IMEI:" + IDUtils.getIMEI(getActivity()) + "\n"
                + "MYID:" + IDUtils.getAndroidID(getActivity()) + "\n"
        );

        listView = view.findViewById(R.id.fm_lv01);
        mAdapter = new AllotaAdapter(getActivity(), vOtaList, R.layout.allota_item, this);
        listView.setAdapter(mAdapter);

        apiAction = new ApiActionlmpl();
        apiAction.getAllVersion(IDUtils.getModel(getActivity()), IDUtils.getVersion(getActivity()), IDUtils.getIMEI(getActivity()),
                IDUtils.getSnUid(getActivity()), IDUtils.getAndroidID(getActivity()),new Observer<RetAllVersion>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull RetAllVersion allVersion) {
                FlyLog.d("onNext:"+allVersion);
                if(allVersion.data!=null) {
                    vOtaList.clear();
                    vOtaList.addAll(allVersion.data);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                FlyLog.e("onError: " + e);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @Override
    public void click(View v) {
        int pos = (int) v.getTag();
        Flyup.getInstance().updaterOtaPackage(vOtaList.get(pos));
        ((MainActivity)getActivity()).replaceFragMent(new MainFragment());
    }
}
