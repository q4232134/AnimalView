package jiaozhu.com.animalview.model;

import java.io.File;

/**
 * Created by jiaozhu on 16/4/14.
 */
public class FileModel {
    private File file;
    private byte status;
    public static final byte STATUS_EMPTY = 0;//空目录
    public static final byte STATUS_SHOW = 1;//可用阅读器打开
    public static final byte STATUS_OPEN = 2;//存在子目录


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }
}
