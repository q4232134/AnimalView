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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.SelectorRecyclerAdapter;
import jiaozhu.com.animalview.dao.FileModelDao;
import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.pannel.Adapter.FileAdapter;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;
import jiaozhu.com.animalview.support.Tools;

public class MainActivity extends AppCompatActivity implements SelectorRecyclerAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private File rootFile = Constants.ROOT_DIR;
    private List<FileModel> list = new ArrayList<>();
    private FileAdapter adapter;
    private Stack<File> stack = new Stack<>();//路径堆栈
    private List<FileModel> commList;
    private ProgressDialog dialog;
    private Toolbar toolbar;

    private Comparator<FileModel> comparable = new Comparator<FileModel>() {
        @Override
        public int compare(FileModel m1, FileModel m2) {
            return compareInt(m2.getStatus(), m1.getStatus());
        }
    };

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
     * 显示服务选择对话框
     */
    private void showChoiceDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("选择需要连接的地址");
        builder.setSingleChoiceItems(ips.toArray(new String[ips.size()]), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", "guest", "");
                testConnect(ips.get(which), auth, dialog);
            }
        });
        builder.setNeutralButton("刷新列表", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchServer(new Runnable() {
                    @Override
                    public void run() {
                        showChoiceDialog();
                    }
                });
            }
        });
        builder.create().show();
    }


    /**
     * 测试连接
     *
     * @param ip
     * @param dialog
     */
    private void testConnect(final String ip, final NtlmPasswordAuthentication auth, final DialogInterface dialog) {
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            SmbFile f;
            int flag = -1;//-1 无法连接,0 未知用户,1 连接成功

            @Override
            public void runnable() {
                try {
                    f = new SmbFile(getSmbPath(ip), auth);
                    f.connect();
                    flag = 1;
                } catch (SmbAuthException e) {
                    flag = 0;
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBackgroundFinished() {
                switch (flag) {
                    case -1:
                        Toast.makeText(MainActivity.this, "无法连接服务", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        Toast.makeText(MainActivity.this, "无效的用户名或密码", Toast.LENGTH_SHORT).show();
                        showAuthDialog(ip);
                        break;
                    case 1:
                        toSmbActivity(ip);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    List<String> ips = new ArrayList<>();//可用服务列表
    int taskNum = 256;

    /**
     * 搜寻可用服务器
     *
     * @param onFinish
     */
    private void searchServer(final Runnable onFinish) {
        final String ipStr = Tools.getLocalIpAddress(this);
        if (ipStr != null) {
            final byte[] ip = Tools.string2Ip(ipStr);
            if (ip == null) return;
            taskNum = 256;
            ips.clear();
            dialog.setTitle("正在搜索可用服务");
            dialog.show();
            for (int i = 0; i < 256; i++) {
                ip[3] = (byte) i;
                BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
                    boolean status;
                    final byte[] tempIp = ip.clone();

                    @Override
                    public void runnable() {
                        //能够ping通并且支持smb的标记状态为true
                        if (Tools.pingIP(tempIp)) {
                            try {
                                SmbFile f = new SmbFile(getSmbPath(Tools.ip2String(tempIp)));
                                f.setConnectTimeout(1000);
                                f.connect();
                                status = true;
                            } catch (SmbAuthException e) {
                                //用户名密码错误
                                status = true;
                            } catch (MalformedURLException e) {
                            } catch (IOException e) {
                            }
                        }
                    }

                    @Override
                    public void onBackgroundFinished() {
                        --taskNum;
                        if (status) {
                            ips.add(Tools.ip2String(tempIp));
                        }
                        if (taskNum == 0) {
                            //忽略本机IP
                            for (String temp : ips) {
                                if (temp.equals(ipStr)) {
                                    ips.remove(temp);
                                    break;
                                }
                            }
                            Collections.sort(ips, new Comparator<String>() {
                                @Override
                                public int compare(String lhs, String rhs) {
                                    int l = Tools.string2Ip(lhs)[3];
                                    int r = Tools.string2Ip(rhs)[3];
                                    return l < r ? -1 : (l == r ? 0 : 1);
                                }
                            });
                            dialog.dismiss();
                            onFinish.run();
                        }
                    }
                }, false);
            }
        }
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

    private void showAuthDialog(final String ip) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(ip);
        View view = LayoutInflater.from(this).inflate(R.layout.view_auth_input, null);
        final EditText mName = (EditText) view.findViewById(R.id.name);
        final EditText mPw = (EditText) view.findViewById(R.id.password);
        builder.setView(view);
        builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NtlmPasswordAuthentication auth =
                        new NtlmPasswordAuthentication("", mName.getText().toString(), mPw.getText().toString());
                testConnect(ip, auth, dialog);
            }
        });
        builder.create().show();
    }

    private void toSmbActivity(String ip) {
        Intent i = new Intent();
        i.putExtra(SmbActivity.SERVER_IP, ip);
        i.setClass(this, SmbActivity.class);
        startActivity(i);
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
                Map<String, FileModel> newMap = getDirList(rootFile);
                Map<String, FileModel> oldMap = FileModelDao.getInstance().getModelsByNames(newMap.keySet());
                for (Map.Entry<String, FileModel> entry : newMap.entrySet()) {
                    FileModel temp = oldMap.get(entry.getKey());
                    if (temp != null) {
                        entry.getValue().setLastPage(temp.getLastPage());
                    }
                }
                FileModelDao.getInstance().replace(new ArrayList<>(newMap.values()));
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
        List<FileModel> list = FileModelDao.getInstance()
                .getModelByTime(System.currentTimeMillis() - Constants.HISTORY_DURATION);
        for (FileModel model : list) {
            if (!model.getFile().exists()) {
                FileModelDao.getInstance().delete(model.getName());
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
            case R.id.action_settings:
                toSettingActivity();
                break;
            case R.id.action_choice:
                onChoiceClick();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 单击选择按钮
     */
    private void onChoiceClick() {
        final Runnable showChoiceRunnable = new Runnable() {
            @Override
            public void run() {
                showChoiceDialog();
            }
        };
        if (ips.isEmpty()) {
            searchServer(showChoiceRunnable);
        } else {
            showChoiceRunnable.run();
        }
    }

    private void toSettingActivity() {
        Intent i = new Intent();
        i.setClass(this, SettingsActivity.class);
        startActivity(i);
    }

    /**
     * 刷新
     */
    void fresh() {
        final File file = stack.peek();
        setTitle(file.getName());
        if (!file.exists()) {
            Toast.makeText(this, "目录不存在", Toast.LENGTH_SHORT).show();
        }
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            List<FileModel> tempList = new ArrayList<FileModel>();

            @Override
            public void runnable() {
                File historyFile = Preferences.getInstance().getHistoryFile();
                commList.clear();
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        //是否为隐藏文件
                        if (file.getName().startsWith(".") || file.isHidden())
                            return false;
                        //是否为压缩文件
                        if (file.isFile() && !Tools.isZipFile(file))
                            return false;
                        return true;
                    }
                });
                if (files != null) {
                    Map<String, FileModel> models = FileModelDao.getInstance().getModelsByFiles(Arrays.asList(files));
                    for (File temp : files) {
                        FileModel model = models.get(temp.getName());
                        //数据库不存在则创建model并存入数据库
                        if (model == null) {
                            model = new FileModel();
                            model.setFile(temp);
                            model.getStatus();
                            FileModelDao.getInstance().replace(model);
                        }
                        //在历史文件路径中的file全部标示
                        if ((historyFile.getPath() + "/").startsWith(model.getPath() + "/")) {
                            model.setHistory(true);
                        }
                        //判断是否需要显示在目录与是否能够打开
                        if (model.showInList()) {
                            tempList.add(model);
                            if (model.animalAble()) {
                                commList.add(model);
                            }
                        }
                    }
                }
                Collections.sort(tempList, comparable);
                Collections.sort(commList, comparable);
            }

            @Override
            public void onBackgroundFinished() {
                list.clear();
                list.addAll(tempList);
                adapter.notifyDataSetChanged();
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
            case FileModel.STATUS_ZIP:
            case FileModel.STATUS_SHOW:
                Intent i = new Intent();
                i.setClass(this, AnimalActivity.class);
                i.putExtra(AnimalActivity.PARAM_PATH, model.getPath());
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


    /**
     * 获取目录列表，并完成状态标记
     *
     * @param file
     * @return
     */
    public static Map<String, FileModel> getDirList(File file) {
        Map<String, FileModel> map = new HashMap<>();
        //如果为压缩文件
        if (file.isFile() && Tools.isZipFile(file)) {
            FileModel model = new FileModel();
            model.setFile(file);
            model.setStatus(FileModel.STATUS_ZIP);
            map.put(file.getName(), model);
            return map;
        }
        //如果为目录
        if (file.isDirectory()) {
            int status = FileModel.STATUS_EMPTY;
            for (File temp : file.listFiles(Tools.imageFilter)) {
                if (temp.isDirectory()) {
                    status = FileModel.STATUS_OPEN;
                    map.putAll(getDirList(temp));
                } else {
                    //是否为压缩文件
                    if (Tools.isZipFile(temp)) {
                        status = FileModel.STATUS_OPEN;
                        map.putAll(getDirList(temp));
                    }
                    if (status == FileModel.STATUS_EMPTY)
                        status = FileModel.STATUS_SHOW;
                }
            }
            FileModel model = new FileModel();
            model.setFile(file);
            model.setStatus(status);
            map.put(file.getName(), model);
        }
        return map;
    }


}
