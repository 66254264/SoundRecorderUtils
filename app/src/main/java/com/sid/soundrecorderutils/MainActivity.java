package com.sid.soundrecorderutils;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG ="testjson" ;
    private Button mBtnRecordAudio;
    private Button mBtnPlayAudio;
    private WebView wvCustom;
    public static final int RESULT_CODE_STARTAUDIO=10102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //initWebView();
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

    private void initWebView() {
        wvCustom.clearCache(true);
        WebSettings webSettings = wvCustom.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        wvCustom.addJavascriptInterface(new Object() {

            //录制音频
            @JavascriptInterface
            public void RecordAudio(){
                final RecordAudioDialogFragment fragment = RecordAudioDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), RecordAudioDialogFragment.class.getSimpleName());
                fragment.setOnCancelListener(new RecordAudioDialogFragment.OnAudioCancelListener() {
                    @Override
                    public void onCancel() {
                        fragment.dismiss();
                    }
                });
            }
            //结束录制音频
            @JavascriptInterface
            public String finishRecordAudio(){
                SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                final String filePath = sharePreferences.getString("audio_path", "");
                String audioBytes = Utils.FileToStr(filePath);
                JsonObject json = new JsonObject();
                json.addProperty("data",audioBytes);
                json.addProperty("type","mp4");
                Log.i(TAG, "finishRecordAudio: "+json.toString());
                return json.toString();
            }
            //播放音频
            @JavascriptInterface
            public void PlayAudio(){
                RecordingItem recordingItem = new RecordingItem();
                SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                final String filePath = sharePreferences.getString("audio_path", "");
                long elpased = sharePreferences.getLong("elpased", 0);
                recordingItem.setFilePath(filePath);
                recordingItem.setLength((int) elpased);
                PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance(recordingItem);
                fragmentPlay.show(getSupportFragmentManager(), PlaybackDialogFragment.class.getSimpleName());
            }

            @JavascriptInterface
            public void finishActivity() {
                finish();
            }

        }, "android");
        wvCustom.loadUrl("http://172.18.158.34:8080/demo");
        wvCustom.setWebChromeClient(new WebChromeClient());
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
        wvCustom= (WebView) findViewById(R.id.wv_web_test);
    }
}
