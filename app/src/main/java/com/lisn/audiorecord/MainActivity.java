package com.lisn.audiorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lisn.rxpermissionlibrary.permissions.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        mContext = this;
        bt1 = findViewById(R.id.btn1);
        bt2 = findViewById(R.id.btn2);
        bt3 = findViewById(R.id.btn3);
        bt4 = findViewById(R.id.btn4);

        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);
        bt3.setOnClickListener(this);
        bt4.setOnClickListener(this);
    }

    private void getPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        Observable<Boolean> request = rxPermissions.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO);
        request.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (!aBoolean) {
                    Log.e("---", "accept: 请开启相关权限");
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == bt1) {
            start(AudioRecordActivity.class);
        } else if (v == bt2) {
            start(MediaRecordActivity.class);

        } else if (v == bt3) {

        } else if (v == bt4) {
            start(PCM2G711aActivity.class);
        }
    }

    private void start(Class cls) {
        Intent intent = new Intent(mContext, cls);
        startActivity(intent);
    }
}
