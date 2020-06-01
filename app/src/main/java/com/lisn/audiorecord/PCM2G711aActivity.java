package com.lisn.audiorecord;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import com.lisn.audiorecord.utils.G711;
import com.lisn.audiorecord.utils.ShortBytesConvert;

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

public class PCM2G711aActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_record;
    private Button btnPlay;
    private Button btn_decoder;
    private TextView tv;
    private boolean isRecording;
    private AudioRecord audioRecord;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private AudioTrack audioTrack;
    private boolean isPlaying;
    private FileInputStream fileInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcm_2_g711a);

        btn_record = findViewById(R.id.btn_record);
        btnPlay = findViewById(R.id.btnPlay);
        btn_decoder = findViewById(R.id.btn_decoder);
        tv = findViewById(R.id.tv);

        btn_record.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btn_decoder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_record) {
            Log.e("isRecording:", isRecording + "");
            if (!isRecording) {
                creatAudioRecord();
                btn_record.setText("停止录音");
            } else {
                closeRecord();
                btn_record.setText("开始录音");
            }
        } else if (v == btnPlay) {
            if (isPlaying) {
                stop();
            } else {
                play();
            }

        } else if (v == btn_decoder) {
            decoder();
        }
    }

    private void decoder() {
        File inFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.g711a");
        File outFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test_decoder.pcm");

        try {
            FileOutputStream os = new FileOutputStream(outFile);
            FileInputStream is = new FileInputStream(inFile);

            int len;
            byte[] data = new byte[1024];
            short[] pcm = new short[1024];
            while ((len = is.read(data)) > 0) {

                G711.alaw2linear(data,pcm,len);
                os.write(ShortBytesConvert.shortToBytes(pcm), 0, len);
            }

            is.close();
            os.close();
            Log.e("TAG", "onClick: 转换完成");
        } catch (Exception e) {
            Log.e("TAG", "onClick: 转换失败");
            e.printStackTrace();
        }
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


        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.g711a");
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
                    short[] inG711Buffer = new short[minBufferSize];
                    byte[] outG711Buffer = new byte[minBufferSize];

                    if (null != os) {
                        while (isRecording) {
                            int read = audioRecord.read(inG711Buffer, 0, minBufferSize);
                            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                                G711.linear2alaw(inG711Buffer, 0, outG711Buffer, inG711Buffer.length);
                                try {
                                    os.write(outG711Buffer);
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

//        audioTrack = new AudioTrack(
//                new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build(),
//                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
//                        .setEncoding(AUDIO_FORMAT)
//                        .setChannelMask(channelConfig)
//                        .build(),
//                minBufferSize,
//                AudioTrack.MODE_STREAM,
//                AudioManager.AUDIO_SESSION_ID_GENERATE);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_INHZ, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);

        audioTrack.play();
        isPlaying = true;
        btnPlay.setText("停止");
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.g711a");
        try {
            fileInputStream = new FileInputStream(file);
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
//                                short[] decoder = G711Code.G711aDecoder(new short[readCount], tempBuffer, readCount);
                                short[] decoder = new short[readCount];
                                G711.alaw2linear(tempBuffer, decoder, readCount);
                                audioTrack.write(decoder, 0, readCount);
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

            btnPlay.setText("播放");
            isPlaying = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRecord();
        stop();
    }

}
