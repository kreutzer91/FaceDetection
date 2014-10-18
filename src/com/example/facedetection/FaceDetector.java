package com.example.facedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class FaceDetector {
	private long mNativeObj = 0;
	private int mDetectionWidth = 0;
	
	public FaceDetector(String cascadeFileName, int detectionWidth) {
		mDetectionWidth = detectionWidth;
		mNativeObj = nativeCreateDetector(cascadeFileName);
	}
	
	public void detect(Mat imgGray, MatOfRect face) {
		nativeDetect(mNativeObj, imgGray.getNativeObjAddr(), face.getNativeObjAddr(), mDetectionWidth);
	}
	
	public void rescale(Rect face, int cols) {
		float scale = cols / (float)mDetectionWidth;
		if (cols > mDetectionWidth) {
			face.x = (int)(face.x * scale);
			face.y = (int)(face.y * scale);
			face.width = (int)(face.width * scale);
			face.height = (int)(face.height * scale);
		}
	}
	
	private static native long nativeCreateDetector(String cascadeFileName);
	
	private static native void nativeDetect(long thiz, long imgGray, long face, int scaledWidth);
}
