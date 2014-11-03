package com.ysag.heisenberg;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "heisenberg";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("heisenberg");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraview);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Native/Java camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraview);

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    private static final Scalar GREEN = new Scalar(0, 255, 0, 255);

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        /* Mat gray = inputFrame.gray(), */
        /*     rgba = inputFrame.rgba(); */

        /* Imgproc.Canny(gray, gray, 50, 200, 3, false); */

        /* Mat lines = new Mat(); */
        /* Imgproc.HoughLinesP(gray, lines, 1, Math.PI / 180., 50, 50, 10); */

        /* Log.d(TAG, "lines is of size: " + lines.rows() + "," + lines.cols() + "," + lines.channels()); */

        /* for (int i = 0; i < lines.cols(); i++) { */
        /*      */
        /*     double[] line = lines.get(0, i); */
        /*     double x1 = line[0]; */
        /*     double y1 = line[1]; */
        /*     double x2 = line[2]; */
        /*     double y2 = line[3]; */

        /*     Point pt1 = new Point(x1, y1), */
        /*           pt2 = new Point(x2, y2); */

        /*     Core.line(rgba, pt1, pt2, GREEN); */
        /* } */

        /* Imgproc.adaptiveThreshold(gray, gray, 255, */
        /*                           Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, */
        /*                           Imgproc.THRESH_BINARY, */
        /*                           5, 0); */
        findLines(rgba.getNativeObjAddr());
        return rgba;
    }

    public native void findLines(long ptrRgba);

}

