package jiaozhu.com.animalview.pannel;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import jcifs.smb.SmbFilenameFilter;
import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.pannel.Adapter.SmbFileAdapter;
import jiaozhu.com.animalview.support.Constants;

public class SmbActivity extends AppCompatActivity implements SelectorRecyclerAdapter.OnItemClickListener {
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        fresh();
    }

    private void fresh() {
        dialog.setTitle("正在获取列表");
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            SmbFile[] files = {};
            boolean flag = false;//获取是否成功

            @Override
            public void runnable() {
                try {
                    SmbFile file = stack.peek();
                    files = file.listFiles(new SmbFilenameFilter() {
                        @Override
                        public boolean accept(SmbFile smbFile, String s) throws SmbException {
                            if (smbFile.isDirectory()) return true;
                            for (String temp : Constants.IMAGE_TYPE) {
                                if (s.endsWith(temp)) return true;
                            }
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
                    list.clear();
                    list.addAll(Arrays.asList(files));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SmbActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                }
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
        stack.push(list.get(position));
        fresh();
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
    public void onBackPressed() {
        upDir();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
