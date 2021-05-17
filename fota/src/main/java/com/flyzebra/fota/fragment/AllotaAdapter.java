package com.flyzebra.fota.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flyzebra.fota.R;
import com.flyzebra.fota.bean.OtaPackage;

import java.util.List;

public class AllotaAdapter extends BaseAdapter implements OnClickListener {

    private class ViewHolder {
        public TextView tv01 = null;
        public TextView tv02 = null;
        public TextView tv03 = null;
        public ImageButton bt01 = null;
    }

    private List<OtaPackage> vOtaList;
    private int idListview;
    private CallbackOnItemClick mOnItemClick = null;
    private Context mContext;


    public interface CallbackOnItemClick {
        void click(View v);
    }

    public AllotaAdapter(Context context, List<OtaPackage> list, int idListview, CallbackOnItemClick OnItemClick) {
        this.mOnItemClick = OnItemClick;
        this.vOtaList = list;
        this.idListview = idListview;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return vOtaList == null ? 0 : vOtaList.size();
    }

    @Override
    public Object getItem(int position) {
        return vOtaList.get(position);
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
            holder.tv01 = convertView.findViewById(R.id.item_tv01);
            holder.tv02 = convertView.findViewById(R.id.item_tv02);
            holder.tv03 = convertView.findViewById(R.id.item_tv03);
            holder.bt01 = convertView.findViewById(R.id.item_bt01);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OtaPackage otaPackage = vOtaList.get(position);
        holder.tv01.setText(otaPackage.version);
        holder.tv02.setText(otaPackage.otaType == 0 ? "全量包" : "增量包");
        holder.tv03.setText(otaPackage.filesize / 1024 / 1024 + "M");
        holder.bt01.setTag(position);
        holder.bt01.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        mOnItemClick.click(v);
    }


}
