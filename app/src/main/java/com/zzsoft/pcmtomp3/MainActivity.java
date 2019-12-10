package com.zzsoft.pcmtomp3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzsoft.pcmtomp3.audiohelp.MediaPlayerHelper;
import com.zzsoft.pcmtomp3.utils.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MediaPlayerHelper mediaPlayerHelper;
    //AudioCapture
    private AudioCapture audioCapture;

    private String filePath;
    //标记是否开始录音
    private boolean isStartAudio = false;
    //上传标识
    private boolean isUpload = false;

    TextView recordResult;

    Button startRecord,stopRecord,playRecord,stopPlayRecord,delRecord,uploadRecord;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecorder();
        recordResult = findViewById(R.id.record_result);

        startRecord = findViewById(R.id.start_record);
        startRecord.setOnClickListener(this);

        stopRecord = findViewById(R.id.stop_record);
        stopRecord.setOnClickListener(this);

        playRecord = findViewById(R.id.play_record);
        playRecord.setOnClickListener(this);

        stopPlayRecord = findViewById(R.id.stop_play_record);
        stopPlayRecord.setOnClickListener(this);

        delRecord = findViewById(R.id.del_record);
        delRecord.setOnClickListener(this);

        uploadRecord = findViewById(R.id.upload_record);
        uploadRecord.setOnClickListener(this);

    }

    private void initRecorder() {
        mediaPlayerHelper = new MediaPlayerHelper();
        audioCapture = new AudioCapture(this);
    }


    /**
     * 开始录制音频
     */
    private void startRecording() {
        filePath = FileUtils.getFilePath(this);
        audioCapture.initFileStream(filePath);

        isStartAudio = true;
        audioCapture.start();
    }

    /**
     * 停止录制音频
     */
    private void stopRecord() {
        if (isStartAudio) {
            isStartAudio = false;
            audioCapture.stop();
            audioCapture.writeFlush();
        }
    }

    /**
     * 播放录制的音频
     */
    private void startPlayRecord() {
        String audioUrl = filePath;
        mediaPlayerHelper.startPlayAudio(audioUrl);
    }

    /**
     * 停止播放录制的音频
     */
    private void stopPlayRecord() {
        mediaPlayerHelper.stopAudio();
    }

    /**
     * 删除录制的音频
     */
    private void deleteRecord() {
        if (!isUpload && !TextUtils.isEmpty(filePath)) {
            new File(filePath).delete();
        }

    }

    /**
     * 上传录制的音频
     */
    private void uploadRecord() {
        uploadFile(filePath);
    }
    private void uploadFile(String filePath){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_record:
                recordResult.setText("正在录制音频");
                startRecording();
                break;
            case R.id.stop_record:
                recordResult.setText("音频录制完毕  地址："+filePath);
                stopRecord();
                break;
            case R.id.play_record:
                recordResult.setText("播放音频");
                startPlayRecord();
                break;
            case R.id.stop_play_record:
                recordResult.setText("停止播放音频");
                stopPlayRecord();
                break;
            case R.id.del_record:
                recordResult.setText("删除音频");
                deleteRecord();
                break;
            case R.id.upload_record:
                //需要自己完善
                recordResult.setText("上传音频");
                uploadRecord();
                break;
        }
    }
}
