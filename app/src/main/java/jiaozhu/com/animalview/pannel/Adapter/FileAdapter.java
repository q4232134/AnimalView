package jiaozhu.com.animalview.pannel.Adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    public void onBindView(final ViewHolder holder, int position, boolean isSelected) {
        final FileModel model = list.get(position);
        holder.mTitle.setText(model.getFile().getName());
        //高亮历史记录
        if (model.isHistory()) {
            SpannableStringBuilder style = new SpannableStringBuilder(model.getFile().getName());
            style.setSpan(new ForegroundColorSpan(Color.RED), 0, style.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.mTitle.setText(style);
        } else {
            holder.mTitle.setText(model.getFile().getName());
        }
        //是否为新漫画
        if (model.getLastPage() == -1 && model.isAnimal()) {
            holder.mNewMark.setVisibility(View.VISIBLE);
        } else {
            holder.mNewMark.setVisibility(View.GONE);
        }
        //是否被选择
        if (isSelected) {
            holder.mSelectView.setVisibility(View.VISIBLE);
            holder.mView.setBackgroundColor(resource.getColor(R.color.main_item_selected_bg));
        } else {
            holder.mSelectView.setVisibility(View.GONE);
            holder.mView.setBackground(null);
        }
        //是否为可打开漫画
        if (model.isAnimal()) {
            model.setImageView(holder.mImage);
        } else {
            holder.mImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView, mSelectView;
        public TextView mTitle, mNewMark;
        public ImageView mImage;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mSelectView = v.findViewById(R.id.selectView);
            mTitle = (TextView) v.findViewById(R.id.title);
            mNewMark = (TextView) v.findViewById(R.id.newMark);
            mImage = (ImageView) v.findViewById(R.id.image);
        }

    }

}
