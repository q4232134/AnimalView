package jiaozhu.com.animalview.model;

import com.tgb.lk.ahibernate.annotation.Column;
import com.tgb.lk.ahibernate.annotation.Id;
import com.tgb.lk.ahibernate.annotation.Table;

import java.io.File;
import java.util.Date;

import jiaozhu.com.animalview.support.Constants;

/**
 * Created by jiaozhu on 16/4/14.
 */
@Table(name = "animal_table")
public class FileModel{
    public static final String TABLE_NAME = "animal_table";
    public static final byte STATUS_NO_CHECK = 0;//未检查目录
    public static final byte STATUS_EMPTY = 1;//空目录
    public static final byte STATUS_SHOW = 2;//可用阅读器打开
    public static final byte STATUS_OPEN = 3;//存在子目录
    public static final byte STATUS_ZIP = 4;//压缩文档
    public static final byte STATUS_OTHER = 5;//未知文档

    @Id
    @Column(name = "path", type = "varchar2")
    private String path;

    private File file;

    @Column(name = "status", type = "integer")
    private int status = STATUS_NO_CHECK;

    @Column(name = "lastPage", type = "integer")
    private int lastPage = -1;//最后阅读页,-1为新记录

    @Column(name = "createTime", type = "date")
    private Date createTime = new Date();//创建时间

    private boolean isHistory = false;//是否为历史记录

    public File getFile() {
        if (file == null)
            file = new File(path);
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        path = file.getPath();
    }

    public int getStatus() {
        if (status == STATUS_NO_CHECK) {
            status = getFileStatus();
        }
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isHistory() {
        return isHistory;
    }

    public void setHistory(boolean history) {
        isHistory = history;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取目录状态
     *
     * @return
     */
    private byte getFileStatus() {
        if (file.isFile()) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
                return STATUS_ZIP;
            }
            return STATUS_OTHER;
        } else {
            File[] files = file.listFiles();
            if (files.length == 0) return STATUS_EMPTY;
            //最多读取10个文件，超过10个无法判断则判断为未知文档
            for (int i = 0; i < 10 && i < files.length; i++) {
                File temp = files[i];
                if (temp.isDirectory()) {
                    return STATUS_OPEN;
                }
                if (isImageType(temp)) {
                    return STATUS_SHOW;
                }
            }
            return STATUS_OTHER;
        }
    }

    private boolean isImageType(File file) {
        String tempName = file.getName().toLowerCase();
        for (String type : Constants.IMAGE_TYPE) {
            if (tempName.endsWith(type)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "path='" + path + '\'' +
                ", file=" + file +
                ", status=" + status +
                ", lastPage=" + lastPage +
                ", createTime=" + createTime +
                ", isHistory=" + isHistory +
                '}';
    }
}
