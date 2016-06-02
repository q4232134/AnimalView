package jiaozhu.com.animalview.pannel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.pannel.Adapter.SmbFileAdapter;
import jiaozhu.com.animalview.support.Preferences;


public class SmbActivity extends AppCompatActivity implements SelectorRecyclerAdapter.OnItemClickListener,
        SmbFileAdapter.OnBtnClickListener {
    public static final String SERVER_IP = "ServerIP";
    private SmbFile rootFile;
    private List<SmbFile> list = new ArrayList<>();
    private SmbFileAdapter adapter;
    private Stack<SmbFile> stack = new Stack<>();//路径堆栈
    private ProgressDialog dialog;
    private Toolbar toolbar;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smb);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initProgressDialog();
        try {
            rootFile = new SmbFile(getSmbPath(getIntent().getStringExtra(SERVER_IP)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        stack.push(rootFile);
        initRecycleView();
    }

    private void initRecycleView() {
        recyclerView = (RecyclerView) findViewById(R.id.list);
        adapter = new SmbFileAdapter(list, this);
        adapter.setOnItemClickListener(this);
        adapter.setOnBtnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fresh();
    }

    private void fresh() {
        dialog.setTitle("正在获取列表");
        dialog.show();
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            SmbFile[] files = {};
            boolean flag = false;//获取是否成功

            @Override
            public void runnable() {
                try {
                    SmbFile file = stack.peek();
                    file.setConnectTimeout(1000);
                    files = file.listFiles(new SmbFileFilter() {
                        @Override
                        public boolean accept(SmbFile smbFile) throws SmbException {
                            if (smbFile.getName().endsWith("/")) return true;
                            return false;
                        }
                    });
                    flag = true;
                } catch (SmbException e) {
                    flag = false;
                    e.printStackTrace();
                }
            }

            @Override
            public void onBackgroundFinished() {
                if (flag) {
                    //是否还有子目录
                    if (files.length < 1) {
                        Toast.makeText(SmbActivity.this, "没有下一级目录了哦", Toast.LENGTH_SHORT).show();
                        stack.pop();
                    } else {
                        setTitle(stack.peek().getName());
                        list.clear();
                        list.addAll(Arrays.asList(files));
                    }
                } else {
                    Toast.makeText(SmbActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }


    private void initProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
    }


    /**
     * 根据IP获取smb地址
     *
     * @param ip
     * @return
     */
    private String getSmbPath(String ip) {
        return "smb://" + ip + "/";
    }


    @Override
    public void onItemClick(View view, int position) {
        try {
            if (list.get(position).isDirectory()) {
                stack.push(list.get(position));
                fresh();
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向上一层目录
     */
    private boolean upDir() {
        if (stack.size() > 1) {
            stack.pop();
            fresh();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!upDir()) {
            Toast.makeText(this, "已经到了根目录了哦", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                upDir();
                break;
            case R.id.action_exit:
                finish();
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBtnClick(int position, View view) {
        SmbFile temp = list.get(position);
        try {
            if (temp.listFiles(SmbAnimalActivity.imageFilter).length > 0) {
                Intent i = new Intent();
                Preferences.smbList = list;
                i.setClass(this, SmbAnimalActivity.class);
                i.putExtra(SmbAnimalActivity.PARAM_PATH, list.get(position).getPath());
                startActivity(i);
            } else {
                Toast.makeText(this, "没有图片文件哦", Toast.LENGTH_SHORT).show();
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }

    }
}
