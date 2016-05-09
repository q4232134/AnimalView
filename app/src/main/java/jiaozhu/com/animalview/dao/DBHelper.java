package jiaozhu.com.animalview.dao;

import android.content.Context;

import com.tgb.lk.ahibernate.util.MyDBHelper;

import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.support.Constants;


/**
 * Created by apple on 15/10/30.
 */
public class DBHelper extends MyDBHelper {
    private static final Class<?>[] clazz = {FileModel.class
    };

    public DBHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION, clazz);
    }

    public void onUpgrade() {
        super.onUpgrade(this.getWritableDatabase(), Constants.DB_VERSION, Constants.DB_VERSION);
    }

}
