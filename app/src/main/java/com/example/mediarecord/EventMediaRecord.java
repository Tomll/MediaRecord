package com.example.mediarecord;

//媒体录制事件
public class EventMediaRecord {

    int mediaType;//媒体类型：0-录屏事件 1-截屏事件
    int recordState;//录制状态：0-录制成功 1-录制失败 2-录制中
    int recordSecond;//录屏事件，已经录制的时长（单位s）
    String fileSavePath;//录制的媒体文件的本地存储路径

    public EventMediaRecord(int mediaType, int recordState, int recordSecond, String fileSavePath) {
        this.mediaType = mediaType;
        this.recordState = recordState;
        this.recordSecond = recordSecond;
        this.fileSavePath = fileSavePath;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getRecordState() {
        return recordState;
    }

    public void setRecordState(int recordState) {
        this.recordState = recordState;
    }

    public int getRecordSecond() {
        return recordSecond;
    }

    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }
}
