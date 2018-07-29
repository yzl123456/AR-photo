/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Mesh;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;
import com.vuforia.CameraCalibration;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.FileUtils;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.VuforiaSamples.R;
import java.io.FileInputStream;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 * jpec加载MD2动画格式模型
 */

public class ImageTargetRendererMd2 implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "ImageTargetRendererObj";
    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Renderer mRenderer;
    boolean mIsActive = false;

    private World world;
    private Light sun;
    private Object3D cylinder;
    private Object3D[] tmp;
    private Camera cam;
    private FrameBuffer fb;
    private float[] modelViewMat;
    private float fov;
    private float fovy;
    private Mesh[] mesh;

    // 行走动画 相关参数
    private int an = 2;
    private float ind = 0;
    public ImageTargetRendererMd2(ImageTargets activity, SampleApplicationSession session) {
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
            Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(
                    mActivity.getResources().getDrawable(R.drawable.soilder)), 64, 64));
            TextureManager.getInstance().addTexture("texture", texture);
        }
        FileUtils.copayAssetsToSdCard(activity.getAssets());
        try {
            Object3D object3D = Loader.loadMD2(new FileInputStream(FileUtils.path + "soilder.md2"),3f);
            cylinder=object3D;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException",e.getMessage());
        }

        cylinder.strip();
        cylinder.build();
        //这才是将纹理添加进去
        cylinder.setTexture("texture");


        world.addObjects(cylinder);
        // world.addObjects(hi);

        cam = world.getCamera();

        SimpleVector sv = new SimpleVector();
        sv.set(cylinder.getTransformedCenter());
        sv.y -= 100;
        sv.z -= 100;
        sun.setPosition(sv);

       /* sv = new SimpleVector();

        // sv.set(hi[0].getTransformedCenter());
        sv.y -= 5000;
        sv.z -= 5000;
        sv.rotateX(180);
        sun.setPosition(sv);*/

        // for older Android versions, which had massive problems with garbage collection
        MemoryHelper.compact();

    }
    /**
     * 实现动画的代码
     * */
    private void doAnim() {
        // TODO Auto-generated method stub
        //每一帧加0.018f
        ind += 0.018f;
        if (ind > 1f) {
            ind -= 1f;
        }
        // 关于此处的两个变量，ind的值为0-1(jpct-ae规定),0表示第一帧，1为最后一帧；
        //至于an这个变量，它的意思是sub-sequence如果在keyframe(3ds中),因为在一个
        //完整的动画包含了seq和sub-sequence，所以设置为2表示执行sub-sequence的动画，
        //但这里设置为2我就不太明白了，不过如果不填，效果会不自然，所以我就先暂时把它
        //设置为2
        cylinder.animate(ind, an);
    }

    // 显示出模型
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;
        doAnim();
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
        // hide the objects when the targets are not detected
        if (state.getNumTrackableResults() == 0) {
            float m[] = {
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, -10000, 1
            };
            modelviewArray = m;
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
