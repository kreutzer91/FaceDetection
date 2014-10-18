package com.example.facedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OpenCV4AndroidExample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
	private static final int defaultMode = 0;
	private static final int trainMode = 1;
	private static final int testMode = 2;
	private static final int detectionWidth = 320;
	
	private MenuItem mDefaultMode;
	private MenuItem mTrainMode;
	private MenuItem mTestMode;
	
	private CameraBridgeViewBase mOpenCvCameraView;
	private File mCascadeFile;
	private FaceDetector mFaceDetector;
	private Mat mRgba;
	private Mat mGray;
	private int mMode;
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: 
                {
                	Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("face_detection");
					try {
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
	                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
	                    mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
	                    FileOutputStream os = new FileOutputStream(mCascadeFile);
					
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();
					
						mOpenCvCameraView.enableView();
                    
						mFaceDetector = new FaceDetector(mCascadeFile.getAbsolutePath(), detectionWidth);
						cascadeDir.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }
	
	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);	
		mGray = new Mat(height, width, CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
	    
		MatOfRect faces = new MatOfRect();
		mFaceDetector.detect(mGray, faces);
		
		Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
        	mFaceDetector.rescale(facesArray[i], mGray.cols());
        	Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        
		return mRgba;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mDefaultMode = menu.add("Default");
        mTrainMode = menu.add("Add New Person");
        mTestMode = menu.add("Recognize Face");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mDefaultMode)
            mMode = defaultMode;
        else if (item == mTrainMode)
            mMode = trainMode;
        else if (item == mTestMode)
            mMode = testMode;
        return true;
    }

}

