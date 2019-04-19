package com.sid.soundrecorderutils;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Created by IT_Hz.
 * Date: 2019/4/19
 * Time: 11:16
 */
public class MyApplication extends Application {
    //全局字体路径，一般放置在assets/fonts目录
    private static final String fontPath = "fonts/SourceHanSans-Regular.otf";
    @Override
    public void onCreate() {
        super.onCreate();
        replaceSystemDefaultFont(getApplicationContext(), fontPath);
    }


    public void replaceSystemDefaultFont(Context context, String fontPath) {
        replaceTypefaceField("MONOSPACE", Typeface.createFromAsset(context.getAssets(), fontPath));
    }

    //关键--》通过修改MONOSPACE字体为自定义的字体达到修改app默认字体的目的
    private void replaceTypefaceField(String fieldName, Object value) {
        try {
            Field defaultField = Typeface.class.getDeclaredField(fieldName);
            defaultField.setAccessible(true);
            defaultField.set(null, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
