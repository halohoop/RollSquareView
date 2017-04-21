package com.halohoop.rollsquareview.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.halohoop.rollsquareview.R;

/**
 * Created by Pooholah on 2017/4/21.
 */

public class RollSquareViewMain extends View {

    private FixSquare[] mFixSquares;
    private RollSquare mRollSquare;
    private float mHalfSquareWidth;
    private float mDividerWidth;
    private Paint mPaint;
    private boolean mIsClockwise;
    private int mStartEmptyPosition;
    private int mLineCount;

    public RollSquareViewMain(Context context) {
        this(context, null);
    }

    public RollSquareViewMain(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollSquareViewMain(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RollSquareViewMain);
        mLineCount = typedArray.getInteger(R.styleable.RollSquareViewMain_line_count, 3);
        if (mLineCount < 3) {
            mLineCount = 3;//至少要九个方块
        }
        mHalfSquareWidth = typedArray.getDimension(R.styleable.RollSquareViewMain_half_rect_width, 30);
        mDividerWidth = typedArray.getDimension(R.styleable.RollSquareViewMain_rect_divier_width, 10);
        mIsClockwise = typedArray.getBoolean(R.styleable.RollSquareViewMain_is_clockwise, true);
        mStartEmptyPosition = typedArray.getInteger(R.styleable.RollSquareViewMain_start_empty_position, 0);
        if (isInsideTheRect(mStartEmptyPosition, mLineCount)) {
            mStartEmptyPosition = 0;
        }
        typedArray.recycle();
    }

    private boolean isInsideTheRect(int pos, int lineCount) {
        if (pos < lineCount) {//是否第一行
            return false;
        } else if (pos > (lineCount * lineCount - 1 - lineCount)) {//是否最后一行
            return false;
        } else if ((pos + 1) % lineCount == 0) {//是否右边
            return false;
        } else if (pos % lineCount == 0) {//是否左边
            return false;
        }
        return true;
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        initSquares(mStartEmptyPosition);
    }

    private void initSquares(int startEmptyPosition) {
        //创建9个方块
        mFixSquares = new FixSquare[mLineCount * mLineCount];
        for (int i = 0; i < mFixSquares.length; i++) {
            mFixSquares[i] = new FixSquare();
            mFixSquares[i].index = i;
            mFixSquares[i].isShow = startEmptyPosition == i ? false : true;
            mFixSquares[i].rectF = new RectF();
        }
        //创建1个滚动方块
        mRollSquare = new RollSquare();
        mRollSquare.rectF = new RectF();
    }

    private class FixSquare {
        RectF rectF;
        int index;
        boolean isShow;
    }

    private class RollSquare {
        RectF rectF;
        float cx;
        float cy;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int cx = measuredWidth / 2;
        int cy = measuredHeight / 2;
        fixFixSquarePosition(mFixSquares, cx, cy, mDividerWidth, mHalfSquareWidth);
        fixRollSquarePosition(mFixSquares, mRollSquare, mStartEmptyPosition, mIsClockwise);
    }

    private void fixRollSquarePosition(FixSquare[] fixSquares,
                                       RollSquare rollSquare, int startEmptyPosition, boolean isClockwise) {

    }

    /**
     * 固定这些图形的rect位置
     *
     * @param fixSquares
     * @param cx
     * @param cy
     * @param dividerWidth
     * @param halfSquareWidth
     */
    private void fixFixSquarePosition(FixSquare[] fixSquares, int cx, int cy, float dividerWidth, float halfSquareWidth) {
        //确定第一个rect的位置
        float squareWidth = halfSquareWidth * 2;
        int lineCount = (int) Math.sqrt(fixSquares.length);
        float firstRectLeft = 0;
        float firstRectTop = 0;
        if (lineCount % 2 == 0) {//偶数
            int squareCountInAline = lineCount / 2;
            int diviCountInAline = squareCountInAline - 1;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                    + diviCountInAline * dividerWidth
                    + dividerWidth / 2;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
        } else {//奇数
            int squareCountInAline = lineCount / 2;
            int diviCountInAline = squareCountInAline;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                    + diviCountInAline * dividerWidth
                    + halfSquareWidth;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
        }
        for (int i = 0; i < lineCount; i++) {//行
            for (int j = 0; j < lineCount; j++) {//列
                if (i == 0) {
                    if (j == 0) {
                        fixSquares[0].rectF.set(firstRectLeft, firstRectTop,
                                firstRectLeft + squareWidth, firstRectTop + squareWidth);
                    } else {
                        int currIndex = i * lineCount + j;
                        fixSquares[currIndex].rectF.set(fixSquares[currIndex - 1].rectF);
                        fixSquares[currIndex].rectF.offset(dividerWidth + squareWidth, 0);
                    }
                } else {
//                Log.i(TAG, "fixFixSquarePosition: i:" + i + " j:" + j + " i*lineCount + j="+(i*lineCount + j));
                    //i * lineCount + j ==> currentIndex
                    int currIndex = i * lineCount + j;
                    fixSquares[currIndex].rectF.set(fixSquares[currIndex - lineCount].rectF);
                    fixSquares[currIndex].rectF.offset(0, dividerWidth + squareWidth);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mFixSquares.length; i++) {
            if (mFixSquares[i].isShow) {
                canvas.drawRoundRect(mFixSquares[i].rectF, 10, 10, mPaint);
            }
        }
    }
}
