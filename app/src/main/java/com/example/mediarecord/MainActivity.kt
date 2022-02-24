package com.example.mediarecord

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.mediarecord.MediaRecordService.Companion.MediaTypeImage
import com.example.mediarecord.MediaRecordService.Companion.MediaTypeVideo
import java.util.concurrent.Flow.Subscription


class MainActivity : AppCompatActivity() {

    var mProjectionManager: MediaProjectionManager? = null
    var subscribeMediaRecord: Subscription? = null
    var REQUEST_CODE_SCREEN_Record = 1586 //录屏请求码
    var REQUEST_CODE_SCREEN_Capture = 1587 //截屏请求码
    var intentRecordService: Intent? = null//录屏前台服务对应的intent
    //录音权限（录屏需要录音，录音权限需动态申请）
    //String[] permissions = new String[]{android.Manifest.permission.RECORD_AUDIO};

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.btStartRecord).setOnClickListener { requestScreenRecord(REQUEST_CODE_SCREEN_Record) }
        findViewById<TextView>(R.id.btStopRecord).setOnClickListener { stopRecordRecordService() }
        findViewById<Button>(R.id.btScreenCapture).setOnClickListener { requestScreenRecord(REQUEST_CODE_SCREEN_Capture) }

    }


    //发起媒体录制请求
    private fun requestScreenRecord(requestCode: Int) {
        /*if (!checkAndRequestPermissions()) {
            Toast.makeText(this, "缺少基础权限！", Toast.LENGTH_LONG).show();
            return;
        }*/
        mProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mProjectionManager!!.createScreenCaptureIntent(), requestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Util.d(this, "onActivityResult requestCode=$requestCode,resultCode=$resultCode")
        //录屏权限通过
        if ((requestCode == REQUEST_CODE_SCREEN_Record || requestCode == REQUEST_CODE_SCREEN_Capture) && resultCode == RESULT_OK) {
            startRecordService(requestCode, data) //启动录屏服务
        }
    }


    //启动媒体录制服务
    private fun startRecordService(requestCode: Int, data: Intent?) {
        intentRecordService = Intent(this, MediaRecordService::class.java)
        intentRecordService!!.putExtra("data", data)
        //根据请求码设置需要录制的媒体类型
        if (requestCode == REQUEST_CODE_SCREEN_Record) {
            intentRecordService!!.putExtra("mediaType", MediaTypeVideo)
        } else if (requestCode == REQUEST_CODE_SCREEN_Capture) {
            intentRecordService!!.putExtra("mediaType", MediaTypeImage)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentRecordService) //启动前台服务
        } else {
            startService(intentRecordService)
        }
    }

    //停止媒体录制服务
    private fun stopRecordRecordService() {
        if (null != intentRecordService) stopService(intentRecordService)
    }

    //检测并申请业务所需的基础权限
    /*private boolean checkAndRequestPermissions() {
        for (String s : permissions) {
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 28954);
                return false;
            }
        }
        return true;
    }*/


}