package jiaozhu.com.animalview.pannel.Adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by jiaozhu on 16/4/14.
 */
public class FileAdapter extends SelectorRecyclerAdapter<FileAdapter.ViewHolder> {
    List<FileModel> list;
    private Resources resource;

    public FileAdapter(List<FileModel> fileModels, Context context) {
        list = fileModels;
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
        FileModel model = list.get(position);
        holder.mTitle.setText(model.getFile().getName());
        if (isSelected) {
            holder.mSelectView.setVisibility(View.VISIBLE);
            holder.mView.setBackgroundColor(resource.getColor(R.color.main_item_selected_bg));
        } else {
            holder.mSelectView.setVisibility(View.GONE);
            holder.mView.setBackground(null);
        }
        if (model.getStatus() == FileModel.STATUS_SHOW) {
            holder.mPoint.setVisibility(View.VISIBLE);
        } else {
            holder.mPoint.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView, mSelectView, mPoint;
        public TextView mTitle;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mSelectView = v.findViewById(R.id.selectView);
            mTitle = (TextView) v.findViewById(R.id.title);
            mPoint = v.findViewById(R.id.point);
        }

    }

}
