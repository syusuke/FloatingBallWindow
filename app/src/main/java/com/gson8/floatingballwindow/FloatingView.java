package com.gson8.floatingballwindow;

/*
 * FloatingBallWindow making by Syusuke/琴声悠扬 on 2016/6/1
 * E-Mail: Zyj7810@126.com
 * Package: com.gson8.floatingballwindow.FloatingView
 * Description: null
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class FloatingView {

    private static final String TAG = "tagggg";

    private static final int WHAT_HIDE = 0x268;

    private static final long AUTO_RELEASE_ALPHA_TIME = 3000;


    private WindowManager.LayoutParams mLayoutParams;
    private DisplayMetrics mDisplayMetrics;
    private WindowManager mWindowManager;

    private Context mContext;
    private View mContentView;

    private static final float DISTANCE = 45.0f;  //  点击偏移量   在上、下、左、右这个范围之内都会触发点击事件
    private long mLastTouchTimeMillis;

    private float offsetX;
    private float offsetY;

    private boolean mIsShowing;
    private float downX;
    private float downY;
    private float oldX;
    private float oldY;
    private boolean mIsOpen;

    private View mFloatingView;
    private View mPopupView;


    /**
     * 无参构造方法
     *
     * @param context
     */
    public FloatingView(Context context) {
        this(context, null, null);
    }


    /**
     * 带参数的构造方法
     *
     * @param context
     * @param floatingView
     * @param popupView
     */
    public FloatingView(Context context, View floatingView, View popupView) {
        this.mContext = context;
        setFloatingView(floatingView);
        setPopupView(popupView);
        initWindowManager();
        initLayoutParams();
    }


    /**
     * 设置开始的视图
     *
     * @param floatingView
     */
    public void setFloatingView(View floatingView) {
        if(floatingView != null) {
            this.mFloatingView = floatingView;
            setContentView(mFloatingView);
        }
    }


    /**
     * 设置展开后的视图
     *
     * @param popupView
     */
    public void setPopupView(View popupView) {
        if(popupView != null) {
            PopupBackgroundView backgroundView = new PopupBackgroundView(getContext());
            RelativeLayout.LayoutParams lp =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            popupView.setOnTouchListener(new TouchIntercept());
            popupView.setLayoutParams(lp);
            backgroundView.addView(popupView);
            this.mPopupView = backgroundView;
        }
    }

    /**
     * 初始化窗口管理器
     */
    private void initWindowManager() {
        mWindowManager = (WindowManager) getContext().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    /**
     * 初始化WindowManager.LayoutParams参数
     */
    private void initLayoutParams() {
        getLayoutParams().flags = getLayoutParams().flags
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getLayoutParams().dimAmount = 0.2f;
        getLayoutParams().type = WindowManager.LayoutParams.TYPE_TOAST;
        getLayoutParams().height = WindowManager.LayoutParams.WRAP_CONTENT;
        getLayoutParams().width = WindowManager.LayoutParams.WRAP_CONTENT;
        getLayoutParams().gravity = Gravity.START | Gravity.TOP;
        getLayoutParams().format = PixelFormat.RGBA_8888;
        getLayoutParams().alpha = 1.0f;  //  设置整个窗口的透明度
        offsetX = 0;
        offsetY = getStatusBarHeight(getContext());
        getLayoutParams().x = (int) (mDisplayMetrics.widthPixels - offsetX);
        getLayoutParams().y = (int) (mDisplayMetrics.heightPixels * 1.0f / 4 - offsetY);
    }

    /**
     * 设置当前窗口布局
     *
     * @param contentView
     */
    private void setContentView(View contentView) {
        if(contentView != null) {
            if(getIsShowing()) {
                getWindowManager().removeView(mContentView);
                createContentView(contentView);
                getWindowManager().addView(mContentView, getLayoutParams());
                updateLocation(getDisplayMetrics().widthPixels / 2,
                        getDisplayMetrics().heightPixels / 2, true);
            } else {
                createContentView(contentView);
            }
        }
    }

    /**
     * 创建一个窗口显示的内容
     *
     * @param contentView
     */
    private void createContentView(View contentView) {
        this.mContentView = contentView;
        contentView.measure(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED); // 主动计算视图View的宽高信息
        offsetY = getStatusBarHeight(getContext()) + contentView.getMeasuredHeight() / 2;
        offsetX = contentView.getMeasuredWidth() / 2;
        contentView.setOnTouchListener(new WindowTouchListener());
    }


    /**
     * 带有按键监听和触摸事件的View
     */
    class PopupBackgroundView extends RelativeLayout {

        public PopupBackgroundView(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if(getKeyDispatcherState() == null) {
                    return super.dispatchKeyEvent(event);
                }

                if(event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if(state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if(event.getAction() == KeyEvent.ACTION_UP) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if(state != null && state.isTracking(event) && !event.isCanceled()) {
                        turnMini();
                        return true;
                    }
                }
                return super.dispatchKeyEvent(event);
            } else {
//                TODO
//                return super.dispatchKeyEvent(event);
                return false;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_UP:
                    Log.e(TAG, "onTouchEvent: ACTION_UP");
                    turnMini();
                    return true;
                case MotionEvent.ACTION_OUTSIDE:
                    Log.e(TAG, "onTouchEvent: ACTION_OUTSIDE");
                    turnMini();
                    return true;
            }
            Log.e(TAG, "onTouchEvent: ");
            return super.onTouchEvent(event);
        }
    }


    class TouchIntercept implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_UP:
                    mLastTouchTimeMillis = System.currentTimeMillis();
                    break;
            }
            return true;
        }
    }


    /**
     * 窗口监听类回调接口
     */
    class WindowTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mIsOpen) {
                        down(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(!mIsOpen) {
                        move(event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(!mIsOpen) {
                        up(event);
                    }
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    if(mIsOpen) {
                        turnMini();
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 按下的事件
     *
     * @param event
     */
    private void down(MotionEvent event) {
        downX = event.getRawX();
        downY = event.getRawY();

        getLayoutParams().alpha = 1.0f;

        mLastTouchTimeMillis = System.currentTimeMillis();

        getWindowManager().updateViewLayout(getContentView(), getLayoutParams());

    }

    /**
     * 移动事件
     *
     * @param event
     */
    private void move(MotionEvent event) {
        mLastTouchTimeMillis = System.currentTimeMillis();
        updateLocation(event.getRawX(), event.getRawY(), true);
    }

    /**
     * 手指弹起的时候的事件
     *
     * @param event
     */
    private void up(MotionEvent event) {

        Log.e(TAG, "up: ");

        float x = event.getRawX();
        float y = event.getRawY();

        if(x >= downX - DISTANCE && x <= downX + DISTANCE && y >= downY - DISTANCE &&
                y <= downY + DISTANCE) {
            //显示 popupView
            Log.e(TAG, "up: Show popupView");
            showPopupView();
        } else {
            //给一个动画去贴边
            Log.e(TAG, "up: go back");
            ValueAnimator animator = alignAnimator(x, y);
            animator.start();
        }
    }


    /**
     * 返回原始状态的球
     */
    public void turnMini() {

        if(!mIsOpen) {
            return;
        }
        mIsOpen = false;
        getLayoutParams().flags &=
                ~(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);// 取消WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE属性
        getLayoutParams().flags |=
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//重新设置WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE属性
        getLayoutParams().height = WindowManager.LayoutParams.WRAP_CONTENT;
        getLayoutParams().width = WindowManager.LayoutParams.WRAP_CONTENT;
        setContentView(mFloatingView);
        getLayoutParams().alpha = 1.0f;
        updateLocation(oldX, oldY, false);
        mLastTouchTimeMillis = System.currentTimeMillis();
        mHandler.sendEmptyMessage(WHAT_HIDE);
    }


    /**
     * 打开选项菜单
     */
    public void showPopupView() {
        if(mIsOpen)
            return;

        Log.e(TAG, "showPopupView: ");

        getLayoutParams().flags &=
                ~(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);// 取消WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE属性
        getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;  //
        getLayoutParams().width = WindowManager.LayoutParams.MATCH_PARENT;  //
        oldX = getLayoutParams().x;
        oldY = getLayoutParams().y;
        setContentView(mPopupView);
        mHandler.removeMessages(WHAT_HIDE);
        mIsOpen = true;
    }


    /**
     * 更新窗口的位置
     */
    private void updateLocation(float x, float y, boolean offset) {
        if(getContentView() != null) {
            if(offset) {
                getLayoutParams().x = (int) (x - offsetX);
                getLayoutParams().y = (int) (y - offsetY);
            } else {
                getLayoutParams().x = (int) x;
                getLayoutParams().y = (int) y;
            }
            getWindowManager().updateViewLayout(mContentView, getLayoutParams());
        }
    }


    /**
     * 自动对齐的一个小动画（自定义属性动画），使自动贴边的时候显得不那么生硬
     */
    private ValueAnimator alignAnimator(float x, float y) {
        ValueAnimator animator = null;
        if(x <= getDisplayMetrics().widthPixels / 2) {
            animator = ValueAnimator.ofObject(new PointEvaluator(), new Point((int) x, (int) y),
                    new Point(0, (int) y));
        } else {
            animator = ValueAnimator.ofObject(new PointEvaluator(), new Point((int) x, (int) y),
                    new Point(getDisplayMetrics().widthPixels, (int) y));
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                updateLocation(point.x, point.y, true);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLastTouchTimeMillis = System.currentTimeMillis();
                mHandler.sendEmptyMessage(WHAT_HIDE);
            }
        });
        animator.setDuration(160);
        return animator;
    }


    /**
     * 动画差值器
     */
    public class PointEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object from, Object to) {
            Point startPoint = (Point) from;
            Point endPoint = (Point) to;
            float x = startPoint.x + fraction * (endPoint.x - startPoint.x);
            float y = startPoint.y + fraction * (endPoint.y - startPoint.y);
            Point point = new Point((int) x, (int) y);
            return point;
        }
    }

    /**
     * 获取状态栏的高度
     */
    public int getStatusBarHeight(Context context) {
        int height = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resId > 0) {
            height = context.getResources().getDimensionPixelSize(resId);
        }
        return height;
    }


    /**
     * 显示窗口
     */
    public void show() {
        if(getContentView() != null && !getIsShowing()) {
            getWindowManager().addView(getContentView(), getLayoutParams());
            mIsShowing = true;
            if(!mIsOpen) {
                mHandler.sendEmptyMessage(WHAT_HIDE);
            }
        }
    }

    /**
     * 隐藏当前显示窗口
     */
    public void dismiss() {
        if(getContentView() != null && getIsShowing()) {
            getWindowManager().removeView(getContentView());
            mIsShowing = false;
        }
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        return this.mContext;
    }

    /**
     * 获取WindowManager
     *
     * @return
     */
    public WindowManager getWindowManager() {
        if(mWindowManager == null) {
            mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 获取当前正在显示的视频
     *
     * @return 当前视图
     */
    public View getContentView() {
        return mContentView;
    }


    /**
     * 判断当前是否有显示窗口
     *
     * @return 有true/没有false
     */
    public boolean getIsShowing() {
        return mIsShowing;
    }

    /**
     * 获取显示信息
     *
     * @return
     */
    public DisplayMetrics getDisplayMetrics() {
        if(mDisplayMetrics == null) {
            mDisplayMetrics = getContext().getResources().getDisplayMetrics();
        }
        return mDisplayMetrics;
    }

    /**
     * 获取 WindowManager.LayoutParams 参数
     *
     * @return
     */
    public WindowManager.LayoutParams getLayoutParams() {
        if(mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
            initLayoutParams();
        }
        return mLayoutParams;
    }

    /*
            Handler ------------------------------------------------------------------------
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case WHAT_HIDE:
                    if(System.currentTimeMillis() - mLastTouchTimeMillis >=
                            AUTO_RELEASE_ALPHA_TIME) {
                        if(!mIsOpen) {
                            getLayoutParams().alpha = 0.4f;
                            getWindowManager()
                                    .updateViewLayout(getContentView(), getLayoutParams());
                        }
                    } else {
                        if(mIsOpen) {
                            mLastTouchTimeMillis =
                                    System.currentTimeMillis() + AUTO_RELEASE_ALPHA_TIME;
                        }
                        mHandler.sendEmptyMessageDelayed(WHAT_HIDE, 300);
                    }
                    break;
            }
        }
    };

}
