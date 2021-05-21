package com.flyzebra.fota.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.FileInfo;
import com.flyzebra.utils.FlyLog;

import java.util.List;

public class FileAdapter extends BaseAdapter implements OnClickListener {

    private class ViewHolder {
        public LinearLayout ll01 = null;
        public TextView tv01 = null;
        public TextView tv02 = null;
        public ImageView iv01 = null;
    }

    private List<FileInfo> vFileList;
    private int idListview;
    private OnItemClick mOnItemClick = null;
    private Context mContext;


    public interface OnItemClick {
        void onItemclick(View v);
    }

    public FileAdapter(Context context, List<FileInfo> list, int idListview, OnItemClick OnItemClick) {
        this.mOnItemClick = OnItemClick;
        this.vFileList = list;
        this.idListview = idListview;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return vFileList == null ? 0 : vFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return vFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(idListview, null);
            holder.ll01 = convertView.findViewById(R.id.item_ll01);
            holder.tv01 = convertView.findViewById(R.id.item_tv01);
            holder.tv02 = convertView.findViewById(R.id.item_tv02);
            holder.iv01 = convertView.findViewById(R.id.item_iv01);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            FileInfo fileInfo = vFileList.get(position);
            holder.tv01.setText(fileInfo.fileName);
            holder.tv02.setText(fileInfo.otherInfo);
            if (fileInfo.type == 0) {
                holder.iv01.setImageResource(R.drawable.icon_dirg1);
            } else {
                holder.iv01.setImageResource(R.drawable.icon_file1);
            }
            holder.iv01.setTag(fileInfo);
            holder.iv01.setOnClickListener(this);
            holder.ll01.setTag(fileInfo);
            holder.ll01.setOnClickListener(this);
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        mOnItemClick.onItemclick(v);
    }


}
