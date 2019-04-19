package com.sid.soundrecorderutils;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Button mBtnRecordAudio;
    private Button mBtnPlayAudio;
    public static final int RESULT_CODE_STARTAUDIO=10102;
    private EditText text;
    private Button readButton;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*//根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSans-Regular.otf");
        TextView textView = (TextView) findViewById(R.id.tv_font);
        textView.setTypeface(tf);*/

        initView();
        //初始化TTS
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // 判断是否转化成功
                if (status == TextToSpeech.SUCCESS){
                    //默认设定语言为中文，原生的android貌似不支持中文。
                    int result = tts.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(MainActivity.this, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
                    }else{
                        //不支持中文就将语言设置为英文
                        tts.setLanguage(Locale.US);
                    }
                }
            }
        });
        //文字转语音
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = text.getText().toString().trim();
                if (!TextUtils.isEmpty(str)){
                    // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    tts.setPitch(1.0f);
                    // 设置语速
                    tts.setSpeechRate(1.0f);
                    //播放语音
                    tts.speak(str, TextToSpeech.QUEUE_ADD, null);
                }

            }
        });
        //录音
        mBtnRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RecordAudioDialogFragment fragment = RecordAudioDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), RecordAudioDialogFragment.class.getSimpleName());
                fragment.setOnCancelListener(new RecordAudioDialogFragment.OnAudioCancelListener() {
                    @Override
                    public void onCancel() {
                        fragment.dismiss();
                    }
                });
            }
        });
        //播放
        mBtnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordingItem recordingItem = new RecordingItem();
                SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                final String filePath = sharePreferences.getString("audio_path", "");
                long elpased = sharePreferences.getLong("elpased", 0);
                recordingItem.setFilePath(filePath);
                recordingItem.setLength((int) elpased);
                PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance(recordingItem);
                fragmentPlay.show(getSupportFragmentManager(), PlaybackDialogFragment.class.getSimpleName());
            }
        });

        if (PackageManager.PERMISSION_GRANTED ==   ContextCompat.
                checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
        }else{
            //提示用户开户权限音频
            String[] perms = {"android.permission.RECORD_AUDIO"};
            ActivityCompat.requestPermissions(MainActivity.this,perms, RESULT_CODE_STARTAUDIO);
        }

    }





    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case RESULT_CODE_STARTAUDIO:
                boolean albumAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(!albumAccepted){

                    Toast.makeText(this,"请开启录音权限",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initView() {
        mBtnRecordAudio = (Button)findViewById(R.id.main_btn_record_sound);
        mBtnPlayAudio = (Button) findViewById(R.id.main_btn_play_sound);
        text = (EditText) findViewById(R.id.et_text);
        readButton = (Button) findViewById(R.id.read);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //tts注销
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
