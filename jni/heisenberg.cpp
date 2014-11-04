#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <sstream>
#include <android/log.h>

using namespace std;
using namespace cv;

#define  LOG_TAG    "heisenberg"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
JNIEXPORT void JNICALL Java_com_ysag_heisenberg_MainActivity_findLines(JNIEnv*, jobject, jlong addrRgba, jdouble thresholdValue);

JNIEXPORT void JNICALL Java_com_ysag_heisenberg_MainActivity_findLines(JNIEnv*, jobject, jlong addrGray, jdouble thresholdValue)
{
    Mat& gray = *(Mat*)addrGray;
    const double thresh = (double) thresholdValue;

    threshold(gray, gray, thresh, 255, THRESH_BINARY);


    LOGD("threshold: %lf", thresh);

    // Canny(gray, gray, 50, 200, 3, false);
    // 
    // vector<Vec4i> lines;
    // HoughLinesP(gray, lines, 1, CV_PI/180, 80, 30, 10);
    // for (size_t i = 0; i < lines.size(); ++i)
    // {
    //     line(img,
    //          Point(lines[i][0], lines[i][1]),
    //          Point(lines[i][2], lines[i][3]),
    //          Scalar(0, 0, 255),
    //          3,
    //          8);
    // }
}
}
