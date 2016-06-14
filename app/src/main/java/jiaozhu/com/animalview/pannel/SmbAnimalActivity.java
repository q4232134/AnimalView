package jiaozhu.com.animalview.pannel;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;
import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;
import jiaozhu.com.animalview.support.Tools;

/**
 * Created by jiaozhu on 16/5/30.
 */
public class SmbAnimalActivity extends BaseAnimalActivity<SmbFile, SmbFile> {
    private List<SmbFile> list = Preferences.smbList;
    public static SmbFilenameFilter imageFilter = new SmbFilenameFilter() {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager.setOffscreenPageLimit(3);
        isMultiAdder = true;//使用多线程加载
    }

    @Override
    SmbFile getFileByPath(String path) {
        for (SmbFile temp : list) {
            if (path.equals(temp.getPath())) {
                return temp;
            }
        }
        return null;
    }

    @Override
    SmbFile getNextFile(SmbFile smbFile) {
        int position = list.indexOf(smbFile);
        for (int i = position + 1; i < list.size(); i++) {
            SmbFile temp = list.get(i);
            SmbFile f[] = null;
            try {
                f = temp.listFiles(imageFilter);
            } catch (SmbException e) {
                e.printStackTrace();
            }
            if (f != null && f.length > 0) {
                return temp;
            }
        }
        return null;
    }

    @Override
    SmbFile getPreviousFile(SmbFile smbFile) {
        int position = list.indexOf(smbFile);
        for (int i = position - 1; i > 0; i--) {
            SmbFile temp = list.get(i);
            SmbFile f[] = null;
            try {
                f = temp.listFiles(imageFilter);
            } catch (SmbException e) {
                e.printStackTrace();
            }
            if (f != null && f.length > 0) {
                return temp;
            }
        }
        return null;
    }

    @Override
    List<SmbFile> listFiles(SmbFile smbFile) {
        List<SmbFile> list = new ArrayList<>();
        try {
            list = Arrays.asList(smbFile.listFiles(imageFilter));
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
        try {
            SmbFile temp = getNextFile(smbFile);
            if (temp == null) temp = getPreviousFile(smbFile);
            smbFile.delete();
            return temp;
        } catch (SmbException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.msg_delete_fail, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    int getLastPage(SmbFile smbFile) {
//        FileModel model = FileModelDao.getInstance().get(smbFile.getName());
//        if (model != null) return model.getLastPage();
        return 0;
    }

    @Override
    boolean saveLastPage(SmbFile smbFile, int lastPage) {
//        FileModel model = new FileModel();
//        model.setName(smbFile.getName());
//        model.setStatus(FileModel.STATUS_SMB);
//        model.setLastPage(lastPage);
//        FileModelDao.getInstance().replace(model);
        return false;
    }

}
