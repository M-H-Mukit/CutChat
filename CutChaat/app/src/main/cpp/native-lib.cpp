#include <jni.h>
#include <string>
#include <opencv2/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp>


using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_mukit_cutchaat_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mukit_cutchaat_ForegroundSegmentationActivity_imageFromJNI(JNIEnv *env, jobject instance,
                                                                    jlong inputImage,
                                                                    jlong outputImage) {

    // TODO
    Mat &mat = *(Mat *) inputImage;
    Mat &outputMat = *(Mat *) outputImage;
    Mat srcImgMat = mat;


    //graying image
    Mat grayImgMat;
    cvtColor(srcImgMat, grayImgMat, COLOR_BGR2GRAY);

    //Blurring Image for noise reduction
    Mat blurImgMat;
    GaussianBlur(grayImgMat, blurImgMat, Size(5, 5), 0, 0, BORDER_DEFAULT);

    //Edge detection using Canny algorithm
    Mat edgeImgMat;
    int lowThreshold = 12;
    int highThreshold = lowThreshold * 3 + 10;
    int kernel_size = 3;
    Canny(blurImgMat, edgeImgMat, lowThreshold, highThreshold, kernel_size);

    // Dilate Image
    Mat dilateImgMat = edgeImgMat;
    int dilateType = MORPH_ELLIPSE;
    int dilateSize = 2;
    Mat elementDilate = getStructuringElement(dilateType,
                                              Size(2 * dilateSize + 1, 2 * dilateSize + 1),
                                              Point(dilateSize, dilateSize));
    dilate(edgeImgMat, dilateImgMat, elementDilate);

    //5. Flood fill
    Mat floodFilled = cv::Mat::zeros(dilateImgMat.rows + 2, dilateImgMat.cols + 2, CV_8U);
    floodFill(dilateImgMat, floodFilled, cv::Point(0, 0), 0, 0, cv::Scalar(), cv::Scalar(),
              4 + (255 << 8) + cv::FLOODFILL_MASK_ONLY);
    floodFilled = cv::Scalar::all(255) - floodFilled;
    Mat temp;
    floodFilled(Rect(1, 1, dilateImgMat.cols - 2, dilateImgMat.rows - 2)).copyTo(temp);
    floodFilled = temp;

    //7. Find largest contour
    int largestArea = 0;
    int largestContourIndex = 0;
    Rect boundingRectangle;
    Mat largestContour(srcImgMat.rows, srcImgMat.cols, CV_8UC1, Scalar::all(0));
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    vector<Point> l_contours;
    findContours(floodFilled, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_NONE);

    for (int i = 0; i < contours.size(); i++) {
        double a = contourArea(contours[i], false);
        if (a > largestArea) {
            largestArea = a;
            largestContourIndex = i;
            boundingRectangle = boundingRect(contours[i]);
        }
    }

    Scalar color(255, 255, 255);
    drawContours(largestContour, contours, largestContourIndex, color, FILLED, 8, hierarchy);

    //erode
    Mat erodedFloodFill;
    int erosionType = MORPH_ELLIPSE;
    int erosionSize = 4;
    Mat erosionElement = getStructuringElement(erosionType,
                                               Size(2 * erosionSize + 1, 2 * erosionSize + 1),
                                               Point(erosionSize, erosionSize));
    erode(largestContour, erodedFloodFill, erosionElement);

    Mat maskedSrc;
    srcImgMat.copyTo(maskedSrc, erodedFloodFill);


    // 8. Mask original image
    // Smoothing edge
    Mat img = maskedSrc;
    Mat whole_image = srcImgMat;
    whole_image.convertTo(whole_image, CV_32FC3, 1.0 / 255.0);
    cv::resize(whole_image, whole_image, img.size());
    img.convertTo(img, CV_32FC3, 1.0 / 255.0);

    // Prepare mask
    Mat mask;
    Mat img_gray;
    cv::cvtColor(img, img_gray, cv::COLOR_BGR2GRAY);
    img_gray.convertTo(mask, CV_32FC1);
    threshold(1.0 - mask, mask, 0.9, 1.0, cv::THRESH_BINARY_INV);

    GaussianBlur(mask, mask, Size(21, 21), 4.0);

    Mat res;
    vector<Mat> ch_img(3);
    cv::split(whole_image, ch_img);

    ch_img[0] = ch_img[0].mul(mask);
    ch_img[1] = ch_img[1].mul(mask);
    ch_img[2] = ch_img[2].mul(mask);


    cv::merge(ch_img, res);

    double minVal, maxVal;
    minMaxLoc(res, &minVal, &maxVal);
    Mat draw,out;
    res.convertTo(draw, CV_8UC3, 255.0 / (maxVal - minVal), -minVal);
    draw.copyTo(out,erodedFloodFill);
    outputMat = out;
}
