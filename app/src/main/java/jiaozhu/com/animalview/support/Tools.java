package jiaozhu.com.animalview.support;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jiaozhu.com.animalview.commonTools.Log;
import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by Administrator on 2014/6/20.
 */
public class Tools {
    /**
     * 将横线显示字符串转换成竖直显示的
     *
     * @param str
     * @return
     */
    public static String getVerString(String str) {
        str = str.replace("(", "\n︵");
        str = str.replace(")", "︶");
        return str;
    }

    /**
     * 从文件流中间获取字符串
     *
     * @param stream
     * @return
     */
    public static String getStringFromStream(InputStream stream) {
        try {
            InputStreamReader inputReader = new InputStreamReader(stream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写入文件
     *
     * @param file 需要写入的文件
     * @param str  需要写入的内容
     * @return 写入是否成功
     */
    public static boolean writeFile(File file, String str) {
        boolean flag = false;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            byte[] bytes = str.getBytes();
            fos.write(bytes);
            fos.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 检查是否为空
     *
     * @param texts 需要进行检查的view列表
     * @return 是否通过检查
     */
    public static boolean checkEmpty(TextView... texts) {
        for (TextView temp : texts) {
            if (temp.getText().length() < 1) {
                temp.requestFocus();
                temp.requestFocusFromTouch();
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为平板设备
     *
     * @return
     */
    public static boolean isTabletDevice(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        return type == TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 设置除了标题之外的字体大小
     *
     * @param view  父view
     * @param title 标题View
     * @param size  需要变更的大小
     */
    public static void setAllTextSize(View view, TextView title, int size) {
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewChild = vp.getChildAt(i);
                if (viewChild instanceof ViewGroup) {
                    setAllTextSize(viewChild, title, size);
                }
                if (viewChild instanceof TextView && title != viewChild) {
                    ((TextView) viewChild).setTextSize(size);
                }
            }
        }
    }


    private static void showDialog(Context context, final EditText view, final String... items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context);
        builder.setTitle(Html.fromHtml("请选择数据"));
        builder.setSingleChoiceItems(items, -1, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                view.setText((items[which].split("-", 2))[1]);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 将Bitmap保存为文件
     *
     * @param bitmap
     * @param file   文件
     */
    public static void saveBitmap(Bitmap bitmap, File file) {
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件成为bitMap
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
        }
        return bitmap;
    }

    /**
     * 读取文件成为bitMap
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis, null, options);
        } catch (Exception e) {
        }
        return bitmap;
    }


    private static BitmapFactory.Options options = new BitmapFactory.Options();

    {
        options.inJustDecodeBounds = true;
    }

    /**
     * 是否为双页合并状态
     *
     * @param path
     * @return
     */
    public static boolean isDoublePage(String path) {
        Bitmap bm = getBitmap(path, options);
        if (bm.getHeight() < bm.getWidth() * 3 / 4) {
            return true;
        }
        return false;
    }


    /**
     * 读取文件成为bitMap（保持旋转）
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmapWithRota(String path) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
            bitmap = rotaingImageView(readPictureDegree(path), bitmap);
        } catch (Exception e) {
        }
        return bitmap;
    }

    /**
     * 压缩图片
     *
     * @param image 图片
     * @param hh    目标高度
     * @param ww    目标宽度
     * @return
     */
    public static Bitmap comp(Bitmap image, float hh, float ww) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {
            //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap;
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 根据Uri来获取图片
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        BitmapDrawable bm = new BitmapDrawable(context.getResources(), getPathFromUri(context, uri));
        return bm.getBitmap();
    }

    /**
     * 根据Uri获取图片路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPathFromUri(Context context, Uri uri) {
        String[] filePathColumns = {MediaStore.Images.Media.DATA};
        Cursor c = context.getContentResolver().query(uri, filePathColumns, null, null,
                null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePathColumns[0]);
        String picturePath = c.getString(columnIndex);
        c.close();
        return picturePath;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (!oldfile.exists()) {
                return false;
            }
            if (!oldfile.isFile()) {
                return false;
            }
            if (!oldfile.canRead()) {
                return false;
            }
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            Log.e("", "复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        int degree = readPictureDegree(imagePath);
        //如果照片为横向的话交换宽高比
        if (degree == 90 || degree == 270) {
            int temp = height;
            height = width;
            width = temp;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        //根据拍摄旋转图片
        return rotaingImageView(readPictureDegree(imagePath), bitmap);
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = null;
        try {
            resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {

        }
        return resizedBitmap;
    }

    public static String md516(String paramString) {
        return md5Encode(paramString).substring(8, 24);
    }

    /**
     * MD5加密
     *
     * @param str
     * @return
     */
    public static String md5Encode(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 检查是否为debug模式
     *
     * @param context
     * @return
     */
    public static boolean debugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 删除漫画目录(生成删除记录)
     *
     * @param file
     * @return
     */
    public static boolean deleteAnimal(File file) {
        writeFile(Constants.DELETE_LOG, ">>:" + file.getName() + "\n");
        return deleteDir(file);
    }

    /**
     * 递归删除目录
     *
     * @param file
     * @return
     */
    public static boolean deleteDir(File file) {
        if (file.isFile()) return file.delete();
        for (File temp : file.listFiles()) {
            if (!deleteDir(temp)) {
                return false;
            }
        }
        return file.delete();
    }

    /**
     * 支持文件过滤器
     */
    public static FileFilter imageFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.getName().startsWith(".")) return false;
            if (file.isHidden()) return false;
            if (file.isDirectory()) return true;
            for (String temp : Constants.IMAGE_TYPE) {
                if (file.getName().toLowerCase().endsWith(temp)) return true;
            }
            return false;
        }
    };


    /**
     * 获取目录列表，并完成状态标记
     *
     * @param file
     * @return
     */
    public static Map<String, FileModel> getDirList(File file) {
        Map<String, FileModel> map = new HashMap<>();
        if (file.isDirectory()) {
            int status = FileModel.STATUS_EMPTY;
            for (File temp : file.listFiles(imageFilter)) {
                if (temp.isDirectory()) {
                    status = FileModel.STATUS_OPEN;
                    map.putAll(getDirList(temp));
                } else {
                    if (status == FileModel.STATUS_EMPTY)
                        status = FileModel.STATUS_SHOW;
                }
            }
            FileModel model = new FileModel();
            model.setFile(file);
            model.setPath(file.getPath());
            model.setStatus(status);
            map.put(file.getPath(), model);
        }
        return map;
    }

    /**
     * 图片切割方法
     *
     * @param bitmap  图片
     * @param pageNum 0:第一页,1:第二页
     * @return
     */
    public static Bitmap splitBitmap(Bitmap bitmap, int pageNum) {
        int pieceWidth = bitmap.getWidth() / 2;
        int pieceHeight = bitmap.getHeight();
        if (pageNum == 0) {
            return Bitmap.createBitmap(bitmap, 0, 0,
                    pieceWidth, pieceHeight);
        }
        if (pageNum == 1) {
            return Bitmap.createBitmap(bitmap, pieceWidth, 0,
                    pieceWidth, pieceHeight);
        }
        return null;
    }

    /**
     * 读取zip目录
     *
     * @param file
     * @return
     */
    public static List<ZipEntry> readZip(File file) {
        List<ZipEntry> list = new ArrayList<>();
        try {
            ZipFile zip = new ZipFile(file);
            ZipEntry element = zip.entries().nextElement();
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                list.add(entry);
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据目标大小载入图片
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算例子尺寸
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }


}
