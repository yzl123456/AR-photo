package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.WindowManager;

import com.threed.jpct.Object3D;
import com.threed.jpct.World;

import java.io.File;

/**
 * Created by 泽林 on 2018/5/17.
 */

/**
 * 全局变量类，存放了全局需要用到的变量
 * author：应泽林 qq：376712116
 */
public class Constant {
    public static Object3D cylinder;
    public static float x;
    public static float y;
    public static int TRANSFORM = 2;
    public static int MODE=TRANSFORM;//初始状态是平移
    public static int LASTMODE;
    public static Bitmap picture;
    public static int picSaved = 0;
    public static String photoPath;

    public static World world;
    public static Object3D cyclinder1;
    public static Object3D cyclinder2;
    public static Object3D cyclinder3;
    public static Object3D cyclinder4;
    public static Object3D cyclinder5;
    public static Object3D cyclinder6;
    public static Object3D cyclinder7;
    public static Object3D cyclinder8;
    public static Object3D cyclinder9;
    public static Object3D cyclinder10;


    public static Bitmap QRCode;
    public static int width;
    public static int height;
    public static String imgName;
    public static File file;

    public static int savedPic = 0;
}
