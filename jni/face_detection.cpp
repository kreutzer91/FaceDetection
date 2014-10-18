#include <jni.h>
#include <opencv2/opencv.hpp>
#include <vector>
#include <string>
#include <android/log.h>

using namespace std;
using namespace cv;

#define LOG_TAG "FaceDetector/FaceDetector"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

extern "C" {

JNIEXPORT jlong JNICALL Java_com_example_facedetection_FaceDetector_nativeCreateDetector
(JNIEnv * jenv, jclass, jstring jFileName) {
	LOGD("Java_com_example_facedetection_FaceDetector_nativeCreateDetector enter");
	const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
	string stdFileName(jnamestr);
	jlong result = 0;

	result = (jlong)new CascadeClassifier(stdFileName);
	if (((CascadeClassifier *)result)->empty()) {
		LOGD("Detector fails to create!");
	}

	return result;
}

JNIEXPORT void JNICALL Java_com_example_facedetection_FaceDetector_nativeDetect
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces, jint scaledWidth) {
	LOGD("Java_com_example_facedetection_FaceDetector_nativeDetect enter");

	//int scaledWidth = 320;
	Mat img = *((Mat*)imageGray);

	Mat smallImg;
	float scale = img.cols / (float)scaledWidth;
	if (img.cols > scaledWidth) {
		int scaledHeight = cvRound(img.rows / scale);
		resize(img, smallImg, Size(scaledWidth, scaledHeight));
	}
	else {
		smallImg = img;
	}

	Mat equalizedImg;
	equalizeHist(smallImg, equalizedImg);

	int flags = CASCADE_FIND_BIGGEST_OBJECT | CASCADE_DO_ROUGH_SEARCH;
	Size minFeatureSize(20, 20);
	float searchScaleFactor = 1.1f;
	int minNeighbors = 4;
	vector<Rect> objects;
	((CascadeClassifier *)thiz)->detectMultiScale(equalizedImg, objects, searchScaleFactor, minNeighbors, flags, minFeatureSize);

	*((Mat*)faces) = Mat(objects, true);


	return;
}

}

