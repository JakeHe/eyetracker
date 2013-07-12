package org.opencv.samples.fd;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class SampleCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Sample::SurfaceView";

    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;
    private FpsMeter            mFps;
    protected Bitmap			CameraFrame;

    public SampleCvViewBase(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFps = new FpsMeter();
        CameraFrame = null;
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {
                Log.i(TAG, "before mCamera.getSupportedPreviewSizes()");
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                Log.i(TAG, "after mCamera.getSupportedPreviewSizes()");
                int mFrameWidth = width;
                int mFrameHeight = height;

                // selecting optimal camera preview size
                {
                    double minDiff = Double.MAX_VALUE;
                    for (Size size : sizes) {
                        if (Math.abs(size.height - height) < minDiff) {
                            mFrameWidth = (int) size.width;
                            mFrameHeight = (int) size.height;
                            minDiff = Math.abs(size.height - height);
                        }
                    }
                }

                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        //Highgui.CV_CAP_ANDROID       is the camera index of the back camera
        //Highgui.CV_CAP_ANDROID + 1   is the camera index of the front camera 
        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID + 1);
        if (mCamera.isOpened()) {
            (new Thread(this)).start();
        } else {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Failed to open native camera");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (mCamera != null) {
            synchronized (this) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    protected abstract void processFrame(VideoCapture capture);

    public void run() {
        Log.i(TAG, "Starting processing thread");
        mFps.init();

        while (true) {

            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }

                processFrame(mCamera);

                mFps.measure();
            }

            if (CameraFrame != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(CameraFrame, (canvas.getWidth() - CameraFrame.getWidth()) / 2, (canvas.getHeight() - CameraFrame.getHeight()) / 2, null);
                    mFps.draw(canvas, (canvas.getWidth() - CameraFrame.getWidth()) / 2, 0);
                    mHolder.unlockCanvasAndPost(canvas);
                }
                CameraFrame.recycle();
            }
        }

        Log.i(TAG, "Finishing processing thread");
    }
}