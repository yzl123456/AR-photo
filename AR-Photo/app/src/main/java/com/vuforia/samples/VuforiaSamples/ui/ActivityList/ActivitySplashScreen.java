/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.vuforia.samples.VuforiaSamples.R;


public class ActivitySplashScreen extends Activity
{
    
    private static long SPLASH_MILLIS = 0;//不显示此页面
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
            R.layout.splash_screen, null, false);

        addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent intent = new Intent(ActivitySplashScreen.this, AboutScreen.class);
                intent.putExtra("ABOUT_TEXT_TITLE", "Image Target");
                intent.putExtra("ACTIVITY_TO_LAUNCH", "app.ImageTargets.ImageTargets");
                intent.putExtra("ABOUT_TEXT", "ImageTargets/IT_about.html");

                startActivity(intent);

            }

        }, SPLASH_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //        Log.i(LOGTAG, "OnTouchEvent");
        super.onTouchEvent(event);
        Log.i("zoin",event.getAction()+"");
//        mGestureDetector.onTouchEvent(event);
//        boolean result = false;
//
//        if( event.getAction() == MotionEvent.ACTION_DOWN ){
//            touchDownX = event.getX();
////            return true;
//        }
//
//        if( mAppMenu != null && (mAppMenu.isMenuDisplaying() || touchDownX < mGLView.getWidth() / 4) ){
//            // Process the Gestures
//            if (mAppMenu != null && mAppMenu.processEvent(event))
//                return true;
//        }else {
//            mScaleDetector.onTouchEvent(event);
//
//            mRenderer.handleTouchEvent(event);
//            mGLView.requestRender();
//        }

        return true;
    }
}
