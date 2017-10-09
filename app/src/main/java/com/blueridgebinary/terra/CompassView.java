package com.blueridgebinary.terra;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.animation.ValueAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.utils.ListenableBoolean;
import com.blueridgebinary.terra.utils.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom View for displaying compass data.  Comes with three modes.
 */
public class CompassView extends View implements
        ValueAnimator.AnimatorUpdateListener {

    static final String TAG = CompassView.class.getSimpleName();

    private int mContentWidth;
    private int mContentHeight;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int needleModeId;
    private int needleColor;
    private float needleWidth;
    private float needleLengthFraction;
    private int baseImageResId;
    private int needleImageResId;
    private int orientationImageResId;
    private int disabledImageResId;

    private Bitmap mBaseImageBitmap;
    private Bitmap mNeedleImageBitmap;

    private Path mNeedlePath;
    private Paint mNeedlePaint;
    private Paint mImagePaint;
    private Rect drawingRect;

    private Matrix rotationMatrix;
    private float px;
    private float py;

    private int defaultImageResourceId = 17301555;
    // Initialize with default values for dip and azimuth
    private float dip = 90f;
    private float azimuth = 0f;

    // Animation dev stuff
    private float trueDip = 90f;
    private float trueAzimuth = 0f;

    private float prevDip = 90f;
    private float prevAzimuth = 0f;

    private AnimatorSet mAnimatorSet;
    private LinearInterpolator mInterpolator = new LinearInterpolator();

    public ListenableBoolean isEnabled;

    private boolean isAnimating = false;

    private GestureDetector mGestureDetector;

    public CompassView(Context context) {
        super(context);
        init(null, 0);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CompassView, defStyle, 0);

        needleModeId = a.getInt(
                R.styleable.CompassView_needleMode,
                1);

        // get stroke color
        needleColor = a.getResourceId(R.styleable.CompassView_needleColor, android.R.color.holo_red_light);
        // get stroke width
        needleWidth = a.getFloat(R.styleable.CompassView_needleWidth, 1.0f);
        // Orientation arrow max length relative to base image (incase you have a border or something)
        needleLengthFraction = a.getFloat(R.styleable.CompassView_needleLengthProportion, 1.0f);

        baseImageResId = a.getResourceId(R.styleable.CompassView_baseImage, defaultImageResourceId);
        needleImageResId = a.getResourceId(R.styleable.CompassView_needleImage, defaultImageResourceId);
        disabledImageResId = a.getResourceId(R.styleable.CompassView_disabledImage, defaultImageResourceId);

        // Orientation mode image defaults to the same one as the compass base
        orientationImageResId = a.getResourceId(R.styleable.CompassView_orientationModeImage, baseImageResId);
        // Get whether the view is defaulted to enabled or not
        isEnabled = new ListenableBoolean(a.getBoolean(R.styleable.CompassView_isEnabled, true));

        a.recycle();

        // Initialize Paints, etc.
        rotationMatrix = new Matrix();
        mNeedlePath = new Path();
        mImagePaint = new Paint();
        mNeedlePaint = new Paint();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNeedlePaint.setColor(getResources().getColor(needleColor, getContext().getTheme()));
        } else {
            mNeedlePaint.setColor(getResources().getColor(needleColor));
        }
        mNeedlePaint.setStyle(Paint.Style.STROKE);
        mNeedlePaint.setStrokeWidth(needleWidth);
        mNeedlePaint.setStrokeJoin(Paint.Join.ROUND);

        // Get GestureDetector for handling enable/disable
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                setEnabled(!isEnabled.getValue());
                super.onLongPress(e);
            }
        });
        mGestureDetector.setIsLongpressEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Log.d(TAG,"onSizeChanged() called!");
        //Log.d(TAG,String.format("%d %d %d %d",w,h,oldw,oldh));
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();
        mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;
        px = mContentWidth / 2;
        py = mContentHeight / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Get a centered, square portion of the screen you will draw into
        drawingRect = makeCenteredSquareDrawingRect();
        // On first draw load bitmaps
        if (mBaseImageBitmap == null) loadBaseImage();
        // Check if the view is disabled, if so, load the disabled image and return
        if (!isEnabled.getValue()) {
            canvas.drawBitmap(mBaseImageBitmap, null, drawingRect, mImagePaint);
            return;
        }
        // Draw needle layer depending on view mode
        // 1 = azimuth,  2 = vector, 3 = plane
        switch (needleModeId) {
            case 1:
                // Get the needle image if it isn't already loaded
                if (mNeedleImageBitmap == null) loadNeedleImage();
                // Populate rotation matrix
                rotationMatrix.reset();
                rotationMatrix.postTranslate(-mNeedleImageBitmap.getWidth() / 2, -mNeedleImageBitmap.getHeight() / 2);
                rotationMatrix.postRotate(azimuth);
                rotationMatrix.postTranslate(px, py);
                // Draw!
                canvas.drawBitmap(mBaseImageBitmap, null, drawingRect, mImagePaint);
                canvas.drawBitmap(mNeedleImageBitmap, rotationMatrix, mImagePaint);
                break;
            case 2:
                fillCompassArrowPath(drawingRect, dip, 0f);
                // Draw!
                canvas.drawBitmap(mBaseImageBitmap, null, drawingRect, mImagePaint);
                canvas.drawPath(mNeedlePath, mNeedlePaint);
                break;
            case 3:
                fillCompassArrowPath(drawingRect, dip, azimuth);
                // Draw!
                canvas.drawBitmap(mBaseImageBitmap, null, drawingRect, mImagePaint);
                canvas.drawPath(mNeedlePath, mNeedlePaint);
                break;
        }
    }


    public Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
    }


    public void fillCompassArrowPath(Rect drawingRect, float dip, float azimuth) {
        // TODO: Remove hardcoding
        double arrowProportion = 0.2;
        double arrowAngle = Math.toRadians(40);
        // Figure out how long the line should be
        float vectorLength = (mContentHeight / 2.0f) * (dip / 90.0f) * needleLengthFraction;
        // Clear the current path
        mNeedlePath.reset();
        // Move to center of compass
        mNeedlePath.moveTo(drawingRect.centerX(), drawingRect.centerY());
        // Get coords of path points, pre-rotation
        double point[] = {0, 0 - vectorLength};
        double arrowDy = Math.cos(arrowAngle) * (arrowProportion * vectorLength);
        double arrowDx = Math.sin(arrowAngle) * (arrowProportion * vectorLength);
        double arrowLeftTip[] = {point[0] - arrowDx, point[1] + arrowDy};
        double arrowRightTip[] = {point[0] + arrowDx, point[1] + arrowDy};
        // Rotate points if necessary (about origin)
        if (azimuth != 0) {
            point = rotateXY(point, Math.toRadians(azimuth));
            arrowLeftTip = rotateXY(arrowLeftTip, Math.toRadians(azimuth));
            arrowRightTip = rotateXY(arrowRightTip, Math.toRadians(azimuth));
        }


        // Draw, shifting points from origin to center of view
        mNeedlePath.lineTo((float) (point[0] + drawingRect.centerX()), (float) (point[1] + drawingRect.centerY()));
        mNeedlePath.lineTo((float) (arrowLeftTip[0] + drawingRect.centerX()), (float) (arrowLeftTip[1] + drawingRect.centerY()));
        mNeedlePath.moveTo((float) (point[0] + drawingRect.centerX()), (float) (point[1] + drawingRect.centerY()));
        mNeedlePath.lineTo((float) (arrowRightTip[0] + drawingRect.centerX()), (float) (arrowRightTip[1] + drawingRect.centerY()));
        // If in orientation mode, get coords of two ends of strike line
        if (needleModeId == 3) {
            double strikeLeftPoint[] = {0 - (mContentWidth / 2) * needleLengthFraction, 0};
            double strikeRightPoint[] = {0 + (mContentWidth / 2) * needleLengthFraction, 0};
            strikeLeftPoint = rotateXY(strikeLeftPoint, Math.toRadians(azimuth));
            strikeRightPoint = rotateXY(strikeRightPoint, Math.toRadians(azimuth));
            mNeedlePath.moveTo(drawingRect.centerX(), drawingRect.centerY());
            mNeedlePath.lineTo((float) strikeLeftPoint[0] + drawingRect.centerX(), (float) strikeLeftPoint[1] + drawingRect.centerY());
            mNeedlePath.lineTo((float) strikeRightPoint[0] + drawingRect.centerX(), (float) strikeRightPoint[1] + drawingRect.centerY());
        }
    }

    public double[] rotateXY(double[] xyArray, double thetaRadians) {
        double x = xyArray[0];
        double y = xyArray[1];
        xyArray[0] = (x * Math.cos(thetaRadians)) - (y * Math.sin(thetaRadians));
        xyArray[1] = (y * Math.cos(thetaRadians)) + (x * Math.sin(thetaRadians));
        return xyArray;
    }

    public void setOrientation(float newAzimuth, float newDip) {
        // Animate this update to the dip/azimuth
        startAnimating(newAzimuth, newDip);
    }

    public float makeAngleDegreesPositive(float angleDeg) {
        if (angleDeg < 0) {
            return angleDeg + 360;
        }
        else {
            return angleDeg;
        }
    }

    // Checks the new azimuth against the previous azimuth, making it a negative angle if needed
    public float calcNewAzimuthMinRotation(float previousAzimuth, float newAzimuth) {
        // Set new azimuth to the shortest rotation distance
        float difference = newAzimuth - previousAzimuth;
        if (difference > 180) {
            return difference - 360 + previousAzimuth;
        }
        else {
            return newAzimuth;
        }
    }

    // Checks the previous azimuth against the next azimuth, making it a negative angle if needed
    public float calcPrevAzimuthMinRotation(float previousAzimuth, float newAzimuth) {
        // Set new azimuth to the shortest rotation distance
        float difference = newAzimuth - previousAzimuth;
        if (difference < -180) {
            return newAzimuth - (difference + 360);
        } else {
            return previousAzimuth;
        }
    }

    /*
        From https://developer.android.com/topic/performance/graphics/load-bitmap.html
        Calculates the nearest power of 2 sampling factor for downsampling a bitmap that is
        higher resolution than your view
        */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /*
    From https://developer.android.com/topic/performance/graphics/load-bitmap.html
    Loads a bitmap, resampling to downscale if needed
    */

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    // -------------------------GETTERS AND SETTERS -----------------------------------------

    private void loadBaseImage() {
        // If compass is not enabled, load the disabled compass image and return
        if (!isEnabled.getValue()) {
            mBaseImageBitmap = decodeSampledBitmapFromResource(getResources(),
                    disabledImageResId,
                    mContentWidth,
                    mContentHeight);
            mBaseImageBitmap = scaleBitmapToFitPreserveAspectRatio(mBaseImageBitmap);
            return;
        }
        // Otherwise get the proper base image and load it
        switch (needleModeId) {
            case 1:
                mBaseImageBitmap = decodeSampledBitmapFromResource(getResources(),
                        baseImageResId,
                        mContentWidth,
                        mContentHeight);
                break;
            case 2:
                mBaseImageBitmap = decodeSampledBitmapFromResource(getResources(),
                        orientationImageResId,
                        mContentWidth,
                        mContentHeight);
                break;
            case 3:
                mBaseImageBitmap = decodeSampledBitmapFromResource(getResources(),
                        orientationImageResId,
                        mContentWidth,
                        mContentHeight);
                break;
        }
        mBaseImageBitmap = scaleBitmapToFitPreserveAspectRatio(mBaseImageBitmap);
    }
    private void loadNeedleImage() {
        // Load needle image into memory
        mNeedleImageBitmap = decodeSampledBitmapFromResource(getResources(),
                needleImageResId,
                mContentWidth,
                mContentHeight);
        mNeedleImageBitmap = scaleBitmapToFitPreserveAspectRatio(mNeedleImageBitmap);
    }


    public int getNeedleModeId() {
        return needleModeId;
    }

    public void setNeedleModeId(int needleModeId) {
        this.needleModeId = needleModeId;
        // Load new base image
        loadBaseImage();
        invalidate();
    }

    public int getNeedleColor() {
        return needleColor;
    }

    public void setNeedleColor(int needleColor) {
        this.needleColor = needleColor;
        invalidate();
    }

    public float getNeedleWidth() {
        return needleWidth;
    }

    public void setNeedleWidth(float needleWidth) {
        this.needleWidth = needleWidth;
        invalidate();
    }

    public float getNeedleLengthFraction() {
        return needleLengthFraction;
    }

    public void setNeedleLengthFraction(float needleLengthFraction) {
        this.needleLengthFraction = needleLengthFraction;
        invalidate();
    }

    public int getBaseImageResId() {
        return baseImageResId;
    }

    public void setBaseImageResId(int baseImageResId) {
        this.baseImageResId = baseImageResId;
        invalidate();
    }

    public int getNeedleImageResId() {
        return needleImageResId;
    }

    public void setNeedleImageResId(int needleImageResId) {
        this.needleImageResId = needleImageResId;
        invalidate();
    }

    public int getOrientationImageResId() {
        return orientationImageResId;
    }

    public void setOrientationImageResId(int orientationImageResId) {
        this.orientationImageResId = orientationImageResId;
        invalidate();
    }

    public int getDisabledImageResId() {
        return disabledImageResId;
    }

    public void setDisabledImageResId(int disabledImageResId) {
        this.disabledImageResId = disabledImageResId;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled.getValue();
    }

    @Override
    public void setEnabled(boolean enable) {
        isEnabled.setValue(enable);
        loadBaseImage();
        invalidate();
    }

    public Bitmap scaleBitmapToFitPreserveAspectRatio(Bitmap bitmap) {
        float scalingFactor;
        // Get scaled dimensions for bitmap

        //Log.d(TAG,String.format("BITMAP DIMS: %d %d",bitmap.getWidth(),bitmap.getHeight()));
        if ((drawingRect.width() < bitmap.getWidth()) && (bitmap.getWidth() >= bitmap.getHeight()) ) {
            scalingFactor =  (float) drawingRect.width() / (float) bitmap.getWidth();
           // Log.d(TAG,"Bitmap is wider than view.");
        }
        else if ((drawingRect.height() < bitmap.getHeight()) && (bitmap.getHeight() >= bitmap.getWidth())){
            scalingFactor = (float) drawingRect.height() / (float) bitmap.getHeight();
            //Log.d(TAG,"Bitmap is taller than view.");
        }
        else {
            return bitmap;
        }

        //Log.d(TAG,String.format("Scaling Factor: %f",scalingFactor));
        // creates matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scalingFactor, scalingFactor);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return resizedBitmap;
    }

    public void startAnimating(float newAzi, float newDip) {
        // Only start a new animation if the current one is not running
        if (mAnimatorSet != null) {
            if (mAnimatorSet.isRunning()) {
                return;
            }
        }


        // If we're able to draw things, go ahead and update to the new azimuth
        // ensuring azimuth is between 0-360 deg
        this.trueAzimuth = makeAngleDegreesPositive(newAzi);
        this.trueDip = newDip;

        // Calculate the angle that should be passed into rotation routines for the current angle
        // and the new angle to update to.  This allows the compass to properly rotate.
        float drawingAzimuth = calcNewAzimuthMinRotation(this.prevAzimuth,this.trueAzimuth);
        float drawingPrevAzimuth = calcPrevAzimuthMinRotation(this.prevAzimuth,this.trueAzimuth);

        mAnimatorSet = new AnimatorSet();

        ArrayList<Animator> animators = new ArrayList<>();

        ObjectAnimator aziAnimator;
        ObjectAnimator dipAnimator;
        if (Math.abs(trueAzimuth-prevAzimuth) >= 2.5 && (needleModeId == 1 || needleModeId == 3)) {
            // Animation 1 -> Azimuth
            aziAnimator = ObjectAnimator.ofFloat(this,"azimuth",drawingPrevAzimuth,drawingAzimuth);
            aziAnimator.setDuration(500);
            aziAnimator.addUpdateListener(this);
            aziAnimator.setInterpolator(mInterpolator);
            animators.add(0,aziAnimator);
            this.prevAzimuth = trueAzimuth;
        }
        if (Math.abs(trueDip-prevDip) >= 2.5 && (needleModeId == 2 || needleModeId == 3)) {
            // Animation 2 -> Dip
            dipAnimator = ObjectAnimator.ofFloat(this,"dip",prevDip,trueDip);
            dipAnimator.setDuration(500);
            dipAnimator.addUpdateListener(this);
            animators.add(0,dipAnimator);
            this.prevDip = trueDip;
        }
        // Only run animations if angles have changed more than a certain amount
        if (animators.size() > 0) {
            mAnimatorSet.playTogether(animators);
            mAnimatorSet.start();
        }
    }

    public Rect makeCenteredSquareDrawingRect() {

        int[] outLocation = new int[2];
        getLocationOnScreen(outLocation);
        int l = 0;
        int r = mContentWidth;
        int t = 0;
        int b = mContentHeight;

        // find smallest dimension and shrink the larger dimension to make the box roughly square (+- 1px)
        if (mContentWidth > mContentHeight) {
            //Log.d(TAG,"Creating Rect!   Detected that the content width is greater than height");
            int delta = (mContentWidth - mContentHeight)/2;
            l = l + delta;
            r = r - delta;
        }
        else if (mContentHeight > mContentWidth) {
            //Log.d(TAG,"Creating Rect!   Detected that the content height is greater than width");
            int delta = ( mContentHeight-mContentWidth)/2;
            t = t + delta;
            b = b - delta;
        }
        return new Rect(l,t,r,b);
    }

    private float getDip() {
        return dip;
    }

    private void setDip(float dip) {
        this.dip = dip;
    }

    private float getAzimuth() {
        return azimuth;
    }

    private void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // Re-draw view each time the animation executes
        invalidate();
    }

    public float getPrevAzimuth() {
        return prevAzimuth;
    }




}
