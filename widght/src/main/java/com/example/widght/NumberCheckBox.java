package com.example.widght;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NumberCheckBox extends View {

    static ArrayList<NumberCheckBox> numberCheckBoxes=new ArrayList<>();
    private int mSelectColor = Color.RED;
    private int mNoSelectColor = Color.GREEN;
    private final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//实心圆画笔
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//文本画笔
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//圆环画笔

    private int mTextColor;//填在环里面的数字的颜色
    private int mStrokeColor ;//画笔的颜色


    private float mStrokeWidth;//画笔的宽度

    private int mCenterX;//圆心x坐标
    private int mCenterY;//圆心y坐标

    private float mCircleRadius;//实心圆半径

    private boolean isSelect = false;
    private String numberText="";

    private OnOnStateChangeListener listener;
    private static final String MARKED_WORD="您最多只能选择7个图片";

    //监听点击事件
    public void setOnStateChangeListener(OnOnStateChangeListener listener) {
        this.listener = listener;
    }

    public interface OnOnStateChangeListener {
        void onClick(boolean isSelected);
    }

    public NumberCheckBox(Context context) {
        super(context);
        init();
    }

    public NumberCheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberCheckBox);
        mSelectColor = typedArray.getColor(R.styleable.NumberCheckBox_circle_color_normal, getResources().getColor(R.color.selectGreen));
        mNoSelectColor = typedArray.getColor(R.styleable.NumberCheckBox_circle_color_press, Color.TRANSPARENT);
        mStrokeColor = typedArray.getColor(R.styleable.NumberCheckBox_strokeColor, Color.BLACK);
        mStrokeWidth = typedArray.getFloat(R.styleable.NumberCheckBox_strokeWidth, 4);
        typedArray.recycle();
        init();
    }

    public NumberCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(34);
        mCirclePaint.setColor(mNoSelectColor);
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        setClickable(true);
        setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(isSelect);
            }
            toggle();
        });

    }

    private void toggle() {
        isSelect = !isSelect;
        if(isSelect){
            if(numberCheckBoxes.size()<7){
                Log.d("zswy","dzzd");
                numberCheckBoxes.add(this);
                numberText=String.valueOf(numberCheckBoxes.size());
            }else {
                isSelect=false;
                Toast.makeText(getContext(),MARKED_WORD , Toast.LENGTH_SHORT).show();
            }
        }else {
            numberCheckBoxes.remove(this);
            for (int j = 0; j < numberCheckBoxes.size(); j++) {
                String s=String.valueOf(j+1);
                numberCheckBoxes.get(j).refresh(s);
            }
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawText(canvas);
        drawRing(canvas);
    }
    private void refresh(String numberText){
        this.numberText=numberText;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    private void drawText(Canvas canvas) {
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(numberText, 0, numberText.length(), bounds);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        float x = getMeasuredWidth() / 2f;
        float y = (getMeasuredHeight() - fontMetrics.bottom - fontMetrics.top) / 2f ;
        if (isSelect) {
            canvas.drawText(numberText, x, y, mTextPaint);
        } else {
            canvas.drawText("", x, y, mTextPaint);
        }
    }

    private void drawCircle(Canvas canvas) {
        mCircleRadius = Math.min(mCenterX, mCenterY) / 2f;
        if (isSelect) {
            mCirclePaint.setColor(mSelectColor);
        } else {
            mCirclePaint.setColor(mNoSelectColor);
        }
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mCirclePaint);
    }

    private void drawRing(Canvas canvas) {
        float mRingRadius;//圆环半径
        RectF rectF = new RectF();
        mRingRadius = mCircleRadius + (mStrokeWidth) / 2;
        rectF.top = mCenterY - mRingRadius;
        rectF.bottom = mCenterY + mRingRadius;
        rectF.left = mCenterX - mRingRadius;
        rectF.right = mCenterX + mRingRadius;
        canvas.drawArc(rectF, 0, 360, false, mStrokePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingEnd = getPaddingEnd();
        mCenterX = (getWidth() - paddingRight - paddingLeft) / 2;
        mCenterY = (getHeight() - paddingTop - paddingEnd) / 2;
    }


}
