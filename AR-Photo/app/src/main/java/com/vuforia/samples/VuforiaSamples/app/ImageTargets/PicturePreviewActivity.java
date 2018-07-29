package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import com.vuforia.samples.VuforiaSamples.R;

public class PicturePreviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");

        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        d.getMetrics(dm);
        int h = dm.heightPixels;
        float scale = 0.0f;

        ImageView imagepreview = (ImageView)findViewById(R.id.imagepreview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        scale =  h * 1.0f / bm.getHeight();
//        imagepreview.setScaleX(scale);
//        imagepreview.setScaleY(scale);

//        //merge
//        Bitmap bitmap1 = Constant.picture;;
//        Bitmap bitmap2 = Constant.QRCode;
//        Drawable[] array = new Drawable[2];
//        array[0] = new BitmapDrawable(bitmap1);
//        array[1] = new BitmapDrawable(bitmap2);
//        LayerDrawable la = new LayerDrawable(array);
//        // 其中第一个参数为层的索引号，后面的四个参数分别为left、top、right和bottom
//        la.setLayerInset(0, 0, 0, 0, 0);
//        la.setLayerInset(1, Constant.width-200, Constant.height-200, 0, 0);
//        Drawable dd = (Drawable)la;

//        Bitmap newPic = ((BitmapDrawable)dd).getBitmap();
//        Log.i("zoin","???"+newPic);
//        imagepreview.setImageDrawable(la);
        Bitmap xx = mergeBitmap(Constant.picture, Constant.QRCode);

        imagepreview.setImageBitmap(xx);
//        QRCode.setImageBitmap(Constant.QRCode);
//        Button backButton = (Button)findViewById(R.id.backbutton);
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    private Bitmap mergeBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
        Bitmap bitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(),firstBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(firstBitmap, 0,0, null);
        canvas.drawBitmap(secondBitmap, Constant.width-200,Constant.height-200, null);
        return bitmap;
    }

    private Bitmap mergeBitmap2(Bitmap firstBitmap, Bitmap secondBitmap) {
        Bitmap bitmap = Bitmap.createBitmap(firstBitmap.getWidth()+secondBitmap.getWidth(),
                firstBitmap.getHeight(),firstBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(firstBitmap, new Matrix(), null);
        canvas.drawBitmap(secondBitmap, firstBitmap.getWidth(), 0, null);
        return bitmap;
    }
}
