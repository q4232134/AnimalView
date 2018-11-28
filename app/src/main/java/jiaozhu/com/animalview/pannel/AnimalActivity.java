package jiaozhu.com.animalview.pannel;

import android.graphics.Bitmap;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.jiaozhu.ahibernate.util.DaoManager;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jiaozhu.com.animalview.dao.FileModelDao;
import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;
import jiaozhu.com.animalview.support.Tools;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressWarnings("WrongConstant")
public class AnimalActivity extends BaseAnimalActivity<File, AnimalActivity.Entry> {
    List<FileModel> commList = Preferences.list;
    private Archive archive = null;//header必须和archive一一匹配

    @Override
    File getFileByPath(String path) {
        return new File(path);
    }

    @Override
    File getNextFile(File file) {
        int position = indexOfList(file);
        if (position == -1) return null;
        position = position + 1;
        if (position > commList.size() - 1) {
            return null;
        } else {
            return commList.get(position).getFile();
        }
    }

    @Override
    File getPreviousFile(File file) {
        int position = indexOfList(file);
        if (position == -1) return null;
        position = position - 1;
        if (position < 0) {
            return null;
        } else {
            return commList.get(position).getFile();
        }
    }

    @Override
    List<Entry> listFiles(File file) {
        if (Tools.isZipFile(file)) {
            if (Tools.getZipType(file.getName()) == Tools.ZIP_TYPE)
                return listZip(file);
            if (Tools.getZipType(file.getName()) == Tools.RAR_TYPE)
                return listRar(file);
        } else {
            return listDir(file);
        }
        return new ArrayList<>();
    }


    /**
     * 通过RAR文件获取列表
     *
     * @param file
     * @return
     */
    private List<Entry> listRar(File file) {
        List<Entry> list = new ArrayList<>();
        try {
            archive = new Archive(file);
            for (FileHeader header : archive.getFileHeaders()) {
                if (Tools.isImageFile(header.getFileNameString()))
                    list.add(new Entry(header));
            }
            Collections.sort(list, new Comparator<Entry>() {
                @Override
                public int compare(Entry lhs, Entry rhs) {
                    return lhs.rar.getFileNameString().compareTo(rhs.rar.getFileNameString());
                }
            });
        } catch (RarException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 通过ZIP文件获取列表
     *
     * @param file
     * @return
     */
    private List<Entry> listZip(File file) {
        List<Entry> list = new ArrayList<>();
        for (ZipEntry zip : Tools.listZip(file)) {
            if (Tools.isImageFile(zip.getName()))
                list.add(new Entry(zip));
        }
        Collections.sort(list, new Comparator<Entry>() {
            @Override
            public int compare(Entry lhs, Entry rhs) {
                return lhs.zip.getName().compareTo(rhs.zip.getName());
            }
        });
        return list;
    }

    /**
     * 通过文件夹获取列表
     *
     * @param file
     * @return
     */
    private List<Entry> listDir(File file) {
        List<Entry> list = new ArrayList<>();
        for (File temp : file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String tempName = filename.toLowerCase();
                for (String type : Constants.IMAGE_TYPE) {
                    if (tempName.endsWith(type)) return true;
                }
                return false;
            }
        })) {
            list.add(new Entry(temp));
        }
        return list;
    }

    @Override
    Bitmap getBitmapByFile(Entry entry) {
        if (entry.file != null) {
            return Tools.getBitmap(entry.file.getPath());
        }
        if (entry.zip != null) {
            try {
                return Tools.getBitmapByZip(new ZipFile(currentFile), entry.zip);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        if (entry.rar != null) {
            try {
                return Tools.getBitmapByRar(archive, entry.rar);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    String getName(File file) {
        return file.getName();
    }

    @Override
    File deleteFile(File file) {
        File temp = getNextFile(file);
        if (temp == null) temp = getPreviousFile(file);
        Tools.writeFile(Constants.DELETE_LOG, ">>:" + file.getName() + "\n");
        Tools.deleteDir(file);
        return temp;
    }

    @Override
    int getLastPage(File file) {
        return commList.get(indexOfList(file)).getLastPage();
    }

    @Override
    boolean saveLastPage(File file, int lastPage) {
        FileModel temp = commList.get(indexOfList(file));
        temp.setLastPage(lastPage);
        Preferences.getInstance().saveHistory(file);
        DaoManager.getInstance().getDao(FileModelDao.class).replace(temp);
        return false;
    }

    /**
     * 获取文件在列表中的位置
     *
     * @param file
     * @return 未找到返回-1
     */
    private int indexOfList(File file) {
        for (FileModel temp : commList) {
            if (file.getName().equals(temp.getName())) {
                return commList.indexOf(temp);
            }
        }
        return -1;
    }

    public static class Entry {
        File file;
        ZipEntry zip;
        FileHeader rar;

        Entry(ZipEntry zip) {
            this.zip = zip;
        }

        Entry(File file) {
            this.file = file;
        }

        Entry(FileHeader rar) {
            this.rar = rar;
        }
    }

}
