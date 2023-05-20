package com.flyzebra.fota.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.model.FlyEvent;
import com.flyzebra.fota.model.Flyup;
import com.flyzebra.fota.model.IFlyup;
import com.flyzebra.utils.IDUtil;

public class MainFragment extends Fragment implements View.OnClickListener, IFlyup.FlyupResult, FlyEvent {

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
        tv_version.setText("当前版本：\n" + IDUtil.getVersion(getActivity()) + "\n"
                + "IMEI:" + IDUtil.getIMEI(getActivity()) + "\n"
                + "MYID:" + IDUtil.getAndroidID(getActivity()) + "\n"
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
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //获取到最新版本
            case CODE_02:
                upVersionInfo();
                bt_updater.setText(R.string.updater_system);
                bt_updater.setEnabled(true);
                break;
            //获取最新版本失败
            case CODE_03:
                upVersionInfo();
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //获取最新版本失败，网络错误！
            case CODE_04:
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //正在下载升级包...
            case CODE_05:
                bt_updater.setText(R.string.up_system_running);
                bt_updater.setEnabled(false);
                break;
            //下载升级包出错!
            case CODE_06:
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //正在校验升级包MD5值...
            case CODE_07:
                bt_updater.setText(R.string.up_system_running);
                bt_updater.setEnabled(false);
                break;
            //升级包MD5值校验错误!
            case CODE_08:
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //获取升级参数失败！
            case CODE_09:
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //升级失败！
            case CODE_10:
                bt_updater.setText(R.string.check_system);
                bt_updater.setEnabled(true);
                break;
            //正在升级系统
            case CODE_11:
                bt_updater.setText(R.string.up_system_running);
                bt_updater.setEnabled(false);
                break;
            //系统升级完成，需要重启系统才能生效！
            case CODE_12:
                bt_updater.setText(R.string.restart_system);
                bt_updater.setEnabled(true);
                break;
            //系统正在更新
            case CODE_91:
                bt_updater.setText(R.string.up_system_running);
                bt_updater.setEnabled(false);
                break;
            //需要手动更新版本
            case CODE_92:
                bt_updater.setText(R.string.updater_system);
                bt_updater.setEnabled(true);
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
        if (view.getId() == R.id.bt_updater) {
            if (!Flyup.getInstance().isRunning()) {
                Flyup.getInstance().updaterOtaPackage(Flyup.getInstance().getOtaPackage());
            } else {
                if (!Flyup.getInstance().isFinish()) {
                    Toast.makeText(getActivity(), "已有升级任务正在运行！", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("确定要现在重启系统！");
                    builder.setPositiveButton("确定",
                            (dialog, which) -> {
                                SystemProperties.set("sys.powerctl", "reboot");
                                //Intent reboot = new Intent(Intent.ACTION_REBOOT);
                                //reboot.putExtra("nowait", 1);
                                //reboot.putExtra("interval", 1);
                                //reboot.putExtra("window", 0);
                                //Objects.requireNonNull(getActivity()).sendBroadcast(reboot);
                                dialog.dismiss();
                            });
                    builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
                    builder.show();
                }
            }
        }
    }
}
