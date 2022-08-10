package com.example.widght;


import android.animation.ObjectAnimator;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;





public class PhotoImageView extends ImageView {

    private GestureDetector mGestureDetector;
    private Bitmap mBitmap;
    private Paint mPaint;
    private static final String TAG = "PhotoImageView";

    public PhotoImageView(Context context) {
        super(context);
        init(context);
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mGestureDetector = new GestureDetector(context, new PhotoGestureListener());
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dog_20220803125240);
        mPaint = new Paint();
        mOverScroller = new OverScroller(context);
        mScaleGestureDetector = new ScaleGestureDetector(context, new PhotoScaleGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        // 双指缩放操作优先处理事件
        boolean result = mScaleGestureDetector.onTouchEvent(event);
        // 如果不是双指缩放才处理手势事件
        if (!mScaleGestureDetector.isInProgress()) {
            result = mGestureDetector.onTouchEvent(event);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scaleFraction = (mCurrentScale - mSmallScale) / (mBigScale - mSmallScale);
        canvas.translate(mOffsetX * scaleFraction, mOffsetY * scaleFraction);
        canvas.scale(mCurrentScale, mCurrentScale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(mBitmap, mOriginalOffsetX, mOriginalOffsetY, mPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    //小缩放：放缩后一边占满屏幕，一边留白
    private float mSmallScale;
    //大缩放：放缩后一边超出屏幕，一边全屏
    private float mBigScale;
    //记录当前是小缩放还是大缩放
    private float mCurrentScale;
    private boolean isBigScale = false;
    //偏移量
    private float mOriginalOffsetX;
    private float mOriginalOffsetY;
    //
    private float mOffsetX;
    private float mOffsetY;
    //
    private OverScroller mOverScroller;
    private ScaleGestureDetector mScaleGestureDetector;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 求出初始偏移量，让图片居中
       inti();
    }

    private void inti(){
        mOriginalOffsetX = (getWidth() - mBitmap.getWidth()) / 2f;
        mOriginalOffsetY = (getHeight() - mBitmap.getHeight()) / 2f;
        if (mBitmap == null) return;
        if ((mBitmap.getWidth() / mBitmap.getHeight()) > (getWidth() / getHeight())) {
            mSmallScale = (float) getWidth() / mBitmap.getWidth();
            mBigScale = (float) getHeight() / mBitmap.getHeight() * 1.3f;
        } else {
            mSmallScale = (float) getHeight() / mBitmap.getHeight();
            mBigScale = (float) getWidth() / mBitmap.getWidth() * 1.3f;
        }
        mCurrentScale = mSmallScale;
    }

    public class PhotoGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isBigScale = !isBigScale;
            if (isBigScale) {
                mOffsetX = (e.getX() - getWidth() / 2f) - (e.getX() - getWidth() / 2f) * mBigScale / mSmallScale;
                mOffsetY = (e.getY() - getHeight() / 2f) - (e.getY() - getHeight() / 2f) * mBigScale / mSmallScale;
                measureOffset();
                getObjectAnimator().start();
            } else {
                getObjectAnimator().reverse();
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int pointerCount = e1.getPointerCount();
            if (mCurrentScale > mSmallScale) {
                if(pointerCount==2){
                    float x1=e2.getX(0);
                    float x2=e2.getX(1);
                    float y1=e2.getY(0);
                    float y2=e2.getY(1);
                    float x=(x1+x2)/2f;
                    float y=(y1+y2)/2f;
                    mOffsetX=x;
                    mOffsetY=y;
                }
                mOffsetX -= distanceX/mCurrentScale;
                mOffsetY -= distanceY/mCurrentScale;
                measureOffset();
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mCurrentScale > mSmallScale) {
                mOverScroller.fling((int) mOffsetX, (int) mOffsetY, (int) velocityX, (int) velocityY,
                        -(int) ((mBitmap.getWidth() * mBigScale - getWidth()) / 2f),
                        (int) ((mBitmap.getWidth() * mBigScale - getWidth()) / 2f),
                        -(int) ((mBitmap.getHeight() * mBigScale - getHeight()) / 2f),
                        (int) ((mBitmap.getHeight() * mBigScale - getHeight()) / 2f),
                        0, 0);
                postOnAnimation(new FlingRunner());
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    class FlingRunner implements Runnable {

        @Override
        public void run() {
            //判断动画是否在执行
            if (mOverScroller.computeScrollOffset()) {
                mOffsetX = mOverScroller.getCurrX();
                mOffsetY = mOverScroller.getCurrY();
                invalidate();
                postOnAnimation(this);
            }
        }
    }

    private void measureOffset() {
        mOffsetX = Math.min(mOffsetX, (mBitmap.getWidth() * mBigScale - getWidth()) / 2f);
        mOffsetX = Math.max(mOffsetX, -(mBitmap.getWidth() * mBigScale - getWidth()) / 2f);
        mOffsetY = Math.min(mOffsetY, (mBitmap.getHeight() * mBigScale - getHeight()) / 2f);
        mOffsetY = Math.max(mOffsetY, -(mBitmap.getHeight() * mBigScale - getHeight()) / 2f);
    }


    //属性动画
    private ObjectAnimator objectAnimator;

    private ObjectAnimator getObjectAnimator() {
        if (objectAnimator == null) {
            objectAnimator = ObjectAnimator.ofFloat(this, "mCurrentScale", 0);
        }
        //属性值的变化范围，慢慢从mSmallScale变到mBigScale
        objectAnimator.setFloatValues(mSmallScale, mBigScale);
        return objectAnimator;
    }

    public void setMCurrentScale(float mCurrentScale) {
        this.mCurrentScale = mCurrentScale;
        //可以使视图被重新绘画
        invalidate();
    }

    //双指缩放
    private class PhotoScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        float initialScale;

        //缩放
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //通过detector我们可以拿到缩放因子
            mCurrentScale = initialScale * detector.getScaleFactor();
            if(mCurrentScale>mSmallScale){
                isBigScale=true;
            }
            if(mCurrentScale>mBigScale*2f){
                mCurrentScale=mBigScale*2f;
            }
            if(mCurrentScale<mSmallScale){
                mCurrentScale=mSmallScale;
            }
            invalidate();
            return false;
        }

        //缩放前，要记得返回true，消费事件
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //储存一下当前的缩放值
            initialScale = mCurrentScale;
            return true;
        }

        //缩放后
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float i=((mBitmap.getWidth() * mBigScale - getWidth()) / 2f);
        if(mOffsetX==i||mOffsetX==-i||mCurrentScale==mSmallScale){
            getParent().requestDisallowInterceptTouchEvent(false); //父控件可以进行拦截事件`
            inti();
            invalidate();
        }else {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }
}