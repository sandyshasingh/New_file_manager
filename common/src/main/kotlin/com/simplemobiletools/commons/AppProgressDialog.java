package com.simplemobiletools.commons;

import static com.simplemobiletools.commons.ExtensionKt.logException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;


import java.lang.ref.WeakReference;


/**
 * Created by ashishsaini on 31/7/17.
 */

public class AppProgressDialog extends Dialog {
    WeakReference<Context> mContext;

    public AppProgressDialog(Context context) {
        super(context, R.style.appProgressDialog);
        mContext = new WeakReference<>(context);
        setContentView(R.layout.app_progessbar_dailog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setMessage(CharSequence message) {
        ((TextView) findViewById(R.id.message)).setText(message);
    }
    public void setMessage(int messageResId) {
        ((TextView) findViewById(R.id.message)).setText(messageResId);
    }
    @Override
    public void show() {
        boolean shouldShowDialog = false;
        if(mContext != null && mContext.get() != null && mContext.get() instanceof Activity) {
            Activity activity = (Activity) mContext.get();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (activity !=null && !activity.isDestroyed()) {
                    shouldShowDialog = true;
                }
            }else{
                if (activity!=null  && !activity.isFinishing()) {
                    shouldShowDialog = true;
                }
            }
        }
        if(shouldShowDialog) {
            try {
                super.show();
            }catch (Exception e){
                logException(new Throwable("EXception in AppProgressDialog",e));
                if (BuildConfig.DEBUG){
                    throw new WindowManager.BadTokenException("Bad token Exception in Progress Dialog");
                }
            }
        }
    }

}