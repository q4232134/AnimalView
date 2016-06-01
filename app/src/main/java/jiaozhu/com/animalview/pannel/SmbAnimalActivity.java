package jiaozhu.com.animalview.pannel;

import android.graphics.Bitmap;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Tools;

/**
 * Created by jiaozhu on 16/5/30.
 */
public class SmbAnimalActivity extends BaseAnimalActivity<SmbFile> {
    List<SmbFile> list = new ArrayList<>();
    public static SmbFilenameFilter filter = new SmbFilenameFilter() {
        @Override
        public boolean accept(SmbFile smbFile, String s) throws SmbException {
            String tempName = s.toLowerCase();
            for (String type : Constants.IMAGE_TYPE) {
                if (tempName.endsWith(type)) return true;
            }
            return false;
        }
    };

    @Override
    SmbFile getFileByPath(String path) {
        try {
            currentFile = new SmbFile(path);
            return currentFile;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    SmbFile getNextFile(SmbFile smbFile) {
        return null;
    }

    @Override
    SmbFile getPreviousFile(SmbFile smbFile) {
        return null;
    }

    @Override
    List<SmbFile> listFiles(SmbFile smbFile) {
        List<SmbFile> list = new ArrayList<>();
        try {
            list = Arrays.asList(smbFile.listFiles(filter));
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    Bitmap getBitmapByFile(SmbFile smbFile) {
        return Tools.getBitmapBySmb(smbFile.getPath());
    }

    @Override
    String getName(SmbFile smbFile) {
        return smbFile.getName();
    }

    @Override
    SmbFile deleteFile(SmbFile smbFile) {
        return null;
    }

    @Override
    int getLastPage(SmbFile smbFile) {
        return 0;
    }

    @Override
    boolean saveLastPage(SmbFile smbFile, int lastPage) {
        return false;
    }
}
