 package org.opencv.samples.fd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

class FdView extends SampleCvViewBase {
    private static final String TAG = "Sample::FdView";
    private static final String TAG1 = "Face";
    private static final String TAG2 = "Eye";
    private static final float MIN_EYE_TO_FACE_RATIO = 0.1f;
    private static final float MAX_EYE_TO_FACE_RATIO = 0.4F;
    private Mat                 mRgba;
    private Mat                 mGray;
    private Mat					mFace;

    private CascadeClassifier   mCascade;
    private CascadeClassifier	mEyesCascade;

    public FdView(Context context,  AttributeSet attributeSet) {
        super(context, attributeSet);

        try {
            //copy file res/raw/lbpcascade_frontalface.xml to cascade/lbpcascade_frontalface.xml
        	
        	InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            //create a new directory called "cascade" in your application
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            //create "lbpcascade_frontalface.xml" in directory "cascade"
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            //write to "lbpcascade_frontalface.xml"
            FileOutputStream os = new FileOutputStream(cascadeFile);

            //copy file from res/raw/ to cascade/
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            
            //copy file res/raw/haarcascade_eye_tree_eyeglasses.xml to cascade/haarcascade_eye_tree_eyeglasses.xml
            is = context.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
            File eyesCascadeFile = new File(cascadeDir, "haarcascade_eye_tree_eyeglasses.xml");
            os = new FileOutputStream(eyesCascadeFile);
            
            ///copy bytes 
            while ((bytesRead = is.read(buffer)) != -1) {
            	os.write(buffer, 0, bytesRead);
            }
            
            // close input and out stream
            is.close();
            os.close();

            mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mCascade.empty()) {
                Log.e(TAG, "Failed to load face cascade classifier");
                mCascade = null;
            } else
                Log.i(TAG, "Loaded face cascade classifier from " + cascadeFile.getAbsolutePath());
            
            mEyesCascade = new CascadeClassifier(eyesCascadeFile.getAbsolutePath());
            if (mEyesCascade.empty()) {
            	Log.i(TAG, "Failed to load eyes cascade classifier");
            } else 
            	Log.i(TAG, "Loaded eyes cascade classifier from" + eyesCascadeFile.getAbsolutePath());

            cascadeFile.delete();
            eyesCascadeFile.delete();
            //make directory empty before deletion
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mFace = new Mat();
        }
    }

    @Override
    protected void processFrame(VideoCapture capture) {
        capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);

        if (mCascade != null) {
            int height = mGray.rows();
            int faceSize = Math.round(height * FdActivity.minFaceSize);
            MatOfRect faces = new MatOfRect();
            mCascade.detectMultiScale(mGray, faces, 1.3, 2, 2 // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    , new Size(faceSize, faceSize), new Size());

            //we only care the largest face only
            if (faces.toArray().length != 0) {
            	Rect r = faces.toArray()[0];
            	Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
                Log.i(TAG1, " Top Left = (" + r.tl().x + ", " + r.tl().y + "); Bottom Right = (" + r.br().x + ", " + r.br().y + " )" );
            
                if (mEyesCascade != null) {
                		//crash the program      //make a deep copy of a rectangular region of the mat as face
                		//\                      //mFace =  (new Mat(mGray, r)).clone();  
                	//narrow down the search area, by extracting sub mat from the entire surface
                	mFace = mGray.submat(r);
                	//create MatOfRect to hold eyes 
                	MatOfRect eyes = new MatOfRect();
                    int minEyeSize = (int) Math.round(faceSize * MIN_EYE_TO_FACE_RATIO); 
                    int maxEyeSize = (int) Math.round(faceSize * MAX_EYE_TO_FACE_RATIO);
                	mEyesCascade.detectMultiScale(mFace, eyes, 1.2, 2, 2, new Size(minEyeSize , minEyeSize) , 
                			new Size(maxEyeSize, maxEyeSize));
                	
                	if (eyes.toArray().length >= 2) {
                		//locate first eye
                		  Rect e = eyes.toArray()[0];
                		  Core.rectangle(mRgba, new Point(r.x + e.x, r.y + e.y), 
                  				new Point(r.x + e.x + e.width, r.y + e.y + e.height),  new Scalar(0, 0, 255, 255), 1);
                		  Bitmap bmpFirstEye = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpFirstEyeBinary = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  
                		  
                		  try {
                			  Mat matFirstEyeGray = mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height)));
                			  Utils.matToBitmap(matFirstEyeGray, bmpFirstEye);
                			  //assign bmpFirstEye to a member variable of the activity
                			  FdActivity.LeftEye = bmpFirstEye;
                			  
                			  //transform gray-scale to binary and assign to activity's member variable 
                			  Mat matFirstEyeColor = mRgba.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height)));;
                			  Mat matFirstEye = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matFirstEyeEqualized = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matFirstEyeEqualizedInverted = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matFirstEyeEqualizedEroded = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matFirstEyeMinIntensity = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matFirstEyeWithPupil = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  
                			  Imgproc.equalizeHist(mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height))), matFirstEyeEqualized);
                			  //Imgproc.threshold(matFirstEyeEqualized, matFirstEye, 100, 255, Imgproc.THRESH_BINARY);
                			  Core. bitwise_not(matFirstEyeEqualized, matFirstEyeEqualizedInverted);
                			  Imgproc.erode(matFirstEyeEqualizedInverted, matFirstEyeEqualizedEroded, new Mat() , new Point(-1, -1) , 2);
                			  
                			//Turn into binary                                        // threshold value between white and black
                			  Imgproc.threshold(matFirstEyeEqualizedEroded, matFirstEye, 200, 255, Imgproc.THRESH_BINARY);
                			  
                			  
                			//Find contours on binary
                			  java.util.List<MatOfPoint> leftContours = new ArrayList<MatOfPoint>();

                			  Imgproc.findContours(matFirstEye, leftContours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
                			  
                			  int leftMaxIntensity = -500; //assign an invalid initial value
                			  int leftContourWithMaxIntensity = -1;
                			  //Prepare array to hold mat
                			  byte buffFirstEye[] = new byte[(int) (matFirstEye.total() * matFirstEye.channels())];
            				  //Put matSecondEyeEqualizedEroded into an array
            				  byte buffFirstEyeEqualizedEroded[] = new byte[(int) (matFirstEyeEqualizedEroded.total() * matFirstEyeEqualizedEroded.channels())];
            				  matFirstEyeEqualizedEroded.get(0, 0, buffFirstEyeEqualizedEroded);
            				  
            				  if (leftContours.size() > 0) {
                				  //draw contour outline
                				  Imgproc.drawContours(matFirstEye, leftContours, -1, Scalar.all(100.0));
                				  for (int indexCon = 0; indexCon < leftContours.size(); indexCon++) {
                					  //fill indexCon-th contour with white color
                					  Imgproc.drawContours(matFirstEye, leftContours, indexCon, Scalar.all(255.0), -1);
                					  matFirstEye.get(0, 0, buffFirstEye);
                					  Rect boundingRect = Imgproc.boundingRect(leftContours.get(indexCon));
                					  
                					  Log.i("debug", "contour " + indexCon + " is called");
                					  Log.i("debug", "contour " + indexCon + "(" + boundingRect.y + ", " + boundingRect.x + ") width= " + 
                							  	boundingRect.width + ",height= " + boundingRect.height);
                							  
                					  
                					  double totalIntensity = 0.0;
                    				  int numberOfPointsInContour = 0;
                    				  int meanIntensity = 0;
                    				  
                    				  
                    				  for (int y = 0, pos = 0; y < boundingRect.height; y++) {
                    					  for (int x = 0; x < boundingRect.width; x++) {
                    						  pos = (int) ((boundingRect.y + y) * matFirstEye.size().width + boundingRect.x + x);
                    						  
                    						  //Log.i("rect", "Rect" + indexCon + "(y,x) = (" + y + " , " + x + " = " + pos + "gray value = " + buffSecondEyeEqualizedEroded[pos] + " binary value = " + buffSecondEye[pos]);
                    						  //-1 means it is white, 0 means it is black
                    						  if (buffFirstEye[pos] == -1) {
                    							  Log.i("Pixel", "Contour- " + indexCon + "(y,x) = (" + y + " , " + x + ") = " + buffFirstEyeEqualizedEroded[pos]);
                    							  numberOfPointsInContour++;
                    							  totalIntensity += buffFirstEyeEqualizedEroded[pos];
                    						  }
                    						  Log.i("rec", "Rect" + indexCon + "(y,x) = (" + y + " , " + x + ") = " + buffFirstEye[pos]);
                    					  
                    					  }
                    				  }
                    				  
                    				  
                    				  meanIntensity =  (int) (totalIntensity / numberOfPointsInContour);
                    				  
                    				  Log.i("Intensity", "Contour " + indexCon + " mean intensity is " + meanIntensity);
                    				  //May need to consider when two contours have the same mean intensity
                    				  if (meanIntensity > leftMaxIntensity) {
                    					leftMaxIntensity = meanIntensity;
                    					leftContourWithMaxIntensity = indexCon;
                    				  }
                    				  
                    				  
                				  }
                			  }
            				  
            				  
                			//use MaxIntensity found to threshold the image again                                //-5 to increase the threshold value 
                			  Imgproc.threshold(matFirstEye, matFirstEyeMinIntensity, 255 + leftMaxIntensity + 5, 255, Imgproc.THRESH_BINARY);
                			 
                			  matFirstEyeMinIntensity.copyTo(matFirstEyeWithPupil);
                			  
                			//Put matFirstEyeEqualizedEroded into an array
            				  byte buffFirstEyeGray[] = new byte[(int) (matFirstEyeGray.total() * matFirstEyeGray.channels())];
            				  matFirstEyeGray.get(0, 0, buffFirstEyeGray);
                			  
                			  //find the contour containing the max intensity (darkest region) in the original picture (Grayscale)
                			  java.util.List<MatOfPoint> leftContoursMax = new ArrayList<MatOfPoint>();

                			  Imgproc.findContours(matFirstEyeWithPupil, leftContoursMax, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
                			  Imgproc.drawContours(matFirstEyeWithPupil, leftContoursMax, -1, Scalar.all(125.0), -1);
                			  //the index of the contour containing pupil
                			  int leftPupilIndex = -1;
                			  Rect leftPupilBoundingRec = null;
                			  int leftMinIntensity = 1000; //picture is gray-scale. smaller the value, the darker it is; 1000 is random larger number , it can't be 
                
                			  if (leftContoursMax.size() > 0) {
                				  Log.i("debug", "Right now, we have " + leftContoursMax.size() + " remaining");
                				  for (int index = 0; index < leftContoursMax.size(); index++) {
                					  Rect boundingRect = Imgproc.boundingRect(leftContoursMax.get(index));
                					  //draw rectangle around the contour
                					  Log.i("debug", "contour " + index + " is called");
                					  Log.i("debug", "contour " + index + "(" + boundingRect.y + ", " + boundingRect.x + ") width= " + 
                							  	boundingRect.width + ",height= " + boundingRect.height);
                					  //Core.rectangle(matSecondEyeWithPupil, new Point(boundingRect.x, boundingRect.y), 
                					  //		  new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height), Scalar.all(150.0));
                					  
                					  double totalIntensity = 0.0;
                    				  int meanIntensity = 0;
                					  for (int y = 0, pos = 0; y < boundingRect.height; y++) {
                    					  for (int x = 0; x < boundingRect.width; x++) {
                    						  pos = (int) ((boundingRect.y + y) * matFirstEyeGray.size().width + boundingRect.x + x);
                    					  
                    						  Log.i("rect", "Rect" + index + "(y,x) = (" + y + " , " + x + " = " + pos + "gray value = " + buffFirstEyeGray[pos]);
                    						  totalIntensity += buffFirstEyeGray[pos];
                    					  }
                					  }
                					  meanIntensity = (int) (totalIntensity / boundingRect.area());
                					  Log.i("rect", "Rect" + index + " mean intensity is " + meanIntensity);
                					  if (leftMinIntensity > meanIntensity) {
                						  leftMinIntensity = meanIntensity;
                						  leftPupilIndex = index;
                						  leftPupilBoundingRec = boundingRect;
                					  }
                				  }
                			  }
                			  
                			//find pupil using moment
                			  Moments leftMoments = Imgproc.moments(leftContoursMax.get(leftPupilIndex));
                			  
                			  double left_pupil_x = leftMoments.get_m10() / leftMoments.get_m00();
                			  double left_pupil_y = leftMoments.get_m01() / leftMoments.get_m00();
                			  
                			  Log.i("rect-moment", "m10 is " + leftMoments.get_m10() + " ,m01 is " + leftMoments.get_m01() + " , m00 is " + leftMoments.get_m00());
                			  Log.i("rect-moment", "x is " + left_pupil_x +  ", y is " + left_pupil_y);
                			  
          
                			  
                			  Core.circle(matFirstEyeColor, new Point(left_pupil_x, left_pupil_y), 3, Scalar.all(200.0));
                			  
                			  Utils.matToBitmap(matFirstEyeColor, bmpFirstEyeBinary);
                			  FdActivity.LeftEyeBinary = bmpFirstEyeBinary;
                			  
                			  
                			// start a new thread to update top-left box of main UI
                			  new Thread(new Runnable() {
                				  public void run() {
                					  ((FdActivity) getContext()).mLeftEyeView.postInvalidate();
                					  ((FdActivity) getContext()).mLeftEyeViewBinary.postInvalidate();
                				  }
                			  }).start();
                			  
                		  } catch(Exception ee) {
                			  Log.e("org.opencv.samples..processFrame", "Utils.matToBitmap() throws an exception: " + ee.getMessage());
                			  bmpFirstEye.recycle();
                			  bmpFirstEyeBinary.recycle();
                		  }

                		//locate second eye
                		  e = eyes.toArray()[1];
                		  Core.rectangle(mRgba, new Point(r.x + e.x, r.y + e.y), 
                  				new Point(r.x + e.x + e.width, r.y + e.y + e.height),  new Scalar(0, 0, 255, 255), 1);
                		  
                		  Bitmap bmpSecondEyeColor = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEye = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeBinary = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeEqualized = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeEqualizedInverted = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeEqualizedEroded = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeBinaryWithContours = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeMinIntensity = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeWithPupil = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                		  Bitmap bmpSecondEyeWithPupilMoment = Bitmap.createBitmap(e.width, e.height, Bitmap.Config.RGB_565/*.ARGB_8888*/); 
                		  
                		  try {
                			  Mat matSecondEyeGray = mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height)));
                			  Utils.matToBitmap(matSecondEyeGray, bmpSecondEye);
                			  //assign bmpSecondEye to a member variable of the activity
                			  FdActivity.RightEye = bmpSecondEye;
                			  
                			//transform gray-scale to binary and assign to activity's member variable 
                			  Mat matSecondEyeColor = mRgba.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height)));;
                			  Mat matSecondEye = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matSecondEyeEqualized = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matSecondEyeEqualizedInverted = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matSecondEyeEqualizedEroded = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matSecondEyeMinIntensity = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  Mat matSecondEyeWithPupil = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  
                			  Mat matSecondEyeWithPupilMoment = new Mat(e.height, e.width, CvType.CV_8UC1);
                			  
                			  //Histogram Equalization
                			  Imgproc.equalizeHist(mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + e.x + e.width, r.y + e.y + e.height))), matSecondEyeEqualized);
                			  Utils.matToBitmap(matSecondEyeEqualized, bmpSecondEyeEqualized);
                			  FdActivity.RightEyeEqualized = bmpSecondEyeEqualized;
                			  
                			  //Histogram Equalized and inverted
                			  Core. bitwise_not(matSecondEyeEqualized, matSecondEyeEqualizedInverted);
                			  Utils.matToBitmap(matSecondEyeEqualizedInverted, bmpSecondEyeEqualizedInverted);
                			  FdActivity.RightEyeEqualizedInverted = bmpSecondEyeEqualizedInverted;
                			  
                			  //Histogram Equalized,inverted and eroded                                                               //number of iterations
                			  Imgproc.erode(matSecondEyeEqualizedInverted, matSecondEyeEqualizedEroded, new Mat() , new Point(-1, -1) , 2);
                			  Utils.matToBitmap(matSecondEyeEqualizedEroded, bmpSecondEyeEqualizedEroded);
                			  FdActivity.RightEyeEqualizedEroded = bmpSecondEyeEqualizedEroded;
                			  
                			 
                			 
                			  //Turn into binary                                        // threshold value between white and black
                			  Imgproc.threshold(matSecondEyeEqualizedEroded, matSecondEye, 200, 255, Imgproc.THRESH_BINARY);
                			  Utils.matToBitmap(matSecondEye, bmpSecondEyeBinary);
                			  FdActivity.RightEyeBinary = bmpSecondEyeBinary;
                			  
            
                			  //Find contours on binary
                			  java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

                			  Imgproc.findContours(matSecondEye, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
                			  
                			  int maxIntensity = -500; //assign an invalid initial value
                			  int contourWithMaxIntensity = -1;
                			  //Prepare array to hold mat
                			  byte buffSecondEye[] = new byte[(int) (matSecondEye.total() * matSecondEye.channels())];
            				  //Put matSecondEyeEqualizedEroded into an array
            				  byte buffSecondEyeEqualizedEroded[] = new byte[(int) (matSecondEyeEqualizedEroded.total() * matSecondEyeEqualizedEroded.channels())];
            				  matSecondEyeEqualizedEroded.get(0, 0, buffSecondEyeEqualizedEroded);
                			  if (contours.size() > 0) {
                				  //draw contour outline
                				  Imgproc.drawContours(matSecondEye, contours, -1, Scalar.all(100.0));
                				  for (int indexCon = 0; indexCon < contours.size(); indexCon++) {
                					  //fill indexCon-th contour with white color
                					  Imgproc.drawContours(matSecondEye, contours, indexCon, Scalar.all(255.0), -1);
                					  matSecondEye.get(0, 0, buffSecondEye);
                					  Rect boundingRect = Imgproc.boundingRect(contours.get(indexCon));
                					  
                					  Log.i("debug", "contour " + indexCon + " is called");
                					  Log.i("debug", "contour " + indexCon + "(" + boundingRect.y + ", " + boundingRect.x + ") width= " + 
                							  	boundingRect.width + ",height= " + boundingRect.height);
                							  
                					  
                					  double totalIntensity = 0.0;
                    				  int numberOfPointsInContour = 0;
                    				  int meanIntensity = 0;
                    				  
                    				  
                    				  for (int y = 0, pos = 0; y < boundingRect.height; y++) {
                    					  for (int x = 0; x < boundingRect.width; x++) {
                    						  pos = (int) ((boundingRect.y + y) * matSecondEye.size().width + boundingRect.x + x);
                    						  
                    						  //Log.i("rect", "Rect" + indexCon + "(y,x) = (" + y + " , " + x + " = " + pos + "gray value = " + buffSecondEyeEqualizedEroded[pos] + " binary value = " + buffSecondEye[pos]);
                    						  //-1 means it is white, 0 means it is black
                    						  if (buffSecondEye[pos] == -1) {
                    							  Log.i("Pixel", "Contour- " + indexCon + "(y,x) = (" + y + " , " + x + ") = " + buffSecondEyeEqualizedEroded[pos]);
                    							  numberOfPointsInContour++;
                    							  totalIntensity += buffSecondEyeEqualizedEroded[pos];
                    						  }
                    						  Log.i("rec", "Rect" + indexCon + "(y,x) = (" + y + " , " + x + ") = " + buffSecondEye[pos]);
                    					  
                    					  }
                    				  }
                    				  
                    				  
                    				  meanIntensity =  (int) (totalIntensity / numberOfPointsInContour);
                    				  
                    				  Log.i("Intensity", "Contour " + indexCon + " mean intensity is " + meanIntensity);
                    				  //May need to consider when two contours have the same mean intensity
                    				  if (meanIntensity > maxIntensity) {
                    					maxIntensity = meanIntensity;
                    					contourWithMaxIntensity = indexCon;
                    				  }
                    				  
                    				  
                				  }
                			  }
                			  
                			  Log.i("MinIntensity", "Contour " + contourWithMaxIntensity + " has max intensity " + maxIntensity);
                			  
                /*
                			  if (contours.size() > 0) {
                				  //Imgproc.drawContours(matSecondEye, contours, -1, Scalar.all(100.0), -1); //fill
                				  Imgproc.drawContours(matSecondEye, contours, -1, Scalar.all(100.0));
                				  Imgproc.drawContours(matSecondEye, contours, 0, Scalar.all(255.0), -1);
                				  Rect boundingRect = Imgproc.boundingRect(contours.get(0));
                				  //Core.rectangle(matSecondEye, new Point(boundingRect.x, boundingRect.y), new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height), Scalar.all(80.0));
                				  //Put matSecondEye into an array
                				  byte buffSecondEye[] = new byte[(int) (matSecondEye.total() * matSecondEye.channels())];
                				  matSecondEye.get(0, 0, buffSecondEye);
                				  //Put matSecondEyeEqualizedEroded into an array
                				  byte buffSecondEyeEqualizedEroded[] = new byte[(int) (matSecondEyeEqualizedEroded.total() * matSecondEyeEqualizedEroded.channels())];
                				  matSecondEyeEqualizedEroded.get(0, 0, buffSecondEyeEqualizedEroded);
                				  
                				  // traverse all the points in the bounding rectangle
                				  double totalIntensity = 0;
                				  double numberOfPointsInContour = 0;
                				  int meanIntensity = 0;
                				  for (int y = 0, pos = 0; y < boundingRect.height; y++) {
                					  for (int x = 0; x < boundingRect.width; x++) {
                						  pos = (int) ((boundingRect.y + y) * matSecondEye.size().width + boundingRect.x + x);
                						  
                						  //-1 means it is white, 0 means it is black
                						  if (buffSecondEye[pos] == -1) {
                							  //Log.i("Intensity", "(y,x) = (" + y + " , " + x + ") = " + buffSecondEyeEqualizedEroded[pos]);
                							  numberOfPointsInContour++;
                							  totalIntensity += buffSecondEyeEqualizedEroded[pos];
                						  }
                						  //Log.i("Intensity", "(y,x) = (" + y + " , " + x + ") = " + buffSecondEye[pos]);
                					  }
                				  }
                				  meanIntensity =  (int) (totalIntensity / numberOfPointsInContour);
        						  Log.i("Intensity", "mean intensity is " + meanIntensity);
                			  }
                	*/		  
                			  //draw the whitest contour with different gray
                			  Imgproc.drawContours(matSecondEye, contours, contourWithMaxIntensity, Scalar.all(125.0), -1);
                			  
                		 /*	  
                			  //Using erosion to eliminate reflection
                			  Rect boundingRectOfReflection = Imgproc.boundingRect(contours.get(contourWithMaxIntensity));
                			  Core.rectangle(matSecondEye, new Point(boundingRectOfReflection.x, boundingRectOfReflection.y), 
        					  		  new Point(boundingRectOfReflection.x + boundingRectOfReflection.width, boundingRectOfReflection.y + boundingRectOfReflection.height), Scalar.all(160.0));
                		 */	  
                			  
                			  Utils.matToBitmap(matSecondEye ,bmpSecondEyeBinaryWithContours);
                			  FdActivity.RightEyeBinaryWithContours = bmpSecondEyeBinaryWithContours;
                			  
                			  
                			  //use MaxIntensity found to threshold the image again                                //-5 to increase the threshold value 
                			  Imgproc.threshold(matSecondEyeEqualizedEroded, matSecondEyeMinIntensity, 255 + maxIntensity + 5, 255, Imgproc.THRESH_BINARY);
                			  Utils.matToBitmap(matSecondEyeMinIntensity ,bmpSecondEyeMinIntensity);
                			  FdActivity.RightEyeBinaryMeanIntensity = bmpSecondEyeMinIntensity;
                			  
                			  
                			  matSecondEyeMinIntensity.copyTo(matSecondEyeWithPupil);
                			  
                			  //Put matSecondEyeEqualizedEroded into an array
            				  byte buffSecondEyeGray[] = new byte[(int) (matSecondEyeGray.total() * matSecondEyeGray.channels())];
            				  matSecondEyeGray.get(0, 0, buffSecondEyeGray);
                			  //matSecondEyeWithPupil = matSecondEyeMinIntensity.submat(0, matSecondEyeMinIntensity.height(), 0, matSecondEyeMinIntensity.width());
                			  //find the contour containing the max intensity (darkest region) in the original picture (Grayscale)
                			  java.util.List<MatOfPoint> contoursMax = new ArrayList<MatOfPoint>();

                			  Imgproc.findContours(matSecondEyeWithPupil, contoursMax, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
                			  Imgproc.drawContours(matSecondEyeWithPupil, contoursMax, -1, Scalar.all(125.0), -1);
                			  //the index of the contour containing pupil
                			  int pupilIndex = -1;
                			  Rect pupilBoundingRec = null;
                			  int minIntensity = 1000; //picture is gray-scale. smaller the value, the darker it is; 1000 is random larger number , it can't be 
                			  if (contoursMax.size() > 0) {
                				  Log.i("debug", "Right now, we have " + contoursMax.size() + " remaining");
                				  for (int index = 0; index < contoursMax.size(); index++) {
                					  Rect boundingRect = Imgproc.boundingRect(contoursMax.get(index));
                					  //draw rectangle around the contour
                					  Log.i("debug", "contour " + index + " is called");
                					  Log.i("debug", "contour " + index + "(" + boundingRect.y + ", " + boundingRect.x + ") width= " + 
                							  	boundingRect.width + ",height= " + boundingRect.height);
                					  //Core.rectangle(matSecondEyeWithPupil, new Point(boundingRect.x, boundingRect.y), 
                					  //		  new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height), Scalar.all(150.0));
                					  
                					  double totalIntensity = 0.0;
                    				  int meanIntensity = 0;
                					  for (int y = 0, pos = 0; y < boundingRect.height; y++) {
                    					  for (int x = 0; x < boundingRect.width; x++) {
                    						  pos = (int) ((boundingRect.y + y) * matSecondEyeGray.size().width + boundingRect.x + x);
                    					  
                    						  Log.i("rect", "Rect" + index + "(y,x) = (" + y + " , " + x + " = " + pos + "gray value = " + buffSecondEyeGray[pos]);
                    						  totalIntensity += buffSecondEyeGray[pos];
                    					  }
                					  }
                					  meanIntensity = (int) (totalIntensity / boundingRect.area());
                					  Log.i("rect", "Rect" + index + " mean intensity is " + meanIntensity);
                					  if (minIntensity > meanIntensity) {
                						  minIntensity = meanIntensity;
                						  pupilIndex = index;
                						  pupilBoundingRec = boundingRect;
                					  }
                				  }
                			  }
                			  Core.rectangle(matSecondEyeWithPupil, new Point(pupilBoundingRec.x, pupilBoundingRec.y), 
                					  		  new Point(pupilBoundingRec.x + pupilBoundingRec.width, pupilBoundingRec.y + pupilBoundingRec.height), Scalar.all(160.0));
                			  Log.i("rect", "contour " + pupilIndex + " contains pupil, has intensity " + minIntensity);
                			   
                			  //**Apply erosion to eliminate the reflection located in the bounding box of the contour
                			  Mat matSecondEyePupilCentered = matSecondEyeEqualizedInverted.submat(new Rect(new Point(pupilBoundingRec.x, pupilBoundingRec.y), 
        					  		  new Point(pupilBoundingRec.x + pupilBoundingRec.width, pupilBoundingRec.y + pupilBoundingRec.height)));
                			  Bitmap bmpSecondEyePupilCentered = Bitmap.createBitmap(pupilBoundingRec.width, pupilBoundingRec.height, Bitmap.Config.RGB_565/*.ARGB_8888*/);
                			  //invert black and white
                			  Core. bitwise_not(matSecondEyePupilCentered, matSecondEyePupilCentered);
                			  //apply erosion
                			  Imgproc.erode(matSecondEyePupilCentered, matSecondEyePupilCentered, new Mat() , new Point(-1, -1) , 2);
                			  //invert black and white again
                			  Core. bitwise_not(matSecondEyePupilCentered, matSecondEyePupilCentered);
                			  
                			  Utils.matToBitmap(matSecondEyePupilCentered ,bmpSecondEyePupilCentered);
                			  FdActivity.RightEyePupilCentered = bmpSecondEyePupilCentered;
                			  
                			  //find pupil using moment
                			  //Moments moments = Imgproc.moments(contoursMax.get(pupilIndex));
                			  //Moments moments = Imgproc.moments();
                			  
                			  //double pupil_x = moments.get_m10() / moments.get_m00();
                			  //double pupil_y = moments.get_m01() / moments.get_m00();
                			  double pupil_x  = pupilBoundingRec.x + pupilBoundingRec.width / 2;
                			  double pupil_y = pupilBoundingRec.y + pupilBoundingRec.height / 2;
                			  
                			  //Log.i("rect-moment", "m10 is " + moments.get_m10() + " ,m01 is " + moments.get_m01() + " , m00 is " + moments.get_m00());
                			  //Log.i("rect-moment", "x is " + pupil_x +  ", y is " + pupil_y);
                			  
                			  Core.circle(matSecondEyeWithPupil, new Point(pupil_x, pupil_y), 3, Scalar.all(200.0));
                			  
                			  Utils.matToBitmap(matSecondEyeWithPupil ,bmpSecondEyeWithPupil);
                			  FdActivity.RightEyeBinaryWithPupil = bmpSecondEyeWithPupil;
                			  
                			  //back up cold copy before drawing on it
                			  matSecondEyeColor.copyTo(matSecondEyeWithPupilMoment);
                			  
                			  //draw pupil on original color mat
                			  Core.circle(matSecondEyeColor, new Point(pupil_x, pupil_y), 3, Scalar.all(200.0));
                			  Utils.matToBitmap(matSecondEyeColor, bmpSecondEyeColor);
                			  FdActivity.RightEyeColorWithPupilDrawn = bmpSecondEyeColor;
                			  
                			  /*
                			  
                			  Mat matSecondGrayInverted = new Mat((int) (1.5 * pupilBoundingRec.width), (int) (1.5 * pupilBoundingRec.width), CvType.CV_8UC1);
                			  Core. bitwise_not(matSecondEyeGray.submat(new Rect(new Point(pupil_x -  1.5 * pupilBoundingRec.width, pupil_y - 1.5 * pupilBoundingRec.width), new Point(pupil_x + 1.5 * pupilBoundingRec.width, pupil_y + 1.5 * pupilBoundingRec.width))),matSecondGrayInverted );
                			  Moments momentOriginal = Imgproc.moments(matSecondGrayInverted);
                			  //Moments momentOriginal = Imgproc.moments(matSecondEyeGray);
                			  double pupil_x_original =  momentOriginal.get_m10() / momentOriginal.get_m00();
                			  double pupil_y_original =  momentOriginal.get_m01() / momentOriginal.get_m00();
                			  
                			  Log.i("rect-moment", "x (original) is " + pupil_x_original + ", y (original) is " + pupil_y_original);
                			  
                			  pupil_x_original  += pupil_x -  1.5 * pupilBoundingRec.width;
                			  pupil_y_original  += pupil_y - 1.5 * pupilBoundingRec.width;
                			  
                			  Log.i("rect-moment", "x (original abs) is " + pupil_x_original + ", y (original abs) is " + pupil_y_original);
                			  
                			  Core.rectangle(matSecondEyeWithPupilMoment, new Point(pupil_x -  1.5 * pupilBoundingRec.width, pupil_y - 1.5 * pupilBoundingRec.width), new Point(pupil_x + 1.5 * pupilBoundingRec.width, pupil_y + 1.5 * pupilBoundingRec.width), Scalar.all(200.0));
                			
                			  Core.circle(matSecondEyeWithPupilMoment, new Point(pupil_y_original, pupil_x_original), 3, Scalar.all(200.0));
                			  Utils.matToBitmap(matSecondEyeWithPupilMoment, bmpSecondEyeWithPupilMoment);
                			  FdActivity.RightEyeColorWithPupilDrawnMoment = bmpSecondEyeWithPupilMoment;
                			  
                			  */
                			  
                			  //Put gray mat into an array for future processing
                			  //working on Equalized, inverted and eroded mat
                			  /*
                			  byte buffIntensities[] = new byte[(int) (matSecondEyeEqualizedEroded.total() * matSecondEyeEqualizedEroded.channels())];
                			  matSecondEyeEqualizedEroded.get(0, 0, buffIntensities);
                			  
                			  for (int m = 0; m < buffIntensities.length; m++) {
                				  Log.i("buffIntensities", "" + m + " : " + buffIntensities[m]);
                			  }
                			  */
                			  /*
                			  //draw contour with different intensity of color based on the mean intensity of the contour
                			  for (int i = 0; i < contours.size(); i++) {
                				  Point[] points = contours.get(i).toArray();
                				  int numOfPoints = points.length;
                				  int pos = 0;
                				  for (int j = 0; j < numOfPoints; j++) {
                					  pos = (int) (points[j].y * matSecondEyeEqualizedEroded.size().width + points[j].x);
                					  Log.i("contours", "contour-" + i + " (" + points[j].x + ", " + points[j].y + ")" + buffIntensities[pos]);
                				  }
                			  }
                			  */
                			  
                			  
                			// start a new thread to update middle-left box of main UI
                			  new Thread(new Runnable() {
                				  public void run() {
                					  
                					  
                					  ((FdActivity) getContext()).mRightEyeView.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewEqualized.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewEqualizedInverted.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewEqualizedEroded.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewBinary.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewBinaryWithContours.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewBinaryMeanIntensity.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewBinaryWithPupil.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyeViewColorWithPupilDrawn.postInvalidate();
                					  ((FdActivity) getContext()).mRightEyePupilCentered.postInvalidate();
                					  //((FdActivity) getContext()).mRightEyeViewColorWithPupilDrawnMoment.postInvalidate();
                				  }
                			  }).start();
                			  
                		  } catch(Exception ee) {
                			  Log.e("org.opencv.samples..processFrame", "Utils.matToBitmap() throws an exception: " + ee.getMessage());
                			  bmpSecondEye.recycle();
                			  bmpSecondEyeBinary.recycle();
                			  bmpSecondEyeEqualized.recycle();
                    		  bmpSecondEyeEqualizedInverted.recycle();
                    		  bmpSecondEyeEqualizedEroded.recycle();
                    		  bmpSecondEyeBinaryWithContours.recycle();
                		  }
                		  
                		  //get both eyes bitmap
                		  Rect ee = eyes.toArray()[0];
                		  Bitmap bmpBothEyes = null;
                		  Bitmap bmpBothEyesBinary = null;
                		  Bitmap bmpBothEyesEqualized = null;
                		  try {
	                		  //find out which eye is left eye and which one is right eye
	                		  if (e.x < ee.x) { //e is the left eye
	                			  bmpBothEyes = Bitmap.createBitmap(ee.x - e.x + ee.width, e.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  Utils.matToBitmap(mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + ee.x + ee.width, r.y + e.y + e.height))), bmpBothEyes);
	                			  FdActivity.BothEyes = bmpBothEyes;
	                			  
	                			  Mat matBothEyes = new Mat(ee.x - e.x + ee.width, e.height, CvType.CV_8UC1);
	                			  Mat matBothEyesEqualized = new Mat(ee.x - e.x + ee.width, e.height, CvType.CV_8UC1);
	                			  bmpBothEyesBinary =  Bitmap.createBitmap(ee.x - e.x + ee.width, e.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  
	                			  //Histogram equalization
	                			  Imgproc.equalizeHist(mGray.submat(new Rect(new Point(r.x + e.x, r.y + e.y), new Point(r.x + ee.x + ee.width, r.y + e.y + e.height))), matBothEyesEqualized);
	                			  bmpBothEyesEqualized = Bitmap.createBitmap(ee.x - e.x + ee.width, e.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  Utils.matToBitmap(matBothEyesEqualized, bmpBothEyesEqualized);
	                			  FdActivity.BothEyesEqualized = bmpBothEyesEqualized;
	                			  
	                			  //binary
	                			  Imgproc.threshold(matBothEyesEqualized ,matBothEyes, 100, 255, Imgproc.THRESH_BINARY);
	                			  Utils.matToBitmap(matBothEyes, bmpBothEyesBinary);
	                			  FdActivity.BothEyesBinary = bmpBothEyesBinary;
	                			  
	                		  } else { // ee is the left eye
	                			  bmpBothEyes = Bitmap.createBitmap(e.x - ee.x + e.width, ee.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  Utils.matToBitmap(mGray.submat(new Rect(new Point(r.x + ee.x, r.y + ee.y), new Point(r.x + e.x + e.width, r.y + ee.y + ee.height))), bmpBothEyes);
	                			  FdActivity.BothEyes = bmpBothEyes;
	                			  
	                			  Mat matBothEyes = new Mat(e.x - ee.x + e.width, ee.height, CvType.CV_8UC1);
	                			  Mat matBothEyesEqualized = new Mat(e.x - ee.x + e.width, ee.height, CvType.CV_8UC1);
	                			  bmpBothEyesBinary = Bitmap.createBitmap(e.x - ee.x + e.width, ee.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  
	                			  //histogram equalization
	                			  Imgproc.equalizeHist(mGray.submat(new Rect(new Point(r.x + ee.x, r.y + ee.y), new Point(r.x + e.x + e.width, r.y + ee.y + ee.height))), matBothEyesEqualized);
	                			  bmpBothEyesEqualized = Bitmap.createBitmap(e.x - ee.x + e.width, ee.height ,Bitmap.Config.RGB_565/*.ARGB_8888*/);
	                			  Utils.matToBitmap(matBothEyesEqualized, bmpBothEyesEqualized);
	                			  FdActivity.BothEyesEqualized = bmpBothEyesEqualized;
	                			  
	                			  //binary
	                			  Imgproc.threshold(matBothEyesEqualized, matBothEyes, 100, 255, Imgproc.THRESH_BINARY);
	                			  Utils.matToBitmap(matBothEyes, bmpBothEyesBinary);
	                			  FdActivity.BothEyesBinary = bmpBothEyesBinary;
	                		  }
	                		  
	                		  new Thread(new Runnable() {
                				  public void run() {
                					  //redraw view
                					  ((FdActivity) getContext()).mBothEyesView.postInvalidate();
                					  ((FdActivity) getContext()).mBothEyesViewBinary.postInvalidate();
                					  ((FdActivity) getContext()).mBothEyesViewEqualized.postInvalidate();
                				  }
                			  }).start();
                		  } catch (Exception eee) {
                			  Log.e("org.opencv.samples..processFrame..bothEyes", "Utils.matToBitmap() throws an exception: " + eee.getMessage());
                			  bmpBothEyes.recycle();
                			  bmpBothEyesBinary.recycle();
                			  bmpBothEyesEqualized.recycle();
                		  }
            			 
                		  

                	}


                	
                }
            }
        }

        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565/*.ARGB_8888*/);
        
        try {
        	Utils.matToBitmap(mRgba, bmp);
        	CameraFrame = bmp;
        } catch(Exception e) {
        	Log.e("org.opencv.samples.puzzle15", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            CameraFrame = null;
        }
    }

    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();

            mRgba = null;
            mGray = null;
        }
    }
}

