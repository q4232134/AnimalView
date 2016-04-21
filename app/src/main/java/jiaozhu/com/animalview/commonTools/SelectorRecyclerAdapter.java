package jiaozhu.com.animalview.commonTools;

import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;

/**
 * Created by jiaozhu on 16/3/22.
 */
public abstract class SelectorRecyclerAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {
    protected boolean isSelectModel = false;
    protected HashSet<Integer> selectSet = new HashSet<>();//被选中条目列表
    private SelectorStatusChangedListener selectorListener;
    private ItemStatusChangedListener itemListener;
    private OnItemClickListener itemClickListener;
    private int lastSelectedItem = 0;//上一个被选中的item(用于单选模式)
    private int selectorMode = 0;
    public static final int MODE_NONE = 0;//普通模式
    public static final int MODE_SINGER = 1;//单选模式
    public static final int MODE_MULTI = 2;//多选模式

    private View actionView;//顶部工具栏
    private ActionItemClickedListener actionItemClickedListener;//菜单单击监听器
    private ActionMode actionMode;

    public int getSelectorMode() {
        return selectorMode;
    }

    public void setSelectorMode(int selectorMode) {
        this.selectorMode = selectorMode;
    }

    public SelectorStatusChangedListener getSelectorListener() {
        return selectorListener;
    }

    public void setSelectorListener(SelectorStatusChangedListener selectorListener) {
        this.selectorListener = selectorListener;
    }

    public ItemStatusChangedListener getItemListener() {
        return itemListener;
    }

    public void setItemListener(ItemStatusChangedListener itemListener) {
        this.itemListener = itemListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return itemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 设置actionMode
     *
     * @param actionView                toolbar或者actionbar
     * @param actionItemClickedListener 回调监听
     */
    public void setActionView(View actionView, ActionItemClickedListener actionItemClickedListener) {
        this.actionView = actionView;
        this.actionItemClickedListener = actionItemClickedListener;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        final T vh = onCreateHolder(parent, viewType);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (isSelectModel) {
                    if (isSelect(position)) {
                        removeSelect(position);
                    } else {
                        setSelect(position);
                    }
                } else {
                    onItemClick(position, v);
                }
            }
        });
        if (selectorMode != MODE_NONE)
            vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (itemSelectable(position)) {
                        startSelectorMode();
                        setSelect(position);
                        return true;
                    }
                    return false;
                }
            });
        return vh;
    }

    @Override
    public void onBindViewHolder(final T holder, int position) {
        onBindView(holder, position, isSelectModel && isSelect(position));
    }

    /**
     * 启动选择模式
     */
    public void startSelectorMode() {
        if (!isSelectModel && selectorMode != MODE_NONE) {
            isSelectModel = true;
            selectSet.clear();//初始化
            if (selectorListener != null) {
                selectorListener.onSelectorStatusChanged(true);
            }
            startActionMode();
        }
    }

    /**
     * 启动action模式
     */
    private void startActionMode() {
        if (actionView != null && actionItemClickedListener != null) {
            actionMode = actionView.startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return actionItemClickedListener.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    boolean flag = actionItemClickedListener.onActionItemClicked(mode, item);
                    if (flag) cancelSelectorMode();
                    return flag;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    cancelSelectorMode();
                }
            });
        }
    }

    /**
     * 取消选择模式
     */
    public void cancelSelectorMode() {
        if (isSelectModel) {
            isSelectModel = false;
            selectSet.clear();
            this.notifyDataSetChanged();
            if (selectorListener != null) {
                selectorListener.onSelectorStatusChanged(false);
            }
            if (actionView != null && actionItemClickedListener != null) {
                actionMode.finish();
            }
        }
    }

    /**
     * 选中条目
     *
     * @param position
     * @return 选中是否成功
     */
    public boolean setSelect(int position) {
        if (!itemSelectable(position)) return false;
        selectSet.add(position);
        if (selectorMode == MODE_SINGER) {
            removeSelect(lastSelectedItem);
            lastSelectedItem = position;
        }
        if (itemListener != null)
            itemListener.onItemStatusChanged(position, true);
        notifyItemChanged(position);
        return true;
    }

    /**
     * 取消条目
     *
     * @param position
     */
    public void removeSelect(int position) {
        selectSet.remove(position);
        if (itemListener != null)
            itemListener.onItemStatusChanged(position, false);
        notifyItemChanged(position);
        if (selectSet.isEmpty()) {
            cancelSelectorMode();
        }
    }

    /**
     * 检查条目是否被选中
     *
     * @param position
     * @return
     */
    public boolean isSelect(int position) {
        return selectSet.contains(position);
    }


    /**
     * 重写此方法决定item是否能够被选中
     *
     * @param position
     * @return
     */
    public boolean itemSelectable(int position) {
        return true;
    }

    /**
     * 获取选中列表
     *
     * @return
     */
    public HashSet<Integer> getSelectList() {
        return selectSet;
    }


    /**
     * 参考 T onCreateViewHolder(ViewGroup parent, int viewType)
     */
    abstract protected T onCreateHolder(ViewGroup parent, int viewType);

    /**
     * 参考 void onBindViewHolder(final T holder, int position)
     *
     * @param isSelected 是否被选中
     */
    abstract public void onBindView(final T holder, int position, boolean isSelected);

    /**
     * 通过重写此方法添加单击监听
     *
     * @param position
     * @param view
     */
    private void onItemClick(int position, View view) {
        itemClickListener.onItemClick(view, position);
    }

    public interface SelectorStatusChangedListener {
        /**
         * 选择器状态改变时调用
         *
         * @param isSelectedMod 是否为选择器状态
         */
        void onSelectorStatusChanged(boolean isSelectedMod);
    }

    public interface ItemStatusChangedListener {
        /**
         * 条目状态改变时调用
         *
         * @param isSelected 是否为被选中状态
         */
        void onItemStatusChanged(int position, boolean isSelected);
    }

    public interface ActionItemClickedListener {
        /**
         * 创建action模式菜单
         *
         * @param menu
         * @return
         */
        boolean onCreateActionMode(ActionMode mode, Menu menu);

        /**
         * action单击监听
         *
         * @param item
         * @return
         */
        boolean onActionItemClicked(ActionMode mode, MenuItem item);

    }

    /**
     * 单击监听
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
