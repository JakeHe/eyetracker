package org.opencv.samples.fd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.graphics.Canvas;

public class FdActivity extends Activity {
    private static final String TAG         = "Sample::Activity";

    
    private MenuItem            mItemFace50;
    private MenuItem            mItemFace40;
    private MenuItem            mItemFace30;
    private MenuItem            mItemFace20;
    
    //debugging
    public static boolean DebugOn =	true;		
    
    public static Bitmap		LeftEye;
    public static Bitmap		LeftEyeBinary;
    public static Bitmap        RightEye;
    public static Bitmap		RightEyeColorWithPupilDrawn;
    public static Bitmap 		RightEyeColorWithPupilDrawnMoment;
    public static Bitmap 		RightEyeEqualized;
    public static Bitmap		RightEyeEqualizedInverted;
    public static Bitmap		RightEyeEqualizedEroded;
    public static Bitmap		RightEyeBinary;
    public static Bitmap		RightEyeBinaryWithPupil;
    public static Bitmap		RightEyeBinaryMeanIntensity;
    public static Bitmap		RightEyeBinaryWithContours;
    public static Bitmap 		RightEyePupilCentered;
    public static Bitmap 		BothEyes;
    public static Bitmap		BothEyesBinary;
    public static Bitmap		BothEyesEqualized;
           
    public View					mLeftEyeView;
    public View					mLeftEyeViewBinary;
    public View					mRightEyeView;
    public View					mRightEyeViewColorWithPupilDrawn;
    public View					mRightEyeViewColorWithPupilDrawnMoment;
    public View					mRightEyeViewBinary;
    public View					mRightEyeViewBinaryWithPupil;
    public View					mRightEyeViewBinaryMeanIntensity;
    public View					mRightEyeViewBinaryWithContours;
    public View					mRightEyeViewEqualized;
    public View					mRightEyeViewEqualizedInverted;
    public View					mRightEyeViewEqualizedEroded;
    public View 				mRightEyePupilCentered;
    public View					mBothEyesView;
    public View					mBothEyesViewBinary;
    public View					mBothEyesViewEqualized;

    public static float         minFaceSize = 0.5f;

