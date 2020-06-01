package com.lisn.audiorecord;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lisn.audiorecord.utils.PcmToWavUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lisn.audiorecord.utils.GlobalConfig.AUDIO_FORMAT;
import static com.lisn.audiorecord.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.lisn.audiorecord.utils.GlobalConfig.SAMPLE_RATE_INHZ;

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AudioRecordActivity.class.getName();

    private Button btn_begin;
    private Button btn_convert;
    private Button btn_play;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private AudioRecord audioRecord = null;//声明AudioRecord对象
    private boolean isRecording = false;//是否录音
    private boolean isPlaying = false;//是否在播放声音
    private TextView tv;
    private AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        tv = findViewById(R.id.tv);
        btn_begin = findViewById(R.id.btn_begin);
        btn_convert = findViewById(R.id.btn_convert);
        btn_play = findViewById(R.id.btn_play);
        btn_begin.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_convert.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_begin) {
            if (!isRecording) {
                creatAudioRecord();
                btn_begin.setText("停止录音");
            } else {
                closeRecord();
                btn_begin.setText("开始录音");
            }
        } else if (v == btn_convert) {
            Convert();
        } else if (v == btn_play) {
            if (isPlaying) {
                stop();
            } else {
                play();
            }
        }
    }

    private void Convert() {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "video2.wav");
        if (!wavFile.mkdirs()) {
            Log.e("--------", "wavFile Directory not created");
        }
        if (wavFile.exists()) {
            wavFile.delete();
        }
        String wavFilePath = wavFile.getAbsolutePath();
        String pcmFilePath = pcmFile.getAbsolutePath();
        Log.e(TAG, "onClick: wavFilePath=" + wavFilePath);
        Log.e(TAG, "onClick: pcmFilePath=" + pcmFilePath);
        pcmToWavUtil.pcmToWav(pcmFilePath, wavFilePath);
        //pcmToWavUtil.pcmToWav(Environment.getExternalStorageDirectory().getPath() + "/video2.pcm", wavFile.getAbsolutePath());
    }

    private void closeRecord() {
        isRecording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }


    private void creatAudioRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        //构造函数参数：
        //1.记录源
        //2.采样率，以赫兹表示
        //3.音频声道描述，声道数
        //4.返回音频声道的描述，格式
        //5.写入音频数据的缓冲区的总大小（字节），小于最小值将创建失败
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte[] data = new byte[minBufferSize];
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        if (!file.mkdirs()) {
            Log.e("demo failed---->", "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }

        Log.e("TAG", "creatAutiRecord: " + file.getAbsolutePath());
        tv.setText(file.getAbsolutePath());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                isRecording = true;
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                try {
                    FileOutputStream os = new FileOutputStream(file);
                    if (null != os) {
                        while (isRecording) {
                            int read = audioRecord.read(data, 0, minBufferSize);
                            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                                try {
                                    os.write(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Log.e("run------>", "close file output stream !");
                        os.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void play() {
        /*
         * SAMPLE_RATE_INHZ 对应pcm音频的采样率
         * channelConfig 对应pcm音频的声道
         * AUDIO_FORMAT 对应pcm音频的格式
         * */
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(channelConfig)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);

//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_INHZ, channelConfig,
//                AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);

        audioTrack.play();
        isPlaying = true;
        btn_play.setText("停止");

        try {
//            File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "video2.wav");
//            File file = new File(Environment.getExternalStorageDirectory().getPath(), "test.pcm");
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
            Log.e(TAG, "play: Path = " + file.getAbsolutePath());
            final FileInputStream fileInputStream = new FileInputStream(file);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        stop();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        if (audioTrack != null) {
            Log.d("player:", "Stopping");
            audioTrack.stop();
            Log.d("player:", "Releasing");
            audioTrack.release();
            Log.d("player:", "Nulling");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_play.setText("播放");
                }
            });

            isPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRecord();
    }
}
