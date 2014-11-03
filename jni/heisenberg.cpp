#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_com_ysag_heisenberg_MainActivity_findLines(JNIEnv*, jobject, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_ysag_heisenberg_MainActivity_findLines(JNIEnv*, jobject, jlong addrRgba)
{
    Mat& img = *(Mat*)addrRgba;
    Mat gray;
    cvtColor(img, gray, CV_RGB2GRAY);

    Canny(gray, gray, 50, 200, 3, false);
    
    vector<Vec4i> lines;
    HoughLinesP(gray, lines, 1, CV_PI/180, 80, 30, 10);
    for (size_t i = 0; i < lines.size(); ++i)
    {
        line(img,
             Point(lines[i][0], lines[i][1]),
             Point(lines[i][2], lines[i][3]),
             Scalar(0, 0, 255),
             3,
             8);
    }
}
}