    public FdActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
        LeftEye = null;
    }
    
    //leftEyeView needs to be static, in order for us to reference it in layout XML file
    private static class leftEyeView extends View {
    	  public leftEyeView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw", "Yes, onDraw is called");
    	  if (LeftEye != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(LeftEye, 0, 0, null);
    		  Log.i("ondraw-bitmap", "Yes, bitmap is not null");
    	  }
    	  
    	  }
    }
    
    private static class leftEyeBinaryView extends View {
  	  public leftEyeBinaryView(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-leftEyeViewBinary", "Yes, onDraw is called");
  	  if (LeftEyeBinary != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(LeftEyeBinary, 0, 0, null);
  		  Log.i("ondraw-bitmap-leftEyeViewBinary", "Yes, bitmap is not null");
  	  }
  	  
  	  }
  }
    
    //rightEyeView needs to be static, in order for us to reference it in layout XML file
    //create a second view to hold second eye
    private static class rightEyeView extends View {
    	  public rightEyeView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-righteye", "Yes, onDraw (right eye) is called");
    	  if (RightEye != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEye, 0, 0, null);
    		  Log.i("ondraw-bitmap-righteye", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
    
    private static class rightEyeEqualizedView extends View {
  	  public rightEyeEqualizedView(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-rightEyeEqualizedView", "Yes, onDraw (right eye) is called");
  	  if (RightEyeEqualized != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(RightEyeEqualized, 0, 0, null);
  		  Log.i("ondraw-bitmap-rightEyeEqualizedView", "Yes, bitmap (right eye) is not null");
  	  }
  	  
  	  }
  }
    
    private static class rightEyeEqualizedInvertedView extends View {
    	  public rightEyeEqualizedInvertedView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-rightEyeEqualizedInvertedView", "Yes, onDraw (right eye) is called");
    	  if (RightEyeEqualizedInverted != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEyeEqualizedInverted, 0, 0, null);
    		  Log.i("ondraw-bitmap-rightEyeEqualizedInvertedView", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
    
    
    
    
    
    private static class rightEyeEqualizedErodedView extends View {
    	  public rightEyeEqualizedErodedView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-rightEyeEqualizedErodedView", "Yes, onDraw (right eye) is called");
    	  if (RightEyeEqualizedEroded != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEyeEqualizedEroded, 0, 0, null);
    		  Log.i("ondraw-bitmap-rightEyeEqualizedErodedView", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
      
   
    private static class rightEyeBinaryView extends View {
  	  public rightEyeBinaryView(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-rightEyeViewBinary", "Yes, onDraw (right eye) is called");
  	  if (RightEyeBinary != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(RightEyeBinary, 0, 0, null);
  		  Log.i("ondraw-bitmap-rightEyeViewBinary", "Yes, bitmap (right eye) is not null");
  	  }
  	  
  	  }
  }
    
    private static class rightEyeBinaryViewWithPupil extends View {
    	  public rightEyeBinaryViewWithPupil(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-rightEyeViewBinaryWithPupil", "Yes, onDraw (right eye) is called");
    	  if (RightEyeBinaryWithPupil != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEyeBinaryWithPupil, 0, 0, null);
    		  Log.i("ondraw-bitmap-rightEyeViewBinaryPupil", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
    
    private static class rightEyeColorViewWithPupilDrawnMoment extends View {
  	  public rightEyeColorViewWithPupilDrawnMoment(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-rightEyeColorViewWithPupilDrawnMoment", "Yes, onDraw (right eye) is called");
  	  if (RightEyeColorWithPupilDrawnMoment != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(RightEyeColorWithPupilDrawnMoment, 0, 0, null);
  		  Log.i("ondraw-bitmap-rightEyeColorViewWithPupilDrawnMoment", "Yes, bitmap (right eye) is not null");
  	  }
  	  
  	  }
  }
    
    private static class rightEyeColorViewWithPupilDrawn extends View {
  	  public rightEyeColorViewWithPupilDrawn(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-rightEyeColorViewWithPupilDrawn", "Yes, onDraw (right eye) is called");
  	  if (RightEyeColorWithPupilDrawn != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(RightEyeColorWithPupilDrawn, 0, 0, null);
  		  Log.i("ondraw-bitmap-rightEyeColorViewWithPupilDrawn", "Yes, bitmap (right eye) is not null");
  	  }
  	  
  	  }
  }
    
    
    
    private static class rightEyeBinaryViewMeanIntensity extends View {
    	  public rightEyeBinaryViewMeanIntensity(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-rightEyeBinaryViewMeanIntensity", "Yes, onDraw (right eye) is called");
    	  if (RightEyeBinaryMeanIntensity != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEyeBinaryMeanIntensity, 0, 0, null);
    		  Log.i("ondraw-bitmap-rightEyeBinaryViewMeanIntensity", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
    
    private static class rightEyeBinaryViewWithContours extends View {
    	  public rightEyeBinaryViewWithContours(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-rightEyeBinaryViewWithContours", "Yes, onDraw (right eye) is called");
    	  if (RightEyeBinaryWithContours != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(RightEyeBinaryWithContours, 0, 0, null);
    		  Log.i("ondraw-bitmap-rightEyeBinaryViewWithContours", "Yes, bitmap (right eye) is not null");
    	  }
    	  
    	  }
    }
    
    private static class rightEyeViewWithPupilCentered extends View {
  	  public rightEyeViewWithPupilCentered(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-rightEyeViewWithPupilCentered", "Yes, onDraw (right eye) is called");
  	  if (RightEyePupilCentered != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(RightEyePupilCentered, 0, 0, null);
  		  Log.i("ondraw-bitmap-rightEyeViewWithPupilCentered", "Yes, bitmap (right eye) is not null");
  	  }
  	  
  	  }
  }
    
    private static class bothEyesBinaryView extends View {
    	  public bothEyesBinaryView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-bothEyesBinaryView", "Yes, onDraw (both eyes) is called");
    	  if (BothEyesBinary != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(BothEyesBinary, 0, 0, null);
    		  Log.i("ondraw-bitmap-bothEyesBinaryView", "Yes, bitmap (both eyes) is not null");
    	  }
    	  
    	  }
    }
    
    
    private static class bothEyesView extends View {
  	  public bothEyesView(Context context,  AttributeSet attributeSet) {
  	        super(context, attributeSet);
  	  }
  	  
  	  public void onDraw(Canvas canvas) {
  	        super.onDraw(canvas);
  	  
  	        Log.i("ondraw-bothEyesView", "Yes, onDraw (both eyes) is called");
  	  if (BothEyes != null) {
  		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
  		  canvas.drawBitmap(BothEyes, 0, 0, null);
  		  Log.i("ondraw-bitmap-bothEyesView", "Yes, bitmap (both eyes) is not null");
  	  }
  	  
  	  }
  }
    
    private static class bothEyesEqualizedView extends View {
    	  public bothEyesEqualizedView(Context context,  AttributeSet attributeSet) {
    	        super(context, attributeSet);
    	  }
    	  
    	  public void onDraw(Canvas canvas) {
    	        super.onDraw(canvas);
    	  
    	        Log.i("ondraw-bothEyesEqualizedView", "Yes, onDraw (both eyes) is called");
    	  if (BothEyesEqualized != null) {
    		  //canvas.drawBitmap(LeftEye, (canvas.getWidth() - LeftEye.getWidth()) / 2, (canvas.getHeight() - LeftEye.getHeight()) / 2, null);
    		  canvas.drawBitmap(BothEyesEqualized, 0, 0, null);
    		  Log.i("ondraw-bitmap-bothEyesEqualizedView", "Yes, bitmap (both eyes) is not null");
    	  }
    	  
    	  }
    }
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        
        //setContentView(new FdView(this));
        setContentView(R.layout.mainui);
        
        mLeftEyeView =  (View) findViewById(R.id.viewLeftEye);
        mRightEyeView =  (View) findViewById(R.id.viewRightEye);
        mRightEyeViewColorWithPupilDrawn = (View) findViewById(R.id.viewRightEyeColorWithPupilDrawn);
        //mRightEyeViewColorWithPupilDrawnMoment = (View) findViewById(R.id.viewRightEyeColorWithPupilDrawnMoment);
        mLeftEyeViewBinary =  (View) findViewById(R.id.viewLeftEyeBinary);
        mRightEyeViewEqualized =  (View) findViewById(R.id.viewRightEyeEqualized);
        mRightEyeViewEqualizedInverted = (View) findViewById(R.id.viewRightEyeEqualizedInverted);
        mRightEyeViewEqualizedEroded =  (View) findViewById(R.id.viewRightEyeEqualizedEroded);
        mRightEyeViewBinary =  (View) findViewById(R.id.viewRightEyeBinary);
        mRightEyeViewBinaryWithPupil = (View) findViewById(R.id.viewRightEyeBinaryWithPupil);
        mRightEyeViewBinaryMeanIntensity =  (View) findViewById(R.id.viewRightEyeBinaryMeanIntensity);
        mRightEyeViewBinaryWithContours =  (View) findViewById(R.id.viewRightEyeBinaryWithContours);
        mRightEyePupilCentered = (View) findViewById(R.id.viewRightEyePupilCentered);
        mBothEyesView = (View) findViewById(R.id.viewBothEyes);
        mBothEyesViewBinary = (View) findViewById(R.id.viewBothEyesBinary);
        mBothEyesViewEqualized = (View) findViewById(R.id.viewBothEyesEqualized);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemFace50)
            minFaceSize = 0.5f;
        else if (item == mItemFace40)
            minFaceSize = 0.4f;
        else if (item == mItemFace30)
            minFaceSize = 0.3f;
        else if (item == mItemFace20)
            minFaceSize = 0.2f;
        return true;
    }
}
