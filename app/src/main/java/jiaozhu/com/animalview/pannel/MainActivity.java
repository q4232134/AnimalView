package jiaozhu.com.animalview.pannel;

import android.content.Intent;
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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.dao.DBHelper;
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
                initFileStatus();
            }
        });

        //数据库是否存在
        if (getDatabasePath(Constants.DB_NAME).exists()) {
            deleteUnExistData();
        } else {
            initFileStatus();
        }
        initData();

    }

    /**
     * 初始化目录状态
     */
    private void initFileStatus() {
        long t = System.currentTimeMillis();
        ((DBHelper) FileDao.getInstance().getDbHelper()).onUpgrade();
        List<FileModel> tempList = new ArrayList<>(Tools.getDirList(rootFile).values());
        FileDao.getInstance().replace(tempList);
        Toast.makeText(this, "数据初始化成功", Toast.LENGTH_SHORT).show();
        System.out.println(System.currentTimeMillis() - t);
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
                if (historyFile.getPath().startsWith(model.getPath())) {
                    model.setHistory(true);
                }
                if (model.getStatus() == FileModel.STATUS_SHOW || model.getStatus() == FileModel.STATUS_ZIP) {
                    commList.add(model);
                }
                list.add(model);
            }
        }
        Collections.sort(list, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel m1, FileModel m2) {
                return compareInt(m2.getStatus(), m1.getStatus());
            }
        });
        adapter.notifyDataSetChanged();
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
