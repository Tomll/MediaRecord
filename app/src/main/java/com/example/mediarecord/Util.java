package com.example.mediarecord;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by dongrp on 2018/9/30.
 * 全局工具类
 */

public class Util {
    private static final String TAG = "dongrp";//应用的总TAG
    private static boolean isDebug = true;
    private static Toast toast;

    //logd
    public static void d(Object cls, Object str) {
        if (isDebug) {
            String msg = str.toString();
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - TAG.length();
            //大于max_str_length时
            while (msg.length() > max_str_length) {
                Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + msg);
            //android.util.Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + str);
        }
    }

    //loge
    public static void e(Object cls, Object str) {
        if (isDebug) {
            String msg = str.toString();
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - TAG.length();
            //大于max_str_length时
            while (msg.length() > max_str_length) {
                Log.e(TAG, cls.getClass().getSimpleName() + " -->: " + msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.e(TAG, cls.getClass().getSimpleName() + " -->: " + msg);
            //android.util.Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + str);
        }
    }

    //logi
    public static void i(Object cls, Object str) {
        if (isDebug) {
            String msg = str.toString();
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - TAG.length();
            //大于max_str_length时
            while (msg.length() > max_str_length) {
                Log.i(TAG, cls.getClass().getSimpleName() + " -->: " + msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.i(TAG, cls.getClass().getSimpleName() + " -->: " + msg);
            //android.util.Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + str);
        }
    }

    public static void d(String TAG, Object str) {
        if (isDebug) {
            String msg = str.toString();
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - TAG.length();
            //大于max_str_length时
            while (msg.length() > max_str_length) {
                Log.d(TAG, " -->: " + msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.d(TAG, " -->: " + msg);
            //android.util.Log.d(TAG, cls.getClass().getSimpleName() + " -->: " + str);
        }
    }


    public static void toastLong(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_LONG);
    }

    public static void toastLong(Context context, CharSequence charSequence) {
        showToast(context, charSequence, Toast.LENGTH_LONG);
    }

    public static void toastShort(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_SHORT);
    }

    public static void toastShort(Context context, CharSequence charSequence) {
        showToast(context, charSequence, Toast.LENGTH_SHORT);
    }

    //防重叠Toast
    public static void showToast(Context context, int resId, int duration) {
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), resId, duration);
        } else {
            toast.setText(resId);
            toast.setDuration(duration);
        }
        toast.show();
    }

    //防重叠Toast
    public static void showToast(Context context, CharSequence charSequence, int duration) {
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), charSequence, duration);
        } else {
            toast.setText(charSequence);
            toast.setDuration(duration);
        }
        toast.show();
    }

    public static void glideLoadImage(Context context, String url, ImageView imageView) {
        if (null == url) return;
        String imgName = url.substring(url.lastIndexOf("/") + 1);
        if (isAssetsContainsFile(context, "preImg", imgName)) {//assets中有，加载assets中的图
            Glide.with(context).load("file:///android_asset/preImg/" + imgName).into(imageView);
        } else {//assets中没有的，网络加载
            Glide.with(context).load(url).into(imageView);
        }
    }

    public static void glideLoadImage(Context context, String url, ImageView imageView, TransitionOptions transitionOptions) {
        if (null == url) return;
        String imgName = url.substring(url.lastIndexOf("/") + 1);
        if (isAssetsContainsFile(context, "preImg", imgName)) {//assets中有，加载assets中的图
            Glide.with(context).load("file:///android_asset/preImg/" + imgName).transition(transitionOptions).into(imageView);
        } else {//assets中没有的，网络加载
            Glide.with(context).load(url).transition(transitionOptions).into(imageView);
        }
    }

    //查看assets的path目录下是否有名叫fileName的文件
    public static boolean isAssetsContainsFile(Context context, String path, String fileName) {
        String[] names;//所有文件名
        AssetManager am = context.getAssets();
        try {
            names = am.list(path);//填入目录名获取该目录下所有资源, ""获取所有
            for (String s : names) {
                if (s.equals(fileName)) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //安装apk
    /*public static void installApk(Context context, String apkPath) {
        if (context == null || TextUtils.isEmpty(apkPath)) {
            return;
        }
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            //provider authorities
            Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".TTFileProvider", file);
            //Granting Temporary Permissions to a URI
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }*/

    /**
     * 创建多级目录文件
     */
    public static File createFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            try {
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据路径删除指定文件或文件夹
     *
     * @param sPath ：路径
     * @return ：删除成功返回true,否则返回false
     */
    public static boolean deleteFileOrFolder(String sPath) {
        File file = new File(sPath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(sPath);
            } else {
                return deleteFolder(sPath);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param sPath：文件路径
     * @return ：是否删除成功
     */
    private static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        if (file.isFile() && file.exists()) {
            flag = file.delete();
        }
        return flag;
    }

    /**
     * 删除目录以及目录下的文件
     *
     * @param sPath ：目录路径
     * @return ：是否删除成功
     */
    private static boolean deleteFolder(String sPath) {
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File ff : files) {
                if (ff.isFile()) {
                    flag = deleteFile(ff.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                } else {
                    flag = deleteFolder(ff.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前目录
        return dirFile.delete();
    }

    /**
     * 获取一个文件的md5消息摘要值
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    static Random random;//随机数发生器

    //获取随机数发生器（传入seed）
    public static Random getRandomBySeed(long seed) {
        if (null == random) {
            random = new Random(seed);
        } else {
            random.setSeed(seed);
        }
        return random;
    }

    /***
     * 获取字符串的MD5信息摘要
     */
    public static String getStringMD5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字节数组转十六进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * 时间戳转换成日期
     *
     * @param pattern：传入需要的日期格式字符串，例：yyyy-MM-dd HH:mm:ss
     * @param milSecond：当前系统时间戳
     */
    public static String getDateFromTimeMillis(String pattern, long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：yyyy-MM-dd HH:mm:ss
     */
    public static String getDateFromTimeMillis(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：yyyy-MM-dd
     */
    public static String getDateFromTimeMillis1(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：MM月dd日
     */
    public static String getDateFromTimeMillis2(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：MM月dd日 HH:mm:ss
     */
    public static String getDateFromTimeMillis3(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm:ss");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：yyyy-MM-dd-HH:00
     */
    public static String getDateFromTimeMillis4(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:00");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：mm:ss
     */
    public static String getDateFromTimeMillis5(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：HH:mm
     */
    public static String getDateFromTimeMillis6(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：MM-dd HH:mm
     */
    public static String getDateFromTimeMillis7(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：yyyy-MM-dd HH:mm
     */
    public static String getDateFromTimeMillis8(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：MM.dd
     */
    public static String getDateFromTimeMillis9(long milSecond) {
        Date date = new Date(milSecond);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("MM.dd");
        return format.format(date);
    }

    /**
     * 时间戳转换成日期：HH:mm(一天之内);昨天 HH:mm;前天 HH:mm;MM-dd HH:mm(两天之后);yyyy-MM-dd HH:mm(去年)
     */
    public static String getDifferentDate(long milSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();//获取当前日期开始时的时间戳
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        long yearStartTime = calendar.getTimeInMillis();//获取当前年份开始时的时间戳
        if (milSecond >= startTime) {
            return getDateFromTimeMillis6(milSecond);
        } else if (milSecond >= startTime - 86400000) {
            return "昨天 " + getDateFromTimeMillis6(milSecond);
        } else if (milSecond >= startTime - 86400000 * 2) {
            return "前天 " + getDateFromTimeMillis6(milSecond);
        } else if (milSecond > yearStartTime) {
            return getDateFromTimeMillis7(milSecond);
        } else {
            return getDateFromTimeMillis8(milSecond);
        }
    }

    /**
     * 判断所给时间是否在今天内
     *
     * @param lastTime 上次时间
     * @return 上次时间与当前时间是否在同一天
     */
    public static boolean isToday(long lastTime) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);//获取当前时间
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime;
        long endTime;
        if (currentHour >= 3) {//3点之前按前一天算
            startTime = calendar.getTimeInMillis();//获取当前日期3点的时间戳
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            endTime = calendar.getTimeInMillis();//获取明天3点的时间戳
        } else {//3点之后按后一天算
            endTime = calendar.getTimeInMillis();//获取当前日期3点的时间戳
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
            startTime = calendar.getTimeInMillis();//获取昨天3点的时间戳
        }
        return lastTime >= startTime && lastTime < endTime;
    }

    /**
     * 判断所给时间是否在同一周内
     *
     * @param lastTime 上次时间
     * @return 上次时间与当前时间是否在同一周
     */
    public static boolean isThisWeek(long lastTime, long nextTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(lastTime));
        int week_index = cal.get(Calendar.DAY_OF_WEEK);//获取指定时间是周几
        int week = cal.get(Calendar.WEEK_OF_YEAR);//获取指定时间是第几周
        int hour = cal.get(Calendar.HOUR_OF_DAY);//获取指定时间的小时
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date(nextTime));
        int week_index2 = cal2.get(Calendar.DAY_OF_WEEK);//获取当前是周几
        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);//获取当前是第几周
        int hour2 = cal2.get(Calendar.HOUR_OF_DAY);//获取当前小时
        if (Math.abs(week2 - week) == 1) {
            //两个时间点不在同一个自然周且相邻，那么，在同一个服务周的可能性就是，week_index在周五3点之后，week_index2在下周五3点之前
            return (week_index > Calendar.FRIDAY || (week_index == Calendar.FRIDAY && hour >= 3)) && (week_index2 < Calendar.FRIDAY || (week_index2 == Calendar.FRIDAY && hour2 < 3));
        } else if (week == week2) {
            //两个时间点在同一个自然周，那么，在同一个服务周的可能性就是，week_index，week_index2在周五3点之前或都在周五3点之后
            return ((week_index > Calendar.FRIDAY || (week_index == Calendar.FRIDAY && hour >= 3)) && (week_index2 > Calendar.FRIDAY || (week_index2 == Calendar.FRIDAY && hour2 >= 3))) || ((week_index < Calendar.FRIDAY || (week_index == Calendar.FRIDAY && hour < 3)) && (week_index2 < Calendar.FRIDAY || (week_index2 == Calendar.FRIDAY && hour2 < 3)));
        } else {
            //两个时间点不在同一个自然周且不相邻，一定不在同一个服务周
            return false;
        }
    }

    /**
     * 时间格式化（由秒转化为分，时，天）
     *
     * @param second
     * @return
     */
    public static String secondToWeek(int second) {
        int minute = Math.round((float) second / 60);
        if (minute < 1000) return minute + "分";
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        float hour = (float) minute / 60;
        if (hour < 100) return decimalFormat.format(hour) + "时";
        float day = hour / 24;
        return decimalFormat.format(day) + "天";
    }

    /**
     * 将bitmap转换为byte数组
     *
     * @param bmp         图片
     * @param needRecycle 是否回收利用
     * @return byte数组
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 数组分割方法
     * 思路也比较简单,就是遍历加切块,
     * 若toIndex大于list的size说明已越界,需要将toIndex设为list的size值
     */
    public static <T> List<List<T>> splitList(List<T> list, int pageSize) {
        List<List<T>> listArray = new ArrayList<>();
        for (int i = 0; i < list.size(); i += pageSize) {
            int toIndex = Math.min(i + pageSize, list.size());
            listArray.add(list.subList(i, toIndex));
        }
        return listArray;
    }


    /**
     * 保存图片到公共目录
     * 29 以下，需要提前申请文件读写权限
     * 29及29以上的，不需要权限
     * 保存的文件在 DCIM 目录下
     *
     * @param context 上下文
     * @param bitmap  需要保存的bitmap
     * @param format  图片格式
     * @param quality 压缩的图片质量
     * @param recycle 完成以后，是否回收Bitmap，建议为true
     */
    public static void saveAlbum(Context context, Bitmap bitmap, Bitmap.CompressFormat format, int quality, boolean recycle) {
        String suffix;
        if (Bitmap.CompressFormat.JPEG == format)
            suffix = "JPG";
        else
            suffix = format.name();
        String fileName = System.currentTimeMillis() + "_" + quality + "." + suffix;
        if (Build.VERSION.SDK_INT < 29) {
//            if (!isGranted(context)) {
//                Log.e("ImageUtils", "save to album need storage permission");
//                return;
//            }
            File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File destFile = new File(picDir, fileName);
            if (!save(bitmap, destFile, format, quality, recycle))
                return;
            Uri uri = null;
            if (destFile.exists()) {
                uri = Uri.parse("file://" + destFile.getAbsolutePath());
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
            }
            Util.toastShort(context, "成功保存到相册");
            return;
        } else {
            // Android 10 使用
            Uri contentUri;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else
                contentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/");
            // 告诉系统，文件还未准备好，暂时不对外暴露
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri uri = context.getContentResolver().insert(contentUri, contentValues);
            if (uri == null) return;
            OutputStream os = null;
            try {
                os = context.getContentResolver().openOutputStream(uri);
                bitmap.compress(format, quality, os);
                // 告诉系统，文件准备好了，可以提供给外部了
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                context.getContentResolver().update(uri, contentValues, null, null);
                Util.toastShort(context, "成功保存到相册");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                // 失败的时候，删除此 uri 记录
                context.getContentResolver().delete(uri, null, null);
                return;
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * SDK_INT < 29:保存Bitmap图像到DCIM目录
     */
    private static boolean save(Bitmap bitmap, File file, Bitmap.CompressFormat format, int quality, boolean recycle) {
        if (isEmptyBitmap(bitmap)) {
            Log.e("ImageUtils", "bitmap is empty.");
            return false;
        }
        if (bitmap.isRecycled()) {
            Log.e("ImageUtils", "bitmap is recycled.");
            return false;
        }
        if (!createFile(file, true)) {
            Log.e("ImageUtils", "create or delete file <$file> failed.");
            return false;
        }
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = bitmap.compress(format, quality, os);
            if (recycle && !bitmap.isRecycled()) bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return ret;
    }

    private static boolean isEmptyBitmap(Bitmap bitmap) {
        return bitmap == null || bitmap.isRecycled() || bitmap.getWidth() == 0 || bitmap.getHeight() == 0;
    }

    private static boolean createFile(File file, boolean isDeleteOldFile) {
        if (file == null) return false;
        if (file.exists()) {
            if (isDeleteOldFile) {
                if (!file.delete()) return false;
            } else
                return file.isFile();
        }
        if (!createDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean createDir(File file) {
        if (file == null) return false;
        if (file.exists())
            return file.isDirectory();
        else
            return file.mkdirs();
    }

    /**
     * 判断RecyclerView是否滑动到了底部
     */
    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange();
    }


    /**
     * 改变Recycler的滑动速度
     *
     * @param recyclerView 要改变滑动速度的RecyclerView
     * @param velocity     系统默认的滑动速度是8000dp
     */
    public static void setMaxFlingVelocity(RecyclerView recyclerView, int velocity) {
        try {
            Field field = recyclerView.getClass().getDeclaredField("mMaxFlingVelocity");
            field.setAccessible(true);
            field.set(recyclerView, velocity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据请求参数的map集合获取sign签名信息
     */
    /*public static String getSign(Map<String, String> map) {
        try {
            StringBuilder sb = new StringBuilder();
            //头部拼接秘钥
            sb.append(MyApplication.stringFromJNI());
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
            //对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, (o1, o2) -> (o1.getKey()).compareTo(o2.getKey()));
            //将非空键值对进行拼接
            for (Map.Entry<String, String> item : infoIds) {
                if (item.getValue() != null && !item.getValue().equals("")) {
                    String key = item.getKey();
                    String val = item.getValue();
                    sb.append(key).append(val);
                }
            }
            String str = sb.toString();
            //转Base64
            String strBase64 = Base64.encodeToString(str.getBytes(), Base64.DEFAULT).replaceAll("[\\s*\t\n\r]", "");
            //获取strBase64的MD5值并返回
            return getStringMD5(strBase64);
        } catch (Exception e) {
            return "";
        }
    }*/

    /**
     * 给一个view执行缩放动画(补间动画实现)
     *
     * @param view：目标view
     * @param animDuration：动画时长(缩放整体时长)
     * @param valueMaxPercent：缩放达到最大的相对值，相对于view自身的大小的百分比
     */
    public static void startScaleAnimatorOnAView(View view, long animDuration, float valueMaxPercent) {
        ScaleAnimation animation = new ScaleAnimation(1f, valueMaxPercent, 1f, valueMaxPercent, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(animDuration / 2);
        animation.setRepeatCount(500);
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
        animation.startNow();

//        ObjectAnimator animator0 = ObjectAnimator.ofFloat(view, "scaleX", 1f, valueMaxPercent, valueMiddlePercent, 1f);
//        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, valueMaxPercent, valueMiddlePercent, 1f);
//        animator0.setDuration(animDuration);
//        animator0.setRepeatCount(500);
//        animator1.setDuration(animDuration);
//        animator1.setRepeatCount(500);
//        AnimatorSet animatorSetDown = new AnimatorSet();
//        animatorSetDown.playTogether(animator0, animator1);
//        animatorSetDown.start();
    }


    /**
     * 给一个view执行缩放动画（属性动画实现）
     *
     * @param view：目标view
     * @param animDuration：动画时长(缩放整体时长)
     * @param valueMaxPercent：缩放达到最大的相对值，相对于view自身的大小的百分比
     */
    public static AnimatorSet startPropertyScaleAnimatorOnAView(View view, long animDuration, float valueMaxPercent, float valueMiddlePercent) {
        //创建x轴缩放动画animator0
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(view, "scaleX", 1f, valueMaxPercent, valueMiddlePercent, 1f);
        animator0.setRepeatCount(500);
        animator0.setRepeatMode(ValueAnimator.REVERSE);
        //创建y轴缩放动画animator1
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, valueMaxPercent, valueMiddlePercent, 1f);
        animator1.setRepeatCount(500);
        animator1.setRepeatMode(ValueAnimator.REVERSE);
        //将animator0, animator1加入到animatorSetDown中一起同时播放
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator0, animator1);
        animatorSet.setDuration(animDuration);
        animatorSet.start();
        return animatorSet;
    }

    //获取视频文件缩略图
    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    //////////////////////////////////////////////////////////////////////////
    //                                                                      //
    //                                                                      //
    //                          ↑通用   ↓业务                                 //
    //                                                                      //
    //                                                                      //
    //////////////////////////////////////////////////////////////////////////


    //通过身份证号获取年龄
    public static int getAge(String date) {
        if (null == date || date.length() < 8) return -1;
        try {
            long currentTime = System.currentTimeMillis();
            int bornYear = Integer.parseInt(date.substring(0, 4));//获取出生年份
            System.out.println(bornYear);
            int nowYear = Integer.parseInt(Util.getDateFromTimeMillis("yyyy", currentTime));//获取当前年份
            int bornMonth = Integer.parseInt(date.substring(4, 6));//获取出生月份
            System.out.println(bornMonth);
            int nowMonth = Integer.parseInt(Util.getDateFromTimeMillis("MM", currentTime));//获取当前月份
            if (nowMonth < bornMonth) {
                return nowYear - bornYear - 1;
            }
            if (nowMonth > bornMonth) {
                return nowYear - bornYear;
            }
            int bornDay = Integer.parseInt(date.substring(6, 8));//获取出生日期
            int nowDay = Integer.parseInt(Util.getDateFromTimeMillis("dd", currentTime));//获取当前日期
            System.out.println(bornDay);
            if (nowDay < bornDay) {
                return nowYear - bornYear - 1;
            }
            return nowYear - bornYear;
        } catch (Exception e) {
            return -1;
        }
    }


}
