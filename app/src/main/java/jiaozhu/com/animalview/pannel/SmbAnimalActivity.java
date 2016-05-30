package jiaozhu.com.animalview.pannel;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;

/**
 * Created by jiaozhu on 16/5/30.
 */
public class SmbAnimalActivity extends MainActivity {
    public static String PARAM_URL = "url";
    List<SmbFile> list = new ArrayList<>();
    SmbFile currentFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
//
//    /**
//     * 刷新
//     *
//     * @param pageNum 刷新之后显示的页码,-1代表最后一页
//     */
//    private void fresh(int pageNum) {
//        list.clear();
//        File[] tempList = currentModel.getFile().listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String filename) {
//                String tempName = filename.toLowerCase();
//                for (String type : Constants.IMAGE_TYPE) {
//                    if (tempName.endsWith(type)) return true;
//                }
//                return false;
//            }
//        });
//        if (tempList != null)
//            for (File temp : tempList) {
//                list.add(temp);
//            }
//        setTitle(currentModel.getFile().getName());
//        mSeekBar.setMax(adapter.getCount() - 1);
//        mSeekBar.setProgress(0);
//        freshPageNum();
//        adapter.notifyDataSetChanged();
//        if (pageNum == LAST_PAGE)
//            pageNum = adapter.getCount() - 1;
//        mViewPager.setCurrentItem(pageNum, false);
//    }
}
