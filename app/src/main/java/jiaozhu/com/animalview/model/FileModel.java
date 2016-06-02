package jiaozhu.com.animalview.model;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.tgb.lk.ahibernate.annotation.Column;
import com.tgb.lk.ahibernate.annotation.Id;
import com.tgb.lk.ahibernate.annotation.Table;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jcifs.smb.SmbFile;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Tools;

/**
 * Created by jiaozhu on 16/4/14.
 */
@Table(name = "animal_table")
public class FileModel {
    public static final String TABLE_NAME = "animal_table";
    public static final byte STATUS_NO_CHECK = 0;//未检查目录
    public static final byte STATUS_EMPTY = 1;//空目录
    public static final byte STATUS_SHOW = 2;//可用阅读器打开
    public static final byte STATUS_ZIP = 3;//压缩文档
    public static final byte STATUS_OPEN = 4;//存在子目录
    public static final byte STATUS_OTHER = 5;//未知文档
    public static final byte STATUS_SMB = 6;//远程目录

    private boolean isSmbFile = false;//是否为远程文件
    private SmbFile smbFile;//远程文件

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

    public boolean isSmbFile() {
        return isSmbFile;
    }

    public void setSmbFile(boolean smbFile) {
        isSmbFile = smbFile;
    }

    public SmbFile getSmbFile() {
        return smbFile;
    }

    public void setSmbFile(SmbFile smbFile) {
        this.smbFile = smbFile;
    }

    public File getFile() {
        if (isSmbFile) return null;
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

    /**
     * 是否能够使用阅读器打开
     *
     * @return
     */
    public boolean isAnimal() {
        return getStatus() == STATUS_SHOW || getStatus() == STATUS_ZIP;
    }

    /**
     * 由model判断图片的加载方式并进行加载
     *
     * @param view
     */
    public void setImageView(final ImageView view) {
        //缓存图片是否存在
        BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
            Bitmap bm;

            @Override
            public void runnable() {
                bm = getCacheImage();
            }

            @Override
            public void onBackgroundFinished() {
                view.setImageBitmap(bm);
            }
        });
    }

    /**
     * 获取cache路径
     *
     * @return
     */
    public File getCacheFile() {
        String name = Tools.md516(path) + ".cache";
        return new File(Constants.CACHE_DIR + File.separator + name);
    }

    /**
     * 获取缓存图片
     *
     * @return
     */
    public Bitmap getCacheImage() {
        if (isSmbFile) return null;
        if (!isAnimal()) return null;
        Bitmap bm;
        File file = getCacheFile();
        if (file.exists()) {
            bm = Tools.getBitmap(file.getPath());
        } else {
            bm = createCache();
        }
        return bm;
    }

    @Nullable
    private Bitmap createCache() {
        if (getStatus() == STATUS_ZIP) {
            try {
                ZipFile zipFile = new ZipFile(file);
                Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.entries();
                ZipEntry entry = null;
                //最多读取10个文件
                for (int i = 0; i < 10; i++) {
                    if (!enu.hasMoreElements()) break;
                    ZipEntry temp = enu.nextElement();
                    if (Tools.isImageFile(temp.getName())) {
                        entry = temp;
                        break;
                    }
                }
                if (entry != null) {
                    try {
                        ZipFile file = new ZipFile(getFile());
                        Bitmap temp = Tools.getBitmapByZip(file, entry);
                        Bitmap bm = Tools.resizeImage(temp, Constants.CACHE_WIDTH, Constants.CACHE_HEIGHT);
                        Tools.saveBitmap(bm, getCacheFile());
                        return bm;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getStatus() == STATUS_SHOW) {
            File[] files = getFile().listFiles(new FilenameFilter() {
                boolean returned = false;

                @Override
                public boolean accept(File dir, String filename) {
                    //最多只返回一个文件
                    for (String temp : Constants.IMAGE_TYPE) {
                        if (!returned && filename.toLowerCase().endsWith(temp)) {
                            returned = true;
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (files == null || files.length < 1) return null;
            Bitmap bm = Tools.getImageThumbnail(files[0].getPath(), Constants.CACHE_WIDTH, Constants.CACHE_HEIGHT);
            Tools.saveBitmap(bm, getCacheFile());
            return bm;
        }
        return null;
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
        if (isSmbFile) return STATUS_SMB;
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

    /**
     * 是否在列表显示
     *
     * @return
     */
    public boolean showInList() {
        if (animalAble()) return true;
        return getStatus() == STATUS_OPEN;
    }

    /**
     * 是否能够当做漫画打开
     *
     * @return
     */
    public boolean animalAble() {
        return getStatus() == STATUS_ZIP || getStatus() == STATUS_SHOW;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileModel fileModel = (FileModel) o;

        return path != null ? path.equals(fileModel.path) : fileModel.path == null;

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
