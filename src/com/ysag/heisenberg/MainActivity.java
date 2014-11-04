package com.ysag.heisenberg;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.core.*;
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
import android.widget.*;
import android.media.*;
import android.support.v4.view.*;
import android.view.*;

import java.util.*;

public class MainActivity extends Activity
                          implements CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "heisenberg";

    private double thresholdValue = 127.0;
    private SeekBar thresholdSeekBar = null;
    private boolean initialized = false;
    private SystemState state = SystemState.UNINITIALIZED;
    private boolean toCalc = true;
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

        /* thresholdSeekBar = (SeekBar) findViewById(R.id.threshold_seek_bar); */
        /* thresholdSeekBar.setOnSeekBarChangeListener(this); */

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraview);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double max = (double) seekBar.getMax();
        thresholdValue = 255 * progress / max;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

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
        state = SystemState.UNINITIALIZED;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch(action) {

        case (MotionEvent.ACTION_UP) :
            // Stop playing here
            /* Log.d(tag,"Action was UP"); */
            if (state == SystemState.UNINITIALIZED) {
                state = SystemState.PALM_COLORS_OBTAINED;
            }
            return true;

        default : 
            return super.onTouchEvent(event);
        }      
    }

    private static final Scalar GREEN = new Scalar(0, 255, 0, 255);

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray(),
            rgba = inputFrame.rgba();

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
        switch (this.state) {
        case UNINITIALIZED:
            drawRects(rgba);
            return rgba;
        case PALM_COLORS_OBTAINED:
            Log.d(TAG, "Here in the case, about to call calculatePalmColorInfo()");
            calculatePalmColorInfo(rgba);
            return rgba;
        case INITIALIZED:
            Log.d(TAG, "rgba: " + rgba.rows() + "," + rgba.cols() + "," + rgba.channels());
            Imgproc.cvtColor(main(rgba), rgba, Imgproc.COLOR_GRAY2RGBA);
            Imgproc.pyrUp(rgba, rgba);
            Log.d(TAG, "rgba: " + rgba.rows() + "," + rgba.cols() + "," + rgba.channels());
            return rgba;
        }
        Log.d(TAG, "NO state is good!");
        return rgba;
        /* findLines(gray.getNativeObjAddr(), thresholdValue); */
        /* Log.d(TAG, "got result, returning"); */
        /* return gray; */
    }

    private ROI[] rois = null;

    // 7 samples, 3 channels per sample
    private int[][] avgColors = new int[7][3];

    private int[][] loTolerances = new int[][] {
        {12, 30, 80},
        {12, 30, 80},
        {12, 30, 80},
        {12, 30, 80},
        {12, 30, 80},
        {12, 30, 80},
        {12, 30, 80},
    };

    private int[][] hiTolerances = new int[][] {
        {7, 40, 80},
        {7, 40, 80},
        {7, 40, 80},
        {7, 40, 80},
        {7, 40, 80},
        {7, 40, 80},
        {7, 40, 80},
    };

    private void calculatePalmColorInfo(Mat rgba) {
        Log.d(TAG, "in calculatePalmColorInfo()");
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGB2HLS);
        for (int i = 0; i < 7; ++i) {
            Log.d(TAG, "Updating sample " + i);
            updateColorForSample(i, rgba);
        }
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_HLS2RGB);
        state = SystemState.INITIALIZED;
        Log.d(TAG, "Returning fom calculatePalmColorInfo()");
    }

    private void updateColorForSample(int idx, Mat img) {
        ROI r = rois[idx];
        Rect roiRect = new Rect(r.topLeft, r.bottomRight);
        Mat roi = new Mat(img, roiRect);
        
        List<List<Integer>> roiChannels = new ArrayList<List<Integer>>();

        roiChannels.add(new ArrayList<Integer>());
        roiChannels.add(new ArrayList<Integer>());
        roiChannels.add(new ArrayList<Integer>());


        for (int i = 0; i < roi.rows(); ++i) {
            for (int j = 0; j < roi.cols(); ++j) {
                double[] px = roi.get(i, j);
                for (int k = 0; k < 3; ++k) {
                    roiChannels.get(k).add((int) px[k]);
                }
            }
        }

        for (int k = 0; k < 3; ++k) {
            List<Integer> chan = roiChannels.get(k);
            Collections.sort(chan);
            int size = chan.size();
            int median;
            if (size % 2 == 0) {
                median = chan.get(size / 2 - 1);
            } else {
                median = chan.get(size / 2);
            }
            avgColors[idx][k] = median;
        }
    }

    public native void findLines(long ptrRgba, double thresholdValue);

    public void drawRects(Mat img) {
        if (rois == null) { 
            int width = img.cols(), height = img.rows();
            int sqlen = 15;
            rois = new ROI[] {
                new ROI(img, width/3, height/6, width/3 + sqlen, height/6 + sqlen),
                new ROI(img, width/4, height/2, width/4 + sqlen, height/2 + sqlen),
                new ROI(img, width/3, height/1.5, width/3 + sqlen, height/1.5 + sqlen),
                new ROI(img, width/2, height/2, width/2 + sqlen, height/2 + sqlen),
                new ROI(img, width/2.5, height/2.5, width/2.5 + sqlen, height/2.5 + sqlen),
                new ROI(img, width/2, height/1.5, width/2 + sqlen, height/1.5 + sqlen),
                new ROI(img, width/2.5, height/1.8, width/2.5 + sqlen, height/1.8 + sqlen),
            };
        }
        for (int i = 0; i < rois.length; ++i) {
            rois[i].drawRect(img);
        }
    }

    public void rect(Mat img, int x1, int y1, int x2, int y2) {
        Core.rectangle(img, new Point(x1, y1), new Point(x2, y2), GREEN, 1);
    }

    private static enum SystemState {
        UNINITIALIZED,
        PALM_COLORS_OBTAINED,
        INITIALIZED
    }

    private Mat binarize(Mat img) {
        Mat[] bwImages = new Mat[7];
        Mat bw = Mat.zeros(img.rows(), img.cols(), CvType.CV_8U);

        for (int i = 0; i < 7; ++i) {
            bwImages[i] = new Mat();
            Scalar lo = new Scalar(clamp(avgColors[i][0] - loTolerances[i][0]),
                                   clamp(avgColors[i][1] - loTolerances[i][1]),
                                   clamp(avgColors[i][2] - loTolerances[i][2]));

            Scalar hi = new Scalar(clamp(avgColors[i][0] + hiTolerances[i][0]),
                                   clamp(avgColors[i][1] + hiTolerances[i][1]),
                                   clamp(avgColors[i][2] + hiTolerances[i][2]));

            Mat tmp = new Mat();
            Core.inRange(img, lo, hi, tmp);
            Log.d(TAG, "" + tmp.rows() + "," + tmp.cols() + "," + tmp.channels());
            Core.add(bw, tmp, bw);
        }
        Imgproc.medianBlur(bw, bw, 7);
        return bw;
    }

    
    private Mat main(Mat img) {
        Log.d(TAG, "in main()");
        Mat down = new Mat();
        Imgproc.pyrDown(img, down);
        Imgproc.blur(down, down, new Size(3,3));
        Imgproc.cvtColor(down, down, Imgproc.COLOR_RGB2HLS);
        return binarize(down); 
        // Imgproc.cvtColor(down, down, Imgproc.COLOR_HLS2RGB);

    }

    private int clamp(int value) { 
        return clamp(value, 0, 255);
    }

    private int clamp(int value, int lo, int hi) {
        if (value > hi) {
            return hi;
        }
        if (value < lo) {
            return lo;
        }
        return value;
    }
}

