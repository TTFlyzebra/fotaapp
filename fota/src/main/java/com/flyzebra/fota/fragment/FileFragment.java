package com.flyzebra.fota.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.flyzebra.fota.MainActivity;
import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.FileInfo;
import com.flyzebra.fota.model.Flyup;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileFragment extends Fragment implements FileAdapter.OnItemClick {
    private ListView fm_file_lv01;
    private TextView fm_file_tv01;

    private FileAdapter mAdapter;
    private final List<FileInfo> vFileList = new ArrayList<>();
    private static final String FIRST_DIR = "/sdcard";

    private static final HandlerThread mTaskThread = new HandlerThread("fota_filelist");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private View.OnKeyListener backlistener = (view, i, keyEvent) -> {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (i == KeyEvent.KEYCODE_BACK) {
                String path = fm_file_tv01.getText().toString();
                if (!path.equals(FIRST_DIR)) {
                    int last = path.lastIndexOf('/');
                    String listPath = path.substring(0, last);
                    listFiles(listPath);
                    return true;
                }
            }
        }
        return false;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(backlistener);
        fm_file_lv01 = view.findViewById(R.id.fm_file_lv01);
        fm_file_tv01 = view.findViewById(R.id.fm_file_tv01);
        mAdapter = new FileAdapter(getActivity(), vFileList, R.layout.file_item, this);
        fm_file_lv01.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        listFiles(FIRST_DIR);
    }

    @Override
    public void onItemclick(View v) {
        FileInfo fileInfo = (FileInfo) v.getTag();
        if (fileInfo.isDirectory()) {
            listFiles(fileInfo.fullName);
        } else {
            if (!Flyup.getInstance().isRunning()) {
                Flyup.getInstance().updaterFile(new File(fileInfo.fullName));
                ((MainActivity) getActivity()).replaceFragMent(new MainFragment());
            }else{
                Toast.makeText(getActivity(),"已有升级任务正在运行！",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void listFiles(final String filePath) {
        tHandler.post(() -> {
            File sdcard = new File(filePath);
            if (sdcard.isDirectory()) {
                File[] files = sdcard.listFiles();
                if (files != null) {
                    final List<FileInfo> tmpList = new ArrayList<>();
                    for (File f : files) {
                        if (f == null) continue;
                        if (f.getName().startsWith(".")) continue;
                        //if (f.isDirectory() && !f.getName().equals("update")) continue;
                        if (!f.isDirectory() && !f.getName().toUpperCase().endsWith(".ZIP")) continue;
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.type = f.isDirectory() ? 0 : 1;
                        fileInfo.fileName = f.getName();
                        fileInfo.fullName = f.getAbsolutePath();
                        long time = f.lastModified();
                        String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date(time));
                        if (f.isDirectory()) {
                            fileInfo.otherInfo = "Date:" + ctime;
                        } else {
                            long filesize = f.length();
                            String strfilesize;
                            if ((filesize >> 20) > 0) {
                                strfilesize = (new DecimalFormat(".00").format((float) filesize / 1024 / 1024)) + "M";
                            } else if ((filesize >> 10) > 0) {
                                strfilesize = (new DecimalFormat(".00").format((float) filesize / 1024)) + "KB";
                            } else {
                                strfilesize = filesize + "Byte";
                            }
                            fileInfo.otherInfo = "Date:" + ctime + "  Size:" + strfilesize;
                        }
                        tmpList.add(fileInfo);
                    }
                    Collections.sort(tmpList, (t1, t2) -> {
                        if (t1.type > t2.type) {
                            return 1;
                        } else if (t1.type < t2.type) {
                            return -1;
                        } else return Integer.compare(t1.fileName.compareToIgnoreCase(t2.fileName), 0);
                    });
                    mHandler.post(() -> {
                        vFileList.clear();
                        vFileList.addAll(tmpList);
                        fm_file_tv01.setText(filePath);
                        mAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}
