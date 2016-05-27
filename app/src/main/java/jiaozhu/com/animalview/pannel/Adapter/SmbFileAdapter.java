package jiaozhu.com.animalview.pannel.Adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jcifs.smb.SmbFile;
import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;

/**
 * Created by jiaozhu on 16/5/26.
 */
public class SmbFileAdapter extends SelectorRecyclerAdapter<SmbFileAdapter.ViewHolder> {
    List<SmbFile> list;
    private Resources resource;

    public SmbFileAdapter(List<SmbFile> files, Context context) {
        list = files;
        resource = context.getResources();
    }

    @Override
    protected ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_list, parent, false);
        final ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindView(ViewHolder holder, int position, boolean isSelected) {
        final SmbFile file = list.get(position);
        holder.mTitle.setText(file.getName());
        //是否被选择
        if (isSelected) {
            holder.mSelectView.setVisibility(View.VISIBLE);
            holder.mView.setBackgroundColor(resource.getColor(R.color.main_item_selected_bg));
        } else {
            holder.mSelectView.setVisibility(View.GONE);
            holder.mView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView, mSelectView, mPoint;
        public TextView mTitle, mNewMark;
        public ImageView mImage;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mSelectView = v.findViewById(R.id.selectView);
            mTitle = (TextView) v.findViewById(R.id.title);
            mPoint = v.findViewById(R.id.point);
            mNewMark = (TextView) v.findViewById(R.id.newMark);
            mImage = (ImageView) v.findViewById(R.id.image);
        }

    }
}
