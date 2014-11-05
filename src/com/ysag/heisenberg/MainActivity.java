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
    private static final Scalar BLUE = new Scalar(255, 0, 0, 255);
    private static final Scalar YELLOW = new Scalar(255, 255, 0, 255);
    private static final Scalar RED = new Scalar(0, 0, 255, 255);

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray(),
            rgba = inputFrame.rgba();
        switch (this.state) {
        case UNINITIALIZED:
            drawRects(rgba);
            return rgba;
        case PALM_COLORS_OBTAINED:
            calculatePalmColorInfo(rgba);
            return rgba;
        case INITIALIZED:
            return main(rgba);
        }
        return rgba;
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
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGB2HLS);
        for (int i = 0; i < 7; ++i) {
            updateColorForSample(i, rgba);
        }
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_HLS2RGB);
        state = SystemState.INITIALIZED;
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
            Core.add(bw, tmp, bw);
        }
        Imgproc.medianBlur(bw, bw, 7);
        return bw;
    }

    
    private Mat main(Mat img) {
        Mat down = new Mat();
        Imgproc.pyrDown(img, down);
        Imgproc.blur(down, down, new Size(3,3));
        Imgproc.cvtColor(down, down, Imgproc.COLOR_RGB2HLS);
        Mat bin = binarize(down);
        Mat ret = new Mat();
        Imgproc.pyrUp(bin, ret);
        Imgproc.Canny(ret, ret, 100, 200, 3, false);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(ret, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int theIdx = biggestContourIndex(contours);
        if (theIdx >= 0) {
            Imgproc.drawContours(img, contours, theIdx, GREEN, 3);
            MatOfInt convexHull = new MatOfInt();
            MatOfPoint handContour = contours.get(theIdx);
            Imgproc.convexHull(handContour, convexHull);
            MatOfInt4 convexityDefects = new MatOfInt4();
            Imgproc.convexityDefects(handContour, convexHull, convexityDefects);

            Rect boundingRect = Imgproc.boundingRect(handContour);

            for (int i = 0; i < convexityDefects.rows(); ++i) {
                int[] data = new int[4];
                convexityDefects.get(i, 0, data);

                Point startPoint = getPoint(handContour, data[0]),
                      endPoint = getPoint(handContour, data[1]),
                      farPoint = getPoint(handContour, data[2]);

                if (isSignificantDefect(startPoint, endPoint, farPoint, boundingRect)) {
                    Core.circle(img, startPoint, 3, BLUE, -1);
                    Core.circle(img, endPoint, 3, YELLOW, -1);
                    Core.circle(img, farPoint, 3, RED, -1);
                }
            }
        }
        
        return img;
    }

    private static Point getPoint(MatOfPoint points, int idx) {
        int[] point = new int[2];
        points.get(idx, 0, point);
        return new Point(point[0], point[1]);
    }

    private static double dist(Point p1, Point p2) { 
        double t1 = p2.x - p1.x,
               t2 = p2.y - p1.y;
        
        return Math.sqrt(t1 * t1 + t2 * t2);
    }

    private static double angle(Point a, Point elbow, Point c) {
        double l1 = dist(a, elbow),
               l2 = dist(elbow, c);
        
        double dot = (a.x - elbow.x) * (c.x - elbow.x) +
                     (a.y - elbow.y) * (c.y - elbow.y);

        double angle = Math.acos(dot / (l1 * l2));
        angle *= (180.0 / Math.PI);
        return angle;
    }

    private boolean isSignificantDefect(Point start, Point end, Point far, Rect boundingRect) {
        double tolerance = boundingRect.height / 5.0;
        double angleTolerance = 95.0;
        
        if (dist(start, far) > tolerance &&
            dist(end, far) > tolerance &&
            angle(start, far, end) < angleTolerance)
        {
            double d = boundingRect.y + 3 * boundingRect.height / 4;
            if (end.y <= d && start.y <= d) {
                return true;
            }
        } 
        return false;
    }


    private static Point pointFromArray(int[] arr) {
        return new Point(arr[0], arr[1]);
    }

    private int biggestContourIndex(List<MatOfPoint> contours) {
        int maxsize = 0;
        int maxidx = -1;
        for (int i = 0; i < contours.size(); ++i) {
            MatOfPoint contour = contours.get(i);
            // A MatOfPoint of n points is stored as a n x 1 matrix, with
            // each entry M(i,j) corresponding to two channels, which are
            // the X and Y values of the Point.
            if (contour.rows() > maxsize) {
                maxsize = contour.rows();
                maxidx = i;
            }
        }
        return maxidx;
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

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}

