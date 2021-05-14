package com.flyzebra.fota;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.model.Flyup;
import com.flyzebra.fota.model.IFlyCode;
import com.flyzebra.fota.model.IFlyup;
import com.flyzebra.utils.IDUtils;

public class MainFragment extends Fragment implements View.OnClickListener,IFlyup.FlyupResult, IFlyCode {


    private TextView tv_version, tv_verinfo, tv_upinfo;
    private ProgressBar progressBar;
    private Button bt_updater;
    private StringBuffer verinfo = new StringBuffer();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        tv_version = view.findViewById(R.id.tv_version);
        tv_verinfo = view.findViewById(R.id.tv_verinfo);
        tv_upinfo = view.findViewById(R.id.tv_upinfo);
        progressBar = view.findViewById(R.id.ac_pbar);
        bt_updater = view.findViewById(R.id.bt_updater);
        bt_updater.setOnClickListener(this);
        progressBar.setMax(100);
        tv_version.setText("当前版本：\n" + IDUtils.getVersion(getActivity()) + "\n"
                + "IMEI:" + IDUtils.getIMEI(getActivity()) + "\n"
                + "MYID:" + IDUtils.getAndroidID(getActivity()) + "\n"
        );

        upVersionInfo();
        Flyup.getInstance().addListener(this);
        upVesionProgress(Flyup.getInstance().getLastCode(), Flyup.getInstance().getLastProgress(), Flyup.getInstance().getLastMessage());
    }

    private void upVersionInfo() {
        OtaPackage otaPackage = Flyup.getInstance().getOtaPackage();
        if (otaPackage != null && otaPackage.version != null) {
            verinfo.delete(0, verinfo.length());
            verinfo.append("最新版本：\n")
                    .append(otaPackage.version).append("\n");
            if (otaPackage.filesize > 0) {
                verinfo.append("文件大小").append(otaPackage.filesize / 1024 / 1024).append("M --- ")
                        .append(otaPackage.otaType == 0 ? "全量升级包" : "增量升级包").append("\n\n")
                        .append("发布说明：\n")
                        .append("").append(otaPackage.releaseNote);
            }
        }
        tv_verinfo.setText(verinfo.toString());
    }

    @Override
    public void upVesionProgress(int code, int progress, String msg) {
        tv_upinfo.setText(msg);
        progressBar.setProgress(progress);
        switch (code) {
            //已是最新版本
            case CODE_01:
                upVersionInfo();
                break;
            //获取到最新版本
            case CODE_02:
                upVersionInfo();
                break;
            //获取最新版本失败
            case CODE_03:
                upVersionInfo();
                break;
            //获取最新版本失败，网络错误！
            case CODE_04:
                break;
            //正在下载升级包...
            case CODE_05:
                break;
            //下载升级包出错!
            case CODE_06:
                break;
            //正在校验升级包MD5值...
            case CODE_07:
                break;
            //升级包MD5值校验错误!
            case CODE_08:
                break;
            //升级包数据校验...
            case CODE_09:
                break;
            //准备安装升级包...
            case CODE_10:
                break;
            //升级包数据校验错误!
            case CODE_11:
                break;
            //安装升级包错误!
            case CODE_12:
                break;
            //系统正在更新
            case CODE_91:
                break;
            //需要手动更新版本
            case CODE_92:
                break;
        }
    }

    @Override
    public void onDestroy() {
        Flyup.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_updater:
                Flyup.getInstance().updaterOtaPackage(Flyup.getInstance().getOtaPackage());
                break;
        }
    }
}
