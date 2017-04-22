package com.halohoop.rollsquareview.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.halohoop.rollsquareview.R;

/**
 * Created by Pooholah on 2017/4/21.
 */

public class RollSquareView extends View {

    private FixSquare[] mFixSquares;
    private RollSquare mRollSquare;
    private float mHalfSquareWidth;
    private float mDividerWidth;
    private Paint mPaint;
    private boolean mIsClockwise;
    private int mStartEmptyPosition;
    private int mCurrEmptyPosition;
    private int mLineCount;
    /**
     * 方框的圆角半径
     */
    private float mRollRoundCornor;
    private float mFixRoundCornor;

    private float mRotateDegree;
    private boolean mAllowRoll = false;
    private boolean mIsRolling = false;
    private int mSpeed = 250;
    private boolean mRollWhenShowAndStopWhenHide = false;
    /**
     * 一个方块的动画结束的后是否需要重置(再从startEmpty开始)
     */
    private boolean mIsReset = false;
    private int mSquareColor;
    /**
     * 动画插值器的全局变量
     * 默认为线性
     */
    private Interpolator mRollInterpolator;

    public RollSquareView(Context context) {
        this(context, null);
    }

    public RollSquareView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollSquareView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RollSquareView);
        mRollWhenShowAndStopWhenHide = typedArray.getBoolean(R.styleable.RollSquareView_roll_when_show_stop_when_hide, false);
        mLineCount = typedArray.getInteger(R.styleable.RollSquareView_line_count, 3);
        mRollRoundCornor = typedArray.getFloat(R.styleable.RollSquareView_roll_round_cornor, 10);
        mFixRoundCornor = typedArray.getFloat(R.styleable.RollSquareView_fix_round_cornor, 10);
        int rollInterpolatorResId = typedArray.getResourceId(R.styleable.RollSquareView_roll_interpolator,
                android.R.anim.linear_interpolator);
        mRollInterpolator = AnimationUtils.loadInterpolator(context, rollInterpolatorResId);
        int defaultColor = context.getResources().getColor(R.color.default_color);
        mSquareColor = typedArray.getColor(R.styleable.RollSquareView_square_color, defaultColor);
        mSpeed = typedArray.getInteger(R.styleable.RollSquareView_roll_speed, 250);
        if (mLineCount < 2) {
            mLineCount = 2;//至少要四个方块
        }
        mHalfSquareWidth = typedArray.getDimension(R.styleable.RollSquareView_half_rect_width, 30);
        mDividerWidth = typedArray.getDimension(R.styleable.RollSquareView_rect_divier_width, 10);
        mIsClockwise = typedArray.getBoolean(R.styleable.RollSquareView_is_clockwise, true);
        mStartEmptyPosition = typedArray.getInteger(R.styleable.RollSquareView_start_empty_position, 0);
        if (isInsideTheRect(mStartEmptyPosition, mLineCount)) {
            mStartEmptyPosition = 0;
        }
        mCurrEmptyPosition = mStartEmptyPosition;
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
        mPaint.setColor(mSquareColor);
        initSquares(mStartEmptyPosition);
    }

    private void initSquares(int startEmptyPosition) {
        //创建mLineCount * mLineCount个方块
        mFixSquares = new FixSquare[mLineCount * mLineCount];
        for (int i = 0; i < mFixSquares.length; i++) {
            mFixSquares[i] = new FixSquare();
            mFixSquares[i].index = i;
            mFixSquares[i].isShow = startEmptyPosition == i ? false : true;
            mFixSquares[i].rectF = new RectF();
        }
        //外圈链接起来
        linkTheOuterSquare(mFixSquares, mIsClockwise);
        //创建1个滚动方块
        mRollSquare = new RollSquare();
        mRollSquare.rectF = new RectF();
        mRollSquare.isShow = false;
    }

    private void linkTheOuterSquare(FixSquare[] fixSquares, boolean isClockwise) {
        int lineCount = (int) Math.sqrt(mFixSquares.length);
        //连接第一行
        for (int i = 0; i < lineCount; i++) {
            if (i % lineCount == 0) {//位于最左边
                fixSquares[i].next = isClockwise ? fixSquares[i + lineCount] : fixSquares[i + 1];
            } else if ((i + 1) % lineCount == 0) {//位于最右边
                fixSquares[i].next = isClockwise ? fixSquares[i - 1] : fixSquares[i + lineCount];
            } else {//中间
                fixSquares[i].next = isClockwise ? fixSquares[i - 1] : fixSquares[i + 1];
            }
        }
        //连接最后一行
        for (int i = (lineCount - 1) * lineCount; i < lineCount * lineCount; i++) {
            if (i % lineCount == 0) {//位于最左边
                fixSquares[i].next = isClockwise ? fixSquares[i + 1] : fixSquares[i - lineCount];
            } else if ((i + 1) % lineCount == 0) {//位于最右边
                fixSquares[i].next = isClockwise ? fixSquares[i - lineCount] : fixSquares[i - 1];
            } else {//中间
                fixSquares[i].next = isClockwise ? fixSquares[i + 1] : fixSquares[i - 1];
            }
        }
        //连接左边
        for (int i = 1 * lineCount; i <= (lineCount - 1) * lineCount; i += lineCount) {
            if (i == (lineCount - 1) * lineCount) {//如果是左下角的一个
                fixSquares[i].next = isClockwise ? fixSquares[i + 1] : fixSquares[i - lineCount];
                continue;
            }
            fixSquares[i].next = isClockwise ? fixSquares[i + lineCount] : fixSquares[i - lineCount];
        }
        //连接右边
        for (int i = 2 * lineCount - 1; i <= lineCount * lineCount - 1; i += lineCount) {
            if (i == lineCount * lineCount - 1) {//如果是右下角的一个
                fixSquares[i].next = isClockwise ? fixSquares[i - lineCount] : fixSquares[i - 1];
                continue;
            }
            fixSquares[i].next = isClockwise ? fixSquares[i - lineCount] : fixSquares[i + lineCount];
        }
    }

    private class FixSquare {
        RectF rectF;
        int index;
        boolean isShow;
        FixSquare next;
    }

    private class RollSquare {
        RectF rectF;
        int index;
        boolean isShow;
        /**
         * 旋转中心坐标
         */
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
        FixSquare fixSquare = fixSquares[startEmptyPosition];
        rollSquare.rectF.set(fixSquare.next.rectF);
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

    public void startRoll() {
        if (!mIsRolling && getVisibility() == View.VISIBLE) {
            mAllowRoll = true;
            FixSquare currEmptyFixSquare = mFixSquares[mCurrEmptyPosition];
            FixSquare rollSquare = currEmptyFixSquare.next;
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator translateConrtroller = createTranslateValueAnimator(currEmptyFixSquare,
                    rollSquare);
            ValueAnimator rollConrtroller = createRollValueAnimator();
            animatorSet.setInterpolator(mRollInterpolator);
            animatorSet.playTogether(translateConrtroller, rollConrtroller);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsRolling = true;
                    updateRollSquare();
                    //让空square的next隐藏，现在FixSquares中那就是有两个隐藏了
                    mFixSquares[mCurrEmptyPosition].next.isShow = false;
                    //然后滚动的suqare需要显示出来
                    mRollSquare.isShow = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsRolling = false;
                    mFixSquares[mCurrEmptyPosition].isShow = true;
                    mCurrEmptyPosition = mFixSquares[mCurrEmptyPosition].next.index;
                    //然后滚动的suqare隐藏
                    mRollSquare.isShow = false;
                    if (mAllowRoll) {
                        startRoll();
                    }
                    if (mIsReset) {
                        mCurrEmptyPosition = mStartEmptyPosition;
                        //重置所有的
                        for (int i = 0; i < mFixSquares.length; i++) {
                            mFixSquares[i].isShow = true;
                        }
                        mFixSquares[mCurrEmptyPosition].isShow = false;
                        updateRollSquare();
                        invalidate();
                        startRoll();
                        mIsReset = false;
                    }
                }
            });
            animatorSet.start();
        }
    }

    private void updateRollSquare() {
        mRollSquare.rectF.set(mFixSquares[mCurrEmptyPosition].next.rectF);
        mRollSquare.index = mFixSquares[mCurrEmptyPosition].next.index;
        setRollSquareRotateCenter(mRollSquare, mIsClockwise);
    }

    public void stopRoll() {
        mAllowRoll = false;
    }

    public void resetRoll() {
        stopRoll();
        mIsReset = true;
    }

    private void setRollSquareRotateCenter(RollSquare rollSquare, boolean isClockwise) {
        if (rollSquare.index == 0) {//左上角
            rollSquare.cx = rollSquare.rectF.right;
            rollSquare.cy = rollSquare.rectF.bottom;
        } else if (rollSquare.index == mLineCount * mLineCount - 1) {//右下角
            rollSquare.cx = rollSquare.rectF.left;
            rollSquare.cy = rollSquare.rectF.top;
        } else if (rollSquare.index == mLineCount * (mLineCount - 1)) {//左下角
            rollSquare.cx = rollSquare.rectF.right;
            rollSquare.cy = rollSquare.rectF.top;
        } else if (rollSquare.index == mLineCount - 1) {//右上角
            rollSquare.cx = rollSquare.rectF.left;
            rollSquare.cy = rollSquare.rectF.bottom;
        }
        //以下和顺不顺时针有关，其中判断条件还包含了角落的，但是无关紧要，上边的判断已经过滤掉角落的了
        //走到下面的判断的都不可能是角落的index
        else if (rollSquare.index % mLineCount == 0) {//左边
            rollSquare.cx = rollSquare.rectF.right;
            rollSquare.cy = isClockwise ? rollSquare.rectF.top : rollSquare.rectF.bottom;
        } else if (rollSquare.index < mLineCount) {//上边
            rollSquare.cx = isClockwise ? rollSquare.rectF.right : rollSquare.rectF.left;
            rollSquare.cy = rollSquare.rectF.bottom;
        } else if ((rollSquare.index + 1) % mLineCount == 0) {//右边
            rollSquare.cx = rollSquare.rectF.left;
            rollSquare.cy = isClockwise ? rollSquare.rectF.bottom : rollSquare.rectF.top;
        } else if (rollSquare.index > (mLineCount - 1) * mLineCount) {//下边
            rollSquare.cx = isClockwise ? rollSquare.rectF.left : rollSquare.rectF.right;
            rollSquare.cy = rollSquare.rectF.top;
        }
    }

    private ValueAnimator createRollValueAnimator() {
        ValueAnimator rollAnim = ValueAnimator.ofFloat(0, 90).setDuration(mSpeed);
//        rollAnim.setInterpolator(mRollInterpolator);
        rollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object animatedValue = animation.getAnimatedValue();
                mRotateDegree = (float) animatedValue;
                invalidate();
            }
        });
        return rollAnim;
    }

    private ValueAnimator createTranslateValueAnimator(FixSquare currEmptyFixSquare,
                                                       FixSquare rollSquare) {
        float startAnimValue = 0;
        float endAnimValue = 0;
        PropertyValuesHolder left = null;
        PropertyValuesHolder top = null;
        ValueAnimator valueAnimator = new ValueAnimator().setDuration(mSpeed);
//        valueAnimator.setInterpolator(mRollInterpolator);
        if (isNextRollLeftOrRight(currEmptyFixSquare, rollSquare)) {
            if (mIsClockwise && currEmptyFixSquare.index > rollSquare.index//顺时针且在第一行
                    || !mIsClockwise && currEmptyFixSquare.index > rollSquare.index) {//逆时针且在最后一行
                //向右滚
                startAnimValue = rollSquare.rectF.left;
                endAnimValue = rollSquare.rectF.left + mDividerWidth;
            } else if (mIsClockwise && currEmptyFixSquare.index < rollSquare.index//顺时针且在最后一行
                    || !mIsClockwise && currEmptyFixSquare.index < rollSquare.index) {//逆时针且在第一行
                //向左滚
                startAnimValue = rollSquare.rectF.left;
                endAnimValue = rollSquare.rectF.left - mDividerWidth;
            }
            left = PropertyValuesHolder.ofFloat("left", startAnimValue, endAnimValue);
            valueAnimator.setValues(left);
        } else {
            if (mIsClockwise && currEmptyFixSquare.index < rollSquare.index//顺时针且在最左列
                    || !mIsClockwise && currEmptyFixSquare.index < rollSquare.index) {//逆时针且在最右列
                //向上滚
                startAnimValue = rollSquare.rectF.top;
                endAnimValue = rollSquare.rectF.top - mDividerWidth;
            } else if (mIsClockwise && currEmptyFixSquare.index > rollSquare.index//顺时针且在最右列
                    || !mIsClockwise && currEmptyFixSquare.index > rollSquare.index) {//逆时针且在最左列
                //向下滚
                startAnimValue = rollSquare.rectF.top;
                endAnimValue = rollSquare.rectF.top + mDividerWidth;
            }
            top = PropertyValuesHolder.ofFloat("top", startAnimValue, endAnimValue);
            valueAnimator.setValues(top);
        }
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object left = animation.getAnimatedValue("left");
                Object top = animation.getAnimatedValue("top");
                if (left != null) {
                    mRollSquare.rectF.offsetTo((Float) left, mRollSquare.rectF.top);
                }
                if (top != null) {
                    mRollSquare.rectF.offsetTo(mRollSquare.rectF.left, (Float) top);
                }
                setRollSquareRotateCenter(mRollSquare, mIsClockwise);
                invalidate();
            }
        });
        return valueAnimator;
    }

    /**
     * 如果不是左右运动那就是上下运动
     *
     * @param currEmptyFixSquare
     * @param rollSquare
     * @return
     */
    private boolean isNextRollLeftOrRight(FixSquare currEmptyFixSquare, FixSquare rollSquare) {
        if (currEmptyFixSquare.rectF.left - rollSquare.rectF.left == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mFixSquares.length; i++) {
            if (mFixSquares[i].isShow) {
                canvas.drawRoundRect(mFixSquares[i].rectF, mFixRoundCornor, mFixRoundCornor, mPaint);
            }
        }
        if (mRollSquare.isShow) {
            canvas.rotate(mIsClockwise ? mRotateDegree : -mRotateDegree, mRollSquare.cx, mRollSquare.cy);
            canvas.drawRoundRect(mRollSquare.rectF, mRollRoundCornor, mRollRoundCornor, mPaint);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            if (mRollWhenShowAndStopWhenHide) {
                startRoll();
            }
        } else {
            stopRoll();
        }
    }
}
