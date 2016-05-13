package jiaozhu.com.animalview.pannel;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.dao.FileDao;
import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.pannel.Adapter.FileAdapter;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;
import jiaozhu.com.animalview.support.Tools;

public class MainActivity extends AppCompatActivity implements SelectorRecyclerAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private File rootFile = Constants.ROOT_DIR;
    private List<FileModel> list = new ArrayList<>();
    private FileAdapter adapter;
    private Stack<File> stack = new Stack<>();//路径堆栈
    private List<FileModel> commList;
    private ProgressDialog dialog;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInitDialog();
            }
        });
        initProgressDialog();
        initData();
        //数据库是否存在
        if (getDatabasePath(Constants.DB_NAME).exists()) {
            deleteUnExistData();
        } else {
            initFileStatus();
        }
    }

    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("正在初始化目录结构");
        dialog.setCancelable(false);
    }

    private void showInitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("是否刷新目录状态");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initFileStatus();
            }
        });
        builder.create().show();
    }


    /**
     * 显示删除对话框
     *
     * @param list
     */
    private void showDeleteDialog(final List<FileModel> list) {
        if (list.isEmpty()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Constants.DELETE_MESSAGE_LENGTH && i < list.size(); i++) {
            sb.append(list.get(i).getFile().getName()).append("\n");
        }
        if (list.size() > Constants.DELETE_MESSAGE_LENGTH) {
            sb.append("...");
        }
        builder.setTitle("确认删除以下目录:");
        builder.setMessage(sb);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteSelectFile(list);
            }
        });
        builder.create().show();
    }

    /**
     * 删除指定文件
     *
     * @param models
     */
    private void deleteSelectFile(final Collection<FileModel> models) {
        dialog.setTitle("正在删除指定目录");
        dialog.show();
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            @Override
            public void runnable() {
                for (FileModel temp : models) {
                    Tools.deleteDir(temp.getFile());
                }
            }

            @Override
            public void onBackgroundFinished() {
                Toast.makeText(MainActivity.this, "删除完成", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                fresh();
            }
        });
    }


    /**
     * 初始化目录状态
     * 重新计算目录状态，保留之前历史记录
     */
    private void initFileStatus() {
        dialog.setTitle("正在初始化目录结构");
        dialog.show();
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            @Override
            public void runnable() {
                Map<String, FileModel> newMap = Tools.getDirList(rootFile);
                Map<String, FileModel> oldMap = FileDao.getInstance().getModelsByPaths(newMap.keySet());
                for (Map.Entry<String, FileModel> entry : newMap.entrySet()) {
                    FileModel temp = oldMap.get(entry.getKey());
                    if (temp != null) {
                        entry.getValue().setLastPage(temp.getLastPage());
                    }
                }
                FileDao.getInstance().replace(new ArrayList<>(newMap.values()));
            }

            @Override
            public void onBackgroundFinished() {
                Toast.makeText(MainActivity.this, "数据初始化成功", Toast.LENGTH_SHORT).show();
                fresh();
                dialog.dismiss();
            }
        });
    }

    /**
     * 查询数据库并清除无用数据
     */
    private void deleteUnExistData() {
        List<FileModel> list = FileDao.getInstance()
                .getModelByTime(System.currentTimeMillis() - Constants.HISTORY_DURATION);
        for (FileModel model : list) {
            if (!model.getFile().exists()) {
                FileDao.getInstance().delete(model.getPath());
                //删除缓存文件
                model.getCacheFile().delete();
            }
        }
    }

    private void initData() {
        commList = Preferences.list;
        stack.push(rootFile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //androidL以下需要在这里设置
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new FileAdapter(list, this);
        adapter.setOnItemClickListener(this);
        adapter.setSelectorMode(adapter.MODE_MULTI);
        adapter.setActionView(toolbar, new SelectorRecyclerAdapter.ActionItemClickedListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_action, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_delete:
                        List<FileModel> temps = new ArrayList<FileModel>();
                        for (int position : adapter.getSelectList()) {
                            temps.add(list.get(position));
                        }
                        showDeleteDialog(temps);
                        return true;
                }
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fresh();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                upDir();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    void fresh() {
        final long l = System.currentTimeMillis();
        list.clear();
        final File file = stack.peek();
        if (!file.exists()) {
            Toast.makeText(this, "目录不存在", Toast.LENGTH_SHORT).show();
        }
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            @Override
            public void runnable() {
                File historyFile = Preferences.getInstance().getHistoryFile();
                commList.clear();
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.getName().startsWith(".") || file.isFile() || file.isHidden())
                            return false;
                        return true;
                    }
                });
                if (files != null) {
                    Map<String, FileModel> models = FileDao.getInstance().getModelsByFiles(Arrays.asList(files));
                    for (File temp : files) {
                        FileModel model = models.get(temp.getPath());
                        //数据库不存在则创建model并存入数据库
                        if (model == null) {
                            model = new FileModel();
                            model.setFile(temp);
                            model.getStatus();
                            FileDao.getInstance().replace(model);
                        }
                        //在历史文件路径中的file全部标示
                        if ((historyFile.getPath() + "/").startsWith(model.getPath() + "/")) {
                            model.setHistory(true);
                        }
                        //判断是否需要显示在目录与是否能够打开
                        if (model.showInList()) {
                            list.add(model);
                            if (model.animalAble()) {
                                commList.add(model);
                            }
                        }
                    }
                }
                Collections.sort(list, new Comparator<FileModel>() {
                    @Override
                    public int compare(FileModel m1, FileModel m2) {
                        return compareInt(m2.getStatus(), m1.getStatus());
                    }
                });
            }

            @Override
            public void onBackgroundFinished() {
                adapter.notifyDataSetChanged();
                System.out.println("====================="+(System.currentTimeMillis()-l));
            }
        });
    }

    private static int compareInt(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public void onBackPressed() {
        upDir();
    }

    /**
     * 向上一层目录
     */
    private void upDir() {
        if (stack.size() > 1) {
            stack.pop();
            fresh();
        } else {
            Toast.makeText(this, "已经到了根目录了哦", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        FileModel model = list.get(position);
        switch (model.getStatus()) {
            case FileModel.STATUS_SHOW:
                Intent i = new Intent();
                i.setClass(this, AnimalActivity.class);
                i.putExtra(AnimalActivity.INDEX, commList.indexOf(model));
                startActivity(i);
                break;
            case FileModel.STATUS_OPEN:
                stack.push(model.getFile());
                fresh();
                break;
            case FileModel.STATUS_EMPTY:
                Toast.makeText(this, "这是个空目录哟", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }
}
