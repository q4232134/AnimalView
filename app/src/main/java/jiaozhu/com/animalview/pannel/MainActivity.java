package jiaozhu.com.animalview.pannel;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.pannel.Adapter.FileAdapter;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;

public class MainActivity extends AppCompatActivity implements SelectorRecyclerAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private File rootFile = Constants.ROOT_DIR;
    private List<FileModel> list = new ArrayList<>();
    private FileAdapter adapter;
    private Stack<File> stack = new Stack<>();//路径堆栈
    private List<FileModel> commList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upDir();
            }
        });

        initData();

    }

    private void initData() {
        commList = Preferences.list;
        stack.push(rootFile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //androidL以下需要在这里设置
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new FileAdapter(list, this);
        adapter.setOnItemClickListener(this);
//        adapter.setSelectorMode(adapter.MODE_MULTI);
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
        list.clear();
        File file = stack.peek();
        if (!file.exists()) {
            Toast.makeText(this, "目录不存在", Toast.LENGTH_SHORT).show();
        }
        File historyFile = Preferences.getInstance().getHistoryFile();
        commList.clear();
        File[] tempList = file.listFiles();
        if (tempList != null) {
            for (File temp : tempList) {
                if (temp.getName().startsWith(".")) continue;
                FileModel model = new FileModel();
                model.setFile(temp);
                model.setStatus(getFileStatus(temp));
                if (model.getStatus() == FileModel.STATUS_ZIP) {
                    continue;
                }
                //在历史文件路径中的file全部标示
                if (historyFile.getPath().startsWith(temp.getPath())) {
                    model.setHistory(true);
                }
                if (model.getStatus() == FileModel.STATUS_SHOW || model.getStatus() == FileModel.STATUS_ZIP) {
                    commList.add(model);
                }
                //只显示符合要求的文件或目录
                if (model.getStatus() != FileModel.STATUS_OTHER)
                    list.add(model);
            }
        }
        Collections.sort(list, new Comparator<FileModel>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(FileModel m1, FileModel m2) {
                return Byte.compare(m2.getStatus(), m1.getStatus());
            }
        });
        adapter.notifyDataSetChanged();
    }

    /**
     * 目录状态
     *
     * @param file
     * @return
     */
    private byte getFileStatus(File file) {
        if (file.isFile()) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
                return FileModel.STATUS_ZIP;
            }
            return FileModel.STATUS_OTHER;
        } else {
            File[] files = file.listFiles();
            if (files.length == 0) return FileModel.STATUS_EMPTY;
            for (File temp : files) {
                if (temp.isDirectory()) {
                    return FileModel.STATUS_OPEN;
                }
            }
            return FileModel.STATUS_SHOW;
        }
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
                //如果有历史记录则进行载入
                if (model.isHistory()) {
                    i.putExtra(AnimalActivity.PAGE_NUM, Preferences.getInstance().getHistoryPage());
                }
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
