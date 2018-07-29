/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Mesh;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.Matrix44F;
import com.vuforia.ObjectTracker;
import com.vuforia.Renderer;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.FileUtils;
import com.vuforia.samples.SampleApplication.utils.ImgUtils;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuGroup;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 此为实现AR虚拟合影的最重要关键的类
 * author：应泽林 qq：376712116
 */

public class ImageTargets extends Activity implements SampleApplicationControl,
    SampleAppMenuInterface
{
    private static final String LOGTAG = "ImageTargets";
    //阿里云主机ip 我绑定了自己的域名
    private String host="http://www.facedoor.top";//172.18.19.213

    SampleApplicationSession vuforiaAppSession;
    
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private ImageTargetRendererObj mRenderer;
    
    private GestureDetector mGestureDetector;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    
    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;
    
    private View mFlashOptionView;
    
    private RelativeLayout mUILayout;
    
    private SampleAppMenu mSampleAppMenu;
    
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    
    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    
    private boolean mIsDroidDevice = false;    

    //放缩识别
    private ScaleGestureDetector mScaleDetector;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        //得到屏幕宽度和高度
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        Constant.width = wm.getDefaultDisplay().getWidth();
        Constant.height = wm.getDefaultDisplay().getHeight();

        vuforiaAppSession = new SampleApplicationSession(this);
        
        startLoadingAnimation();        
        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Tarmac.xml");
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");


        //放缩识别
        mScaleDetector = new ScaleGestureDetector(this.getApplicationContext(), new ScaleGestureListener());
        Constant.x=getResources().getDisplayMetrics().widthPixels/2;
        Constant.y=getResources().getDisplayMetrics().heightPixels/2;
    }
    
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    float scale = 1f;
    public class ScaleGestureListener implements  ScaleGestureDetector.OnScaleGestureListener{
        private float curSpan;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.i(LOGTAG, "ON SCALE");
            float span = detector.getCurrentSpan();
            scale=span/curSpan;
            curSpan = span;
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            curSpan = detector.getCurrentSpan();
            Log.i("zoin","prescale---"+curSpan);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
    }
    
    
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }
    
    
    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }
    
    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new ImageTargetRendererObj(this, vuforiaAppSession);
        mGlView.setRenderer(mRenderer);

    }
    
    
    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.loading_overlay,
            null);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
    }
    
    
    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
            .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;
        
        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();
        
        if (mCurrentDataset == null)
            return false;
        
        if (!mCurrentDataset.load(
            mDatasetStrings.get(mCurrentDatasetSelectionIndex),
            STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;
        
        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;
        
        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }
            
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                + (String) trackable.getUserData());
        }
        
        return true;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
            .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;
        
        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }
            
            mCurrentDataset = null;
        }
        
        return result;
    }

    private View mBottomBar;
    private View mCameraButton;
    private ImageView mPicButton;
    ArrayAdapter spinnerAdapter = null;
    Spinner spinner = null;
    private ArrayList<String> spinnerArray = new ArrayList<>();;          // must use this theme

    private Button translationButton;
    private Button rotateButton;
    private List<ChosedModel> modelList = new ArrayList<ChosedModel>();
    private Button chooseBtn;

    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();
            
            mRenderer.mIsActive = true;
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
//            mUILayout.bringToFront();

            //添加自己的UI布局
            String[] buttonTexts = new String[]{ "旋转", "平移" };
            LayoutInflater inflater = LayoutInflater.from(this);
            mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null,false);

            mUILayout.setVisibility(View.VISIBLE);
            addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            mBottomBar = mUILayout.findViewById(R.id.bottom_bar);
            // Gets a reference to the Camera button
            mCameraButton = mUILayout.findViewById(R.id.camera_button);
            //拍照成功后左下角显示的图片按钮
            mPicButton= (ImageView) mUILayout.findViewById(R.id.pic_button);
            mPicButton.setVisibility(View.VISIBLE);
            mBottomBar.setVisibility(View.VISIBLE);
            mCameraButton.setVisibility(View.VISIBLE);
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            mUILayout.bringToFront();
            LinearLayout ll = new LinearLayout(this);
            ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            ll.setBackgroundColor(Color.WHITE);

            //UI control
            translationButton= (Button) mUILayout.findViewById(R.id.translation);
            rotateButton= (Button) mUILayout.findViewById(R.id.rotate);
            translationButton.setEnabled(false);
            translationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constant.MODE=TRANS;//默认为平移模式
                    translationButton.setEnabled(false);
                    rotateButton.setEnabled(true);
                }
            });
            rotateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constant.MODE=ROTATE;
                    rotateButton.setEnabled(false);
                    translationButton.setEnabled(true);
                }
            });

            initChosedModel();
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
            //设置item间距
            recyclerView.addItemDecoration(new ChatDetailItemDecoration(20));
            ChosedModelAdapter adapter = new ChosedModelAdapter(modelList);
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new ChosedModelAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Constant.world.removeAllObjects();
                    Constant.cylinder.clearRotation(); //清空所有旋转
                    Constant.cylinder.clearTranslation(); //清空所有平移
                    if(position==0){
                        Constant.world.addObject(Constant.cyclinder1);
                        Constant.cyclinder1.rotateX((float) Math.PI);         //默认都是仰视图，模型显示是倒着的，直接加载后沿x轴旋转 使模型变正
                        Constant.cylinder = Constant.cyclinder1;
                    }
                    else if(position==1){
                        Constant.world.addObject(Constant.cyclinder2);
                        Constant.cyclinder2.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder2;
                    }
                    else if(position==2){
                        Constant.world.addObject(Constant.cyclinder3);
                        Constant.cyclinder3.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder3;
                    }
                    else if(position==3){
                        Constant.world.addObject(Constant.cyclinder4);
                        Constant.cyclinder4.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder4;
                    }
                    else if(position==4){
                        Constant.world.addObject(Constant.cyclinder5);
                        Constant.cyclinder5.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder5;
                    }
                    else if(position==5){
                        Constant.world.addObject(Constant.cyclinder6);
                        Constant.cyclinder6.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder6;
                    }
                    else if(position==6){
                        Constant.world.addObject(Constant.cyclinder7);
                        Constant.cyclinder7.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder7;
                    }
                    else if(position==7){
                        Constant.world.addObject(Constant.cyclinder8);
                        Constant.cyclinder8.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder8;
                    }
                    else if(position==8){
                        Constant.world.addObject(Constant.cyclinder9);
                        Constant.cyclinder9.rotateX((float) Math.PI);
                        Constant.cyclinder9.rotateY((float) 3);
                        Constant.cylinder = Constant.cyclinder9;
                    }
                    else if(position==9){
                        Constant.world.addObject(Constant.cyclinder10);
                        Constant.cyclinder10.rotateX((float) Math.PI);
                        Constant.cylinder = Constant.cyclinder10;
                    }



                    recyclerView.setVisibility(View.INVISIBLE);
                }
            });
            recyclerView.setVisibility(View.INVISIBLE);
            chooseBtn= (Button) findViewById(R.id.choose);
            chooseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerView.getVisibility()==View.VISIBLE)
                        recyclerView.setVisibility(View.INVISIBLE);
                    else
                        recyclerView.setVisibility(View.VISIBLE);
                }
            });

//            final Button[] buttons = new Button[buttonTexts.length];
//            for(int i=0; i<buttons.length; i++){
//                buttons[i] = new Button(this);
//                buttons[i].setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
//                buttons[i].setText(buttonTexts[i]);
//                buttons[i].setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Button button = (Button) v;
//                        String text = button.getText().toString();
//                        if( text.equals("旋转") ){
//                            buttons[1].setEnabled(true);
//                            buttons[0].setEnabled(false);
//                            Constant.MODE=ROTATE;
//                            Constant.LASTMODE=Constant.MODE;
//                        }else{
//                            buttons[0].setEnabled(true);
//                            buttons[1].setEnabled(false);
//                            Constant.MODE=TRANS;
//                            Constant.LASTMODE=Constant.MODE;
//                        }
////                        mRenderer.changeMode(text);
////                        mGLView.requestRender();
//                    }
//                });
//                ll.addView(buttons[i]);
//            }
//            buttons[1].setEnabled(false);       // default is transform mode

//            spinnerArray.add("X");
//            spinnerArray.add("玉琮");
//            spinnerArray.add("青铜簋");
//            spinnerArray.add("兽面纹铜尊");
//            // init spinner
//            spinner = new Spinner(this);
//            spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
//            spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//            spinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
//            spinner.setAdapter(spinnerAdapter);
//            spinner.setSelection(0);
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//                //当选中某一个数据项时触发该方法
//            /*
//             * parent接收的是被选择的数据项所属的 Spinner对象，
//             * view参数接收的是显示被选择的数据项的TextView对象
//             * position接收的是被选择的数据项在适配器中的位置
//             * id被选择的数据项的行号
//             */
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
//                    //System.out.println(spinner==parent);//true
//                    //System.out.println(view);
//                    //String data = adapter.getItem(position);//从适配器中获取被选择的数据项
//                    //String data = list.get(position);//从集合中获取被选择的数据项
//                    String data = (String)spinner.getItemAtPosition(position);//从spinner中获取被选择的数据
//                    Toast.makeText(ImageTargets.this, position+"--"+data, Toast.LENGTH_SHORT).show();
//                    Constant.world.removeAllObjects();
//                    Constant.cylinder.clearRotation();
//                    Constant.cylinder.clearTranslation();
//                    if(position==0){
//                        Constant.world.addObject(Constant.cyclinder1);
//                        Constant.cylinder = Constant.cyclinder1;
//                    }
//                    else if(position==1){
//                        Constant.world.addObject(Constant.cyclinder2);
//                        Constant.cylinder = Constant.cyclinder2;
//                    }
//                    else if(position==2){
//                        Constant.world.addObject(Constant.cyclinder3);
//                        Constant.cylinder = Constant.cyclinder3;
//                    }
//                    else if(position==3){
//                        Constant.world.addObject(Constant.cyclinder4);
//                        Constant.cylinder = Constant.cyclinder4;
//                    }
//                    else if(position==4){
//                        Constant.world.addObject(Constant.cyclinder5);
//                        Constant.cylinder = Constant.cyclinder5;
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//                    // TODO Auto-generated method stub
//                }
//            });
//
//            ll.addView(spinner);
//
            addContentView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));



            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            
            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
            
            mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
                mGlView, mUILayout, null);
            setSampleAppMenuSettings();
            
        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    /**
     * 初始化资源文件下的obj文件的 名字 和 图片 加入到集合中，用数组更好，我这里偷懒了
     */
    private void initChosedModel() {
        ChosedModel model1=new ChosedModel("X",R.drawable.p1);
        ChosedModel model2=new ChosedModel("玉琮",R.drawable.p2);
        ChosedModel model3=new ChosedModel("青铜簋",R.drawable.p3);
        ChosedModel model4=new ChosedModel("兽面纹铜尊",R.drawable.p4);
        ChosedModel model5=new ChosedModel("555",R.drawable.p5);
        ChosedModel model6=new ChosedModel("666",R.drawable.p6);
        ChosedModel model7=new ChosedModel("春秋青铜豆",R.drawable.p7);
        ChosedModel model8=new ChosedModel("春秋青铜炉",R.drawable.p8);
        ChosedModel model9=new ChosedModel("西晋青瓷男俑",R.drawable.p9);
        ChosedModel model10=new ChosedModel("西晋青瓷女俑",R.drawable.p10);
        modelList.add(model1);
        modelList.add(model2);
        modelList.add(model3);
        modelList.add(model4);
        modelList.add(model5);
        modelList.add(model6);
        modelList.add(model7);
        modelList.add(model8);
        modelList.add(model9);
        modelList.add(model10);

    }

    String imgName = ""; //保存的文件名
    String directoryPath = "";  //保存绝对路径
    //点击拍照按钮
    public void onCameraClick(View v){
        Bitmap needUploadPic;
        //处理发送过来的消息
        final Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    //接受传递过来的bitmap对象
                    final Bitmap picture = (Bitmap) msg.obj;
                    mPicButton.setImageBitmap(picture);
                    Constant.savedPic=1;//标识左下角的图片按钮是否可用，拍完照后有图片才可点击
                    //开分线程上传图片
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //3.上传拍照图
                            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) { //判断外部存储是否可用
                                directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "Camera";
                            } else {
                                Looper.prepare();//子线程中使用Toast
                                Toast.makeText(ImageTargets.this, "缺少SD卡，无法保存照片", Toast.LENGTH_SHORT).show();
                                Looper.loop();
//            photoPath = null;
                                return ;
                            }
                            File file = new File(directoryPath);

                            String imagePath = directoryPath + File.separator + imgName;

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            picture.compress(Bitmap.CompressFormat.PNG, 90, bos);    //保存为png格式
                            //resizeBmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);
                            File tmpFile=null;
                            try {
                                tmpFile = new File(file, imgName);
                                FileOutputStream fos = new FileOutputStream(tmpFile);
                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = fis.read(buf)) > 0) {
                                    fos.write(buf, 0, len);
                                }
                                fis.close();
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                            //网络传输图片，对应服务端的requestmapping地址
                            String url = host+"/upload";
                            //用okhttp框架做网络请求
                            OkHttpClient okHttpClient = new OkHttpClient();
                            RequestBody requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("title", "wangshu")//这里title好像不重要，图片的名字我在app上传到服务端的时候指定了和保存在app本地的文件名相同
                                    .addFormDataPart("image",imgName ,
                                            RequestBody.create( MediaType.parse("image/png"), tmpFile))
                                    .build();
                            Request request = new Request.Builder()
                                    .url(url)
                                    .post(requestBody)
                                    .build();
                            Call call = okHttpClient.newCall(request);
                            //回调函数没有写，以后需要可以补
                            call.enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    Log.e("TAG","上传失败！");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    Log.e("TAG","上传成功！"+response);
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        /**
         * 这里一开始我是先等待上传完成后才能够显示左下角的拍照图和扫二维码获取图片，但由于网络IO太费时了（服务器太便宜太慢。。），大概拍一张得5s，6s的样子，后来换了个思路做
         *
         * 之后是把网络IO放到最后做的，先在手机本地把ar虚拟合影的图片保存到相册（已经编制好了二维码的），也可以点击查看，但其实此时并不能保证图片已经上传到服务器
         * 因为结合了当时的业务场景，拍完照之后游客会走过来看照片是否满意，在游客走过来的那几秒进行网络IO的传输更为合理
         */
        //1.拍照，用消息队列做的
        mGlView.queueEvent(new Runnable() {
            @Override
            public void run() {
                int width = mGlView.getWidth();
                int height = mGlView.getHeight();
                int screenshotSize = width * height;
                ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
                bb.order(ByteOrder.nativeOrder());
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
                int pixelsBuffer[] = new int[screenshotSize];
                bb.asIntBuffer().get(pixelsBuffer);
                bb = null;
                //这里可能是颜色的色道的问题，记得有个学生做的app里丢失了蓝色通道。。也不知道是不是和这个有关系
                for (int i = 0; i < screenshotSize; ++i) {
                    // The alpha and green channels' positions are preserved while the  red and blue are swapped
                    pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
                //保存到全局变量里
                Constant.picture=bitmap;
                //发送消息给handler，同时把bitmap也给传过去
                Message message = new Message();
                message.what=1;
                message.obj = bitmap;
                mHandler.sendMessage(message);

                //2.根据拍照图片编二维码
                imgName=getImageName();
                ImageView imageView=new ImageView(getApplicationContext());
                Bitmap QRbitmap=createQRImage(host+"/resources/img/"+imgName, imageView,200,200);
                Constant.QRCode=QRbitmap;

                //4.合成照片与二维码并保存到系统相册
                ImgUtils.saveImageToGallery(getApplicationContext(),ImgUtils.mergeBitmap(Constant.picture,Constant.QRCode));
            }
        });
    }

    /**
     * 根据时间戳生成拍照的图片名称，服务器上的图片名称和本地的一致
     * @return
     */
    public String getImageName(){
        final Calendar c = Calendar.getInstance();
        long mytimestamp = c.getTimeInMillis();
        String timeStamp = String.valueOf(mytimestamp);
        String randomName = "zbwz" + timeStamp + ".png";
        return randomName;
    }

    /*
    编制二维码，传入URL，要编制二维码的图片，二维码的宽度，二维码的高度
    author:zoin qq：376712116
     */
    //要转换的地址或字符串,可以是中文
    public static Bitmap createQRImage(String url, ImageView sweepIV, int QR_WIDTH, int QR_HEIGHT ) {
        try {//判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else {
                        pixels[y * QR_HEIGHT + x] = 0xffffffff;
                    }
                }
            }//生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            sweepIV.setImageBitmap(bitmap);
            return bitmap;
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    //点击图片显示大图
    public void onPictureClick(View view){
        //未拍照时点击图片无效
        if(Constant.savedPic==0){
            return ;
        }

        Intent intent = new Intent(this, PicturePreviewActivity.class);
        intent.putExtra("path", Constant.photoPath);
        this.startActivity(intent);
    }
    
    // Shows initialization error messages as System dialogs
    private void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }
                
                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    ImageTargets.this);
                builder
                    .setMessage(errorMessage)
                    .setTitle(getString(R.string.INIT_ERROR))
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                finish();
                            }
                        });
                
                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
    
    
    @Override
    public void onVuforiaUpdate(State state)
    {

        if (mSwitchDatasetAsap)
        {
            Log.e("onVuforiaUpdate","成功");
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                .getClassType());
            if (ot == null || mCurrentDataset == null
                || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }
            
            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());
        
        return result;
    }

    private boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;
    
    
    // This method sets the menu's settings
    private void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;
        
        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);
        
        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_extended_tracking),
            CMD_EXTENDED_TRACKING, false);
        group.addSelectionItem(getString(R.string.menu_contAutofocus),
            CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(
            getString(R.string.menu_flash), CMD_FLASH, false);
        
        CameraInfo ci = new CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++)
        {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }
        
        if (deviceHasBackCamera && deviceHasFrontCamera)
        {
            group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
                true);
            group.addRadioItem(getString(R.string.menu_camera_front),
                CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back),
                CMD_CAMERA_REAR, true);
        }
        
        group = mSampleAppMenu
            .addGroup(getString(R.string.menu_datasets), true);
        mStartDatasetsIndex = CMD_DATASET_START_INDEX;
        mDatasetsNumber = mDatasetStrings.size();
        
        group.addRadioItem("Stones & Chips", mStartDatasetsIndex, true);
        group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);        
        
        mSampleAppMenu.attachMenu();
    }
    
    
    @Override
    public boolean menuProcess(int command)
    {
        
        boolean result = true;
        
        switch (command)
        {
            case CMD_BACK:
                finish();
                break;
            
            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);
                
                if (result)
                {
                    mFlash = !mFlash;
                } else
                {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off
                        : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                        getString(mFlash ? R.string.menu_flash_error_off
                            : R.string.menu_flash_error_on));
                }
                break;
            
            case CMD_AUTOFOCUS:
                
                if (mContAutofocus)
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
                    
                    if (result)
                    {
                        mContAutofocus = false;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_off));
                    }
                } else
                {
                    result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
                    
                    if (result)
                    {
                        mContAutofocus = true;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG,
                            getString(R.string.menu_contAutofocus_error_on));
                    }
                }
                
                break;
            
            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:
                
                // Turn off the flash
                if (mFlashOptionView != null && mFlash)
                {
                    // OnCheckedChangeListener is called upon changing the checked state
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        ((Switch) mFlashOptionView).setChecked(false);
                    } else
                    {
                        ((CheckBox) mFlashOptionView).setChecked(false);
                    }
                }
                
                vuforiaAppSession.stopCamera();
                
                try
                {
                    vuforiaAppSession
                        .startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_FRONT
                            : CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
                } catch (SampleApplicationException e)
                {
                    showToast(e.getString());
                    Log.e(LOGTAG, e.getString());
                    result = false;
                }
                doStartTrackers();
                break;
            
            case CMD_EXTENDED_TRACKING:
                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++)
                {
                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);
                    
                    if (!mExtendedTracking)
                    {
                        if (!trackable.startExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to start extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    } else
                    {
                        if (!trackable.stopExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to stop extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    }
                }
                
                if (result)
                    mExtendedTracking = !mExtendedTracking;
                
                break;
            
            default:
                if (command >= mStartDatasetsIndex
                    && command < mStartDatasetsIndex + mDatasetsNumber)
                {
                    mSwitchDatasetAsap = true;
                    mCurrentDatasetSelectionIndex = command
                        - mStartDatasetsIndex;
                }
                break;
        }
        
        return result;
    }
    
    
    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    float xpos = -1;
    float ypos = -1;
    float rotateX = 0;
    float rotateY = 0;
    float translationX = 0;
    float translationY = 0 ;
    float oldDist;
    float newDist;
    int NONE=0;
    int ROTATE=1;
    int TRANS=2;
    int SCALE=3;
    int mode=NONE;
    float xcenter;
    float ycenter;

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        // TODO Auto-generated method stub
        mScaleDetector.onTouchEvent(me);
        // 按键开始：此为单指按下
        if (me.getAction() == MotionEvent.ACTION_DOWN && me.getPointerCount()== 1) {
            // 保存按下的初始x,y位置于xpos,ypos中
            xpos = me.getX();
            ypos = me.getY();
            return true;
        }
        if(me.getAction()==MotionEvent.ACTION_POINTER_UP){
            xpos=me.getX();
            ypos=me.getY();
        }
        // 按键结束
        if (me.getAction() == MotionEvent.ACTION_UP && me.getPointerCount()==1) {
            // 设置x,y及旋转角度为初始值
            xpos = -1;
            ypos = -1;
            rotateX = 0;
            rotateY = 0;
            translationX = 0;
            translationY = 0;
            return true;
        }
        //手指在滑动时
        if (me.getAction() == MotionEvent.ACTION_MOVE && me.getPointerCount()==1) {
            // 计算x,y偏移位置及x,y轴上的旋转角度
            float xd = me.getX() - xpos;
            float yd = me.getY() - ypos;

            xpos = me.getX();
            ypos = me.getY();
            if(Constant.MODE==ROTATE) {
                rotateX = xd / -100f;
                rotateY = yd / -100f;
            }
            else if(Constant.MODE==TRANS && xd<40f && yd<40f) {
                translationX = xd / 40f;
                translationY = yd / 40f;
            }
            return true;
        }


        // 每Move一下休眠毫秒
        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }
        return super.onTouchEvent(me);
    }



    /**
     * jpec加载obj格式模型
     */
    class ImageTargetRendererObj implements GLSurfaceView.Renderer {

        private static final String LOGTAG = "ImageTargetRendererObj";
        private SampleApplicationSession vuforiaAppSession;
        private ImageTargets mActivity;

        private Renderer mRenderer;
        boolean mIsActive = false;
        //世界对象
        private World world;
        //太阳对象，用来控制光源的
        private Light sun;
        //当前显示的obj对象
        private Object3D mCylinder;
        //预处理好资源文件下的obj对象加载到如下变量中
        private Object3D cylinder1;
        private Object3D cylinder2;
        private Object3D cylinder3;
        private Object3D cylinder4;
        private Object3D cylinder5;
        private Object3D cylinder6;
        private Object3D cylinder7;
        private Object3D cylinder8;
        private Object3D cylinder9;
        private Object3D cylinder10;
        //临时的未融合前的OBJECT3D数组，融合后变为对应的cylinder对象
        private Object3D[] tmp;
        private Object3D[] tmp2;
        private Object3D[] tmp3;
        private Object3D[] tmp4;
        private Object3D[] tmp5;
        private Object3D[] tmp6;
        private Object3D[] tmp7;
        private Object3D[] tmp8;
        private Object3D[] tmp9;
        private Object3D[] tmp10;
        private com.threed.jpct.Camera cam;
        private FrameBuffer fb;
        private float[] modelViewMat;
        private float fov;
        private float fovy;
        private Mesh[] mesh;


        /**
         * 这里是将加载的OBJECT3D 数组里的 融合成一个 OBJECT3D对象
         * @param tmp
         * @return
         */
        public Object3D loadObjFile(Object3D[] tmp){

            Object3D cylinder=null;
            if(tmp != null && tmp.length >= 1){
                Object3D object3D = tmp[0];
                cylinder = object3D;
            }
            cylinder.strip();
            cylinder.build();
            //还是不要手动set吧，预处理纹理，让它自动填充
//            for (Object3D o3 : tmp) {
//
//                o3.calcTextureWrapSpherical();
//
//                if (null != o3.getMesh()) {
//                    //给模型上色
////                    o3.setTexture("YU.jpg");
////                    o3.setTexture("texture");
////                    o3.setTexture("zoin");
//
//                }
//                o3.strip();
//                o3.build();
//            }
            return cylinder;
        }

        //从mtl中解析纹理
        private List<String> parseTextureNames(String mtlPath){
            List<String> mTextureList = new ArrayList<>();
            try {
                File mtlFile = new File(mtlPath);
                String textureName = null;
                InputStream inStream = new FileInputStream(mtlFile);
                Log.i("wenli","[readMtlname]instream:"+(inStream==null?"null":inStream));
                if(inStream != null){
                    InputStreamReader inputReader = new InputStreamReader(inStream);
                    BufferedReader buffReader = new BufferedReader(inputReader);
                    String line;
                    //分行读取
                    while((line = buffReader.readLine())!=null){
                        Log.i("wenli","mtl line: "+line);
                        int idx = line.indexOf("map_Kd");

                        if(idx >= 0){
                            textureName = line.substring(idx+7);

                            if(!mTextureList.contains(textureName)){
                                mTextureList.add(textureName);
                            }
                        }else if((idx=line.indexOf("map_Ka"))>=0){
                            textureName = line.substring(idx+7);
                            if(!mTextureList.contains(textureName)){
                                mTextureList.add(textureName);
                            }
                        }
                    }
                    buffReader.close();
                    inputReader.close();
                    inStream.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mTextureList;
        }
        public Bitmap scaleBitmap(Bitmap bitmap){
            int w= bitmap.getWidth();
            int h= bitmap.getHeight();
            int destW=1024;
            int destH= h*destW/w;
            Bitmap newbm = Bitmap.createScaledBitmap(bitmap, destW, destH, true);
            return newbm;
        }

        //加载所有纹理
        private void loadTextures(String mtlPath){
            List<String> tList = parseTextureNames(mtlPath) ;
            TextureManager tm=TextureManager.getInstance();
            for(int i=0;i < tList.size();i++){
                String name = tList.get(i);
                Log.i("wenli","[preloadtextures] texture name:"+name);
//                if(tm.containsTexture(name)){
//                    tm.removeAndUnload(name,mFb);
//                }
                try {
                    Bitmap bmp = BitmapFactory.decodeStream(getAssets().open(name));
                    Bitmap inputBmp = bmp;
                    int w=bmp.getWidth();
                    if((w&(w-1))!=0){
                        inputBmp=scaleBitmap(bmp);
                    }
                    com.threed.jpct.Texture texture = new com.threed.jpct.Texture(inputBmp);
                    tm.addTexture(name,texture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        /**
         * 这里应该有一个挺大的坑点。。。
         * 在加载模型纹理的时候，要先把obj对应的纹理文件预处理进去，然后加载纹理的时候它会自动进行填充的，
         * 千万不要后面手动set纹理。。又麻烦又不靠谱。。官方给的demo是进行set纹理的
         * @param activity
         * @param session
         */
        public ImageTargetRendererObj(ImageTargets activity, SampleApplicationSession session) {
            mActivity = activity;
            vuforiaAppSession = session;
            //实例化虚拟世界
            world = new World();
            world.setAmbientLight(100, 100, 100);
            // 设置了环境光源强度。负:整个场景会变暗;正:将照亮了一切。
            world.setClippingPlanes(2.0f, 3000.0f);

            sun = new Light(world);
            sun.setIntensity(250, 250, 250);

            // 设置显示的颜色
            if (!TextureManager.getInstance().containsTexture("texture")) {
//                com.threed.jpct.Texture texture = new com.threed.jpct.Texture(BitmapHelper.rescale(BitmapHelper.convert(
//                        mActivity.getResources().getDrawable(R.drawable.b)), 64, 64));
//                //自动填充纹理是靠这个纹理管理器来自动实现的，只需要把对应的名字和对象 放到管理器里面就可以自动填充了
//                TextureManager.getInstance().addTexture("texture", texture);//这是个demo，其实没用到
                try {
                    //加载自己的纹理，放到纹理管理器中，注意这里纹理的像素都是2的幂次的，用geomagic导出来的就好
                    com.threed.jpct.Texture t1 = new com.threed.jpct.Texture(getAssets().open("01Image1.png"));
                    TextureManager.getInstance().addTexture("01Image1.png",t1);
                    com.threed.jpct.Texture t2 = new com.threed.jpct.Texture(getAssets().open("02Image1.png"));
                    TextureManager.getInstance().addTexture("02Image1.png",t2);
                    com.threed.jpct.Texture t3 = new com.threed.jpct.Texture(getAssets().open("03Image1.png"));
                    TextureManager.getInstance().addTexture("03Image1.png",t3);
                    com.threed.jpct.Texture t4 = new com.threed.jpct.Texture(getAssets().open("04Image1.png"));
                    TextureManager.getInstance().addTexture("04Image1.png",t4);
                    com.threed.jpct.Texture t5 = new com.threed.jpct.Texture(getAssets().open("05Image1.png"));
                    TextureManager.getInstance().addTexture("05Image1.png",t5);
                    com.threed.jpct.Texture t6 = new com.threed.jpct.Texture(getAssets().open("06Image1.png"));
                    TextureManager.getInstance().addTexture("06Image1.png",t6);
                    com.threed.jpct.Texture t7 = new com.threed.jpct.Texture(getAssets().open("07Image1.png"));
                    TextureManager.getInstance().addTexture("07Image1.png",t7);
                    com.threed.jpct.Texture t8 = new com.threed.jpct.Texture(getAssets().open("08Image1.png"));
                    TextureManager.getInstance().addTexture("08Image1.png",t8);
                    com.threed.jpct.Texture t9 = new com.threed.jpct.Texture(getAssets().open("09Image1.png"));
                    TextureManager.getInstance().addTexture("09Image1.png",t9);
                    com.threed.jpct.Texture t10 = new com.threed.jpct.Texture(getAssets().open("10Image1.png"));
                    TextureManager.getInstance().addTexture("10Image1.png",t10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.copayAssetsToSdCard(activity.getAssets());
            try {
                //默认显示第一个
                mCylinder = Object3D.mergeAll(Loader.loadOBJ(mActivity.getAssets().open("01.obj"),mActivity.getAssets().open("01.mtl"), 2));
                //这里是调用加载obj的方法，把对应obj模型文件和mtl纹理文件穿进去即可，第三个参数表示一开始缩放倍数，可以自定义
                tmp = Loader.loadOBJ(mActivity.getAssets().open("01.obj"),mActivity.getAssets().open("01.mtl"), 2);
                tmp2 = Loader.loadOBJ(mActivity.getAssets().open("02.obj"),mActivity.getAssets().open("02.mtl"), 2);
                tmp3 = Loader.loadOBJ(mActivity.getAssets().open("03.obj"),mActivity.getAssets().open("03.mtl"), 2);
                tmp4 = Loader.loadOBJ(mActivity.getAssets().open("04.obj"),mActivity.getAssets().open("04.mtl"), 2);
                tmp5 = Loader.loadOBJ(mActivity.getAssets().open("05.obj"),mActivity.getAssets().open("05.mtl"), 2);
                tmp6 = Loader.loadOBJ(mActivity.getAssets().open("06.obj"),mActivity.getAssets().open("06.mtl"), 2);
                tmp7 = Loader.loadOBJ(mActivity.getAssets().open("07.obj"),mActivity.getAssets().open("07.mtl"), 2);
                tmp8 = Loader.loadOBJ(mActivity.getAssets().open("08.obj"),mActivity.getAssets().open("08.mtl"), 2);
                tmp9 = Loader.loadOBJ(mActivity.getAssets().open("09.obj"),mActivity.getAssets().open("09.mtl"), 2);
                tmp10 = Loader.loadOBJ(mActivity.getAssets().open("10.obj"),mActivity.getAssets().open("10.mtl"), 2);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //融合数组变为一个对象
            cylinder1 = loadObjFile(tmp);
            cylinder2 = loadObjFile(tmp2);
            cylinder3 = loadObjFile(tmp3);
            cylinder4 = loadObjFile(tmp4);
            cylinder5 = loadObjFile(tmp5);
            cylinder6 = loadObjFile(tmp6);
            cylinder7 = loadObjFile(tmp7);
            cylinder8 = loadObjFile(tmp8);
            cylinder9 = loadObjFile(tmp9);
            cylinder10 = loadObjFile(tmp10);
            //设置双面显示，3D模型默认只和视角线成锐角的该面可以正常显示（默认是单面显示的），开了双面显示就不会出现从洞中看过去是空心的了
            cylinder1.setCulling(false);
            cylinder2.setCulling(false);
            cylinder3.setCulling(false);
            cylinder4.setCulling(false);
            cylinder5.setCulling(false);
            cylinder6.setCulling(false);
            cylinder7.setCulling(false);
            cylinder8.setCulling(false);
            cylinder9.setCulling(false);
            cylinder10.setCulling(false);
            //默认都是仰视图，模型显示是倒着的，直接加载后沿x轴旋转 使模型变正
            cylinder1.rotateX((float) Math.PI);

//            mCylinder.calcTextureWrapSpherical();
//            mCylinder.setTexture("texture");
//            mCylinder.build();

            Constant.cylinder=cylinder1;
            //往世界中添加自己的模型
            world.addObjects(Constant.cylinder);
            mCylinder.strip();
            cam = world.getCamera();
            Constant.world=world;
            Constant.cyclinder1=cylinder1;
            Constant.cyclinder2=cylinder2;
            Constant.cyclinder3=cylinder3;
            Constant.cyclinder4=cylinder4;
            Constant.cyclinder5=cylinder5;
            Constant.cyclinder6=cylinder6;
            Constant.cyclinder7=cylinder7;
            Constant.cyclinder8=cylinder8;
            Constant.cyclinder9=cylinder9;
            Constant.cyclinder10=cylinder10;

            SimpleVector sv = new SimpleVector();
            sv.set(cylinder1.getTransformedCenter());
            sv.y -= 100;
            sv.z -= 100;
            sun.setPosition(sv);

            MemoryHelper.compact();
        }


        // 显示出模型
        @Override
        public void onDrawFrame(GL10 gl) {
            if (!mIsActive)
                return;
            if(Constant.MODE==ROTATE) {//旋转模式
                if (rotateX != 0) {
                    // 旋转物体的绕Y旋转
                    Constant.cylinder.rotateY(rotateX);//rotate底层是利用JNI调用的C++代码，高通AR的库已经封装好成JAVA的了，很好用
                    // 将rotateX置0
                    rotateX = 0;
                }
                if (rotateY != 0) {
                    // 旋转物体的旋转围绕x由给定角度宽（弧度，逆时针为正值）轴矩阵,应用到对象下一次渲染时。
                    Constant.cylinder.rotateX(rotateY);
                    // 将rotateY置0
                    rotateY = 0;
                }
            }
            else if(Constant.MODE==TRANS){//平移模式
                Constant.cylinder.translate(translationX,translationY,0f);//平移也是封装好的，传参数即可
            }
            if(scale != 1){//放缩模式
                Constant.cylinder.scale(scale);
                scale=1;
            }

            // Call our function to render content
            renderFrame();

            updateCamera();
            world.renderScene(fb);
            world.draw(fb);
            fb.display();
        }


        // Called when the surface is created or recreated.
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

            initRendering(); // NOTE: Cocokin sama cpp - DONE

            // Call Vuforia function to (re)initialize rendering after first use
            // or after OpenGL ES context was lost (e.g. after onPause/onResume):
            vuforiaAppSession.onSurfaceCreated();
        }


        // Called when the surface changed size.
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

            if (fb != null) {
                fb.dispose();
            }
            fb = new FrameBuffer(width, height);

            Config.viewportOffsetAffectsRenderTarget = true;

            updateRendering(width, height);

            // Call Vuforia function to handle render surface size changes:
            vuforiaAppSession.onSurfaceChanged(width, height);
        }


        // Function for initializing the renderer.
        private void initRendering() {
            mRenderer = Renderer.getInstance();

            // Define clear color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }

        private void updateRendering(int width, int height) {

            // Update screen dimensions
            vuforiaAppSession.setmScreenWidth(width);
            vuforiaAppSession.setmScreenHeight(height);

            // Reconfigure the video background
            vuforiaAppSession.configureVideoBackground();

            CameraCalibration camCalibration = com.vuforia.CameraDevice.getInstance().getCameraCalibration();
            Vec2F size = camCalibration.getSize();
            Vec2F focalLength = camCalibration.getFocalLength();
            float fovyRadians = (float) (2 * Math.atan(0.5f * size.getData()[1] / focalLength.getData()[1]));
            float fovRadians = (float) (2 * Math.atan(0.5f * size.getData()[0] / focalLength.getData()[0]));

            if (vuforiaAppSession.mIsPortrait) {
                setFovy(fovRadians);
                setFov(fovyRadians);
            } else {
                setFov(fovRadians);
                setFovy(fovyRadians);
            }

        }

        // The render function.
        private void renderFrame() {
            // clear color and depth buffer
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            // get the state, and mark the beginning of a rendering section
            State state = mRenderer.begin();
            // explicitly render the video background
            mRenderer.drawVideoBackground();

            float[] modelviewArray = new float[16];
            // did we find any trackables this frame?
            for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
                // get the trackable
                TrackableResult result = state.getTrackableResult(tIdx);
                Trackable trackable = result.getTrackable();
                printUserData(trackable);

                Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());
                Matrix44F inverseMV = SampleMath.Matrix44FInverse(modelViewMatrix);
                Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMV);

                modelviewArray = invTranspMV.getData();
                updateModelviewMatrix(modelviewArray);

            }

            mRenderer.end();
        }


        private void printUserData(Trackable trackable) {
            String userData = (String) trackable.getUserData();
            Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
        }

        private void updateModelviewMatrix(float mat[]) {
            modelViewMat = mat;
        }

        private void updateCamera() {
            if (modelViewMat != null) {
                float[] m = modelViewMat;

                final SimpleVector camUp;
                if (vuforiaAppSession.mIsPortrait) {
                    camUp = new SimpleVector(-m[0], -m[1], -m[2]);
                } else {
                    camUp = new SimpleVector(-m[4], -m[5], -m[6]);
                }

                final SimpleVector camDirection = new SimpleVector(m[8], m[9], m[10]);
                final SimpleVector camPosition = new SimpleVector(m[12], m[13], m[14]);

                cam.setOrientation(camDirection, camUp);
                cam.setPosition(camPosition);

                cam.setFOV(fov);
                cam.setYFOV(fovy);
            }
        }

        private void setFov(float fov) {
            this.fov = fov;
        }

        private void setFovy(float fovy) {
            this.fovy = fovy;
        }

    }
}

/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/




