package com.lisn.audiorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

public class MediaRecordActivity extends AppCompatActivity {

    private Button btnRecord;
    private Button btnPlay;

    private MediaRecorder recorder;
    private File audioFile;

    private boolean isRecording = false;//是否正在录音，默认为false
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);

        btnRecord = findViewById(R.id.btn_record);
        btnPlay = findViewById(R.id.btn_play);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    btnRecord.setText("RECORD");
                    //此处结束录音
                    isRecording = false;
                    recorder.stop();
                    recorder.release();
                } else {
                    btnRecord.setText("RECORDING");
                    //此处开始录音
                    init();
                    startRecord();
                }
            }
        });
    }

    /**
     * 初始化
     */
    private void init() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置播放源 麦克风
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //设置输入格式 3gp
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //设置编码 AMR
    }


    /**
     * 开始录音
     */
    private void startRecord() {

        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/MediaRecorderTest");

        if (!path.exists()) {
            path.mkdirs();
        }

        try {
            audioFile = new File(path, "test.3gp");
            if (audioFile.exists()) {
                audioFile.delete();
            }
            audioFile.createNewFile();//创建文件

        } catch (Exception e) {
            throw new RuntimeException("Couldn't create recording audio file", e);
        }

        String audioFilePath = audioFile.getPath();
        Log.e("TAG", "startRecord:audioFilePath "+audioFilePath );
        recorder.setOutputFile(audioFilePath); //设置输出文件

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            throw new RuntimeException("IllegalStateException on MediaRecorder.prepare", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException on MediaRecorder.prepare", e);
        }
        isRecording = true;
        recorder.start();
    }
}
