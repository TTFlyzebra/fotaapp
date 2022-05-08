package com.flyzebra.fota.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.flyzebra.fota.MainActivity;
import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.FileInfo;
import com.flyzebra.fota.model.Flyup;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class FileFragment extends Fragment implements FileAdapter.OnItemClick {
    private ListView listView;

    private FileAdapter mAdapter;
    private final List<FileInfo> vFileList = new ArrayList<>();
    private static final String FIRST_DIR = "/data/cache";
    private String lastDir = FIRST_DIR;
    private Stack<String> stackList = new Stack<>();

    private static final HandlerThread mTaskThread = new HandlerThread("fota_filelist");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private View.OnKeyListener backlistener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if (!stackList.empty()) {
                        listFiles(stackList.pop());
                        return true;
                    }
                }
            }
            return false;
        }
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
        listView = view.findViewById(R.id.fm_lv01);
        mAdapter = new FileAdapter(getActivity(), vFileList, R.layout.file_item, this);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        stackList.clear();
        listFiles(FIRST_DIR);
        lastDir = FIRST_DIR;
    }

    @Override
    public void onItemclick(View v) {
        FileInfo fileInfo = (FileInfo) v.getTag();
        if (fileInfo.isDirectory()) {
            stackList.push(lastDir);
            listFiles(fileInfo.fullName);
            lastDir = fileInfo.fullName;
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

    private void listFiles(String filePath) {
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                File sdcard = new File(filePath);
                if (sdcard.isDirectory()) {
                    File[] files = sdcard.listFiles();
                    if (files != null) {
                        final List<FileInfo> tmpList = new ArrayList<>();
                        for (File f : files) {
                            if (f == null) continue;
                            if (f.isDirectory() && !f.getName().equals("recovery")) continue;
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
                        Collections.sort(tmpList, new Comparator<FileInfo>() {
                            @Override
                            public int compare(FileInfo t1, FileInfo t2) {
                                if (t1.type > t2.type) {
                                    return 1;
                                } else if (t1.type < t2.type) {
                                    return -1;
                                } else return Integer.compare(t1.fileName.compareToIgnoreCase(t2.fileName), 0);
                            }
                        });
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                vFileList.clear();
                                vFileList.addAll(tmpList);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }
}
