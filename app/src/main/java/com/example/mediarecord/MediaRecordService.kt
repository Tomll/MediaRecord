package com.example.mediarecord

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by dongrp on 2022/2/18
 * 媒体录制服务
 */
class MediaRecordService : Service() {
    lateinit var intent: Intent
    private var statusBarHeight = 0
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var jobScreenRecord: Job? = null//录屏协程Job
    private var jobScreenCapture: Job? = null//截屏协程Job
    var recordVideoPath: String? = null//录屏文件存储路径
    var recordImagePath: String? = null//截屏文件存储路径
    var msgWhatRecordCount = 1869 //录屏计时消息码
    var maxRecordSecond = 30 //录屏录制时间上限（单位s）
    var second = 0//录屏已录制时间（单位s）

    //公有静态常量
    companion object {
        const val MediaTypeVideo = 0 //录屏媒体类型
        const val MediaTypeImage = 1 //截屏媒体类型
    }

    @SuppressLint("HandlerLeak")
    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == msgWhatRecordCount) {
                if (second >= maxRecordSecond) { //达到录制时间上限,停止录制和计时
                    stopSelf()
                } else { //继续录制和计时
                    second++
//                    RxBus.getDefault().post(EventMediaRecord(MediaTypeVideo, 2, second, recordVideoPath))
                    sendEmptyMessageDelayed(msgWhatRecordCount, 1000)
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //intent赋值
        this.intent = intent
        //获取状态栏高度
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
        //根据intent的媒体类型：启动录屏 or 启动截屏
        when (intent.getIntExtra("mediaType", -1)) {
            MediaTypeVideo -> {
                if (null != jobScreenRecord && !jobScreenRecord!!.isCancelled) {
                    //有进行中的录屏任务，不允许开启新的录屏任务
                    Util.toastLong(this, "启动失败：有未完成的录屏任务")
                } else {
                    createForegroundNotification("正在录屏...")//先创建并启动前台服务
                    jobScreenRecord = GlobalScope.launch { screenRecord() }//启动协程：执行录屏
                    mHandler.sendEmptyMessage(msgWhatRecordCount)//开始录屏计时
                    Util.toastLong(this, "录屏开始")
                }
            }
            MediaTypeImage -> {
                createForegroundNotification("截屏中...")//先创建并启动前台服务
                jobScreenCapture = GlobalScope.launch { screenCapture() }//启动协程；执行截屏
                Util.toastShort(this, "截屏中...")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Util.d(this, "onDestroy")
        //根据intent的媒体类型：通过RxBus回调相应的媒体录制事件给订阅者
        when (intent.getIntExtra("mediaType", -1)) {
            MediaTypeVideo -> {
//                RxBus.getDefault().post(EventMediaRecord(MediaTypeVideo, 0, second, recordVideoPath))
                Util.toastLong(this, "录屏完成")
            }
            MediaTypeImage -> {
//                RxBus.getDefault().post(EventMediaRecord(MediaTypeImage, 0, 0, recordImagePath))
                Util.toastLong(this, "截屏成功")
            }
        }
        releaseRecordRes() //释放媒体录制相关资源
    }

    //释放媒体录制相关资源
    private fun releaseRecordRes() {
        //移除录屏计时消息，second清零
        mHandler.removeMessages(msgWhatRecordCount)
        second = 0
        //停止mediaRecorder录制、并释放资源
        //mediaRecorder.stop()会阻塞UI线程造成卡顿，放在协程内部执行
        GlobalScope.launch {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
            } finally {
                cancel()//任务执行完，在finally中取消此Job,释放资源
            }
        }
        //释放虚拟投影资源
        virtualDisplay?.release()
        virtualDisplay = null
        //关闭diaProjection
        mediaProjection?.stop()
        mediaProjection = null
        //取消协程Job,释放资源
        jobScreenRecord?.cancel()
        jobScreenRecord = null
        jobScreenCapture?.cancel()
        jobScreenCapture = null
        //停止前台服务
        stopForeground(true)
    }

    //创建前台服务通知
    private fun createForegroundNotification(text: String) {
        // 创建通知栏
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "123123")
            .setSmallIcon(R.mipmap.ic_launcher).setContentText(text).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 推送通道
            val channel = NotificationChannel("123123", "RecordVideo", NotificationManager.IMPORTANCE_NONE)
            notificationManager.createNotificationChannel(channel)
        }
        // 展示前台服务
        startForeground(123123, notification)
    }

    //录屏
    private fun screenRecord() {
        //初始化MediaRecorder
        initMediaRecorder()
        //创建虚拟投影
        createVirtualDisplay()
        //开始屏幕录制
        mediaRecorder?.start()
    }

    //初始化MediaRecorder
    private fun initMediaRecorder() {
        mediaRecorder = MediaRecorder()
        // 设置音频来源 需要动态申请 android.permission.RECORD_AUDIO 权限
        //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        // 设置视频来源
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        // 设置输出格式
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        // 设置输出文件
        recordVideoPath = (getExternalFilesDir(null).toString() + File.separator + "recordGameVideo"
                + File.separator + Util.getDateFromTimeMillis("yyyyMMdd_HHmmss", System.currentTimeMillis()) + ".mp4")
        val file = Util.createFile(recordVideoPath)
        mediaRecorder!!.setOutputFile(file.absolutePath)
        // 设置视频宽高
        mediaRecorder!!.setVideoSize(
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels + statusBarHeight
        )
        // 设置视频编码格式
        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        // 设置视频帧率
        mediaRecorder!!.setVideoFrameRate(40)
        // 设置视频编码比特率
        mediaRecorder!!.setVideoEncodingBitRate(4 * 1024 * 1024)
        // 设置音频编码格式
        /*mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // 设置音频编码采样率
        mediaRecorder.setAudioSamplingRate(44100);
        // 设置音频编码比特率
        mediaRecorder.setAudioEncodingBitRate(128000);*/
        //准备
        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //创建虚拟投影：必须在mediaRecorder.prepare()之后调用，否则报错"fail to get surface"
    private fun createVirtualDisplay() {
        //通过MediaProjectionManager获取MediaProjection
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra("data")!!)
        //创建虚拟投影createVirtualDisplay
        if (mediaProjection != null) {
            /*参数释义：
              name:本次虚拟投影的名称
              width:录制帧的宽度
              height:录制帧的高度
              dpi:录制帧的像素密度，必须大于0，一般都取1
              flags:VIRTUAL_DISPLAY_FLAG_PUBLIC通用显示屏
              surface:输出视频的surface,这个比较重要，它是你生成的VirtualDisplay的载体，可以理解是：VirtualDisplay的内容是一帧帧的
              屏幕截屏（所以你看到是有宽高，像素密度等设置），所以MediaProjection获取到的其实是一帧帧的图，然后通过surface（surface你可以
              理解成是android的一个画布，默认它会以每秒60帧来刷新，这里我们不再展开细说），来顺序播放这些图片，形成视频
              callback:回调器
              handler:消息处理器
             */
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "recordVideo",
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels + statusBarHeight,
                1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mediaRecorder!!.surface, null, null
            )
        }
    }

    //截屏
    private suspend fun screenCapture() {
        delay(200)//延迟200ms,等待前台服务启动
        //通过MediaProjectionManager获取MediaProjection
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra("data")!!)
        //创建ImageReader
        @SuppressLint("WrongConstant")
        val imageReader = ImageReader.newInstance(
            resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels + statusBarHeight, PixelFormat.RGBA_8888, 1
        )
        //创建虚拟投影createVirtualDisplay
        if (mediaProjection != null) {
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "recordImage",
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels + statusBarHeight,
                Resources.getSystem().displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, null
            )
        }
        //获取截屏bitmap并存文件
        delay(200)//延迟200ms执行，等待imageReader截取到图片
        val image = imageReader.acquireLatestImage()
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap!!.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        imageReader.close()
        image.close()
        //将提取的bitmap转存为文件
        if (bitmap != null) {
            try {
                //设置输出文件
                recordImagePath = (getExternalFilesDir(null).toString() + File.separator + "recordGameImage"
                        + File.separator + Util.getDateFromTimeMillis("yyyyMMdd_HHmmss", System.currentTimeMillis()) + ".jpg")
                val file = Util.createFile(recordImagePath)
                //bitmap输出到文件
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
                //关闭流
                out.flush()
                out.close()
                bitmap.recycle()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //停止服务
        stopSelf()
    }


}