package doext.module.do_VerticalMarqueeLabel.implement;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.object.DoUIModule;

/**
 * Created by feng_ on 2017/3/8.
 */

public class OneMarqueeLabel extends RelativeLayout implements do_VerticalMarqueeLabel_View.DoIVerticalMarqueeLabel {

    /**
     * 轮播时间间隔
     */
    private long duration = 2000;
    private long delay = 1500;
    private boolean mSingleLine = true;
    private DoUIModule doUIModule;
    /**
     * 滚动方向
     */
    // 默认朝上,0为朝上,1为朝下
    private String scrollDirection = "up";

    private JSONArray mDataSource;

    private TextView mTvContentTop;

    private TextView mTvContentBottom;

    /**
     * 是否运行轮播图
     */
    protected boolean mIsRun;

    /**
     * 自动轮播使用的handler
     */
    private Handler mHandler;

    /**
     * 当前轮播的项索引
     */
    private int mCurrentItemIndex;
    private int heightSize;
    private Context ctx;

    public OneMarqueeLabel(Context context, DoUIModule _doUIModule) throws Exception {
        super(context);
        doUIModule = _doUIModule;
        scrollDirection = _doUIModule.getPropertyValue("direction");
        duration = DoTextHelper.strToLong(_doUIModule.getPropertyValue("duration"), 2000);
        ctx = context;
    }

    private void init(DoUIModule module) throws Exception {
        initTextView(module);
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.CENTER_VERTICAL);

        addView(mTvContentTop, lp1);
        addView(mTvContentBottom, lp1);
        mHandler = new SliderScrollHandler(this);
    }

    private void initTextView(DoUIModule module) throws Exception {
        mTvContentTop = new TextView(ctx);
        mTvContentBottom = new TextView(ctx);
        initLabel(mTvContentTop, module);
        initLabel(mTvContentBottom, module);
        if (mSingleLine) {
            mTvContentTop.setEllipsize(TextUtils.TruncateAt.END);
            mTvContentBottom.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private void initLabel(TextView textView, DoUIModule module) throws Exception {
        textView.setSingleLine(mSingleLine);
        textView.setTextColor(DoUIModuleHelper.getColorFromString(module.getPropertyValue("fontColor"), Color.BLACK));
        if (!"".equals(module.getPropertyValue("fontStyle"))) {
            DoUIModuleHelper.setFontStyle(textView, module.getPropertyValue("fontStyle"));
        }
        if (!"".equals(module.getPropertyValue("textFlag"))) {
            DoUIModuleHelper.setTextFlag(textView, module.getPropertyValue("textFlag"));
        }
        if ("".equals(module.getPropertyValue("fontSize"))) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(doUIModule, "17"));
        } else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(doUIModule, module.getPropertyValue("fontSize")));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        heightSize = getMeasuredHeight();
    }

    /**
     * 重置
     */
    private void resetData() throws JSONException {
        if (mDataSource == null || mDataSource.length() == 0) {
            return;
        }

        String normal = mDataSource.getString(0);

        if (normal != null) {
            mTvContentTop.setText(normal);
        }
    }

    /**
     * 滚动
     */
    private void autoSlider() throws JSONException {
        if (mDataSource == null) {
            return;
        } else {
            if (mDataSource.length() >= 2) {
                String normal = mDataSource.getString(mCurrentItemIndex);

                if (normal != null) {
                    mTvContentTop.setText(normal);
                }

                if (mCurrentItemIndex == mDataSource.length() - 1) {
                    mCurrentItemIndex = 0;
                } else {
                    mCurrentItemIndex = mCurrentItemIndex + 1;
                }
                String next = mDataSource.getString(mCurrentItemIndex);
                if (next != null) {
                    mTvContentBottom.setText(next);
                }
            } else if (mDataSource.length() == 1) {
                String normal = mDataSource.getString(0);
                if (normal != null) {
                    mTvContentTop.setText(normal);
                    mTvContentBottom.setText(normal);
                }
            } else {
                mTvContentTop.setText("");
                mTvContentBottom.setText("");
            }
            startTopAnim();
            startBottomAnim();
        }
    }

    ValueAnimator animatorTop;
    ValueAnimator animatorBottom;

    private void startTopAnim() {
        int value = -heightSize; // 默认朝上
        if ("down".equals(scrollDirection)) {
            // 朝下
            value = heightSize;
        }
        animatorTop = ObjectAnimator.ofFloat(mTvContentTop, "translationY", 0F, value);
        // 设置执行时间(1000ms)
        animatorTop.setDuration(duration);
        animatorTop.setInterpolator(new LinearInterpolator());
        animatorTop.start();
    }

    private void startBottomAnim() {
        int value = heightSize + mTvContentBottom.getHeight(); // 默认朝上
        if ("down".equals(scrollDirection)) {
            // 朝下
            value = -(heightSize + mTvContentBottom.getHeight());
        }
        animatorBottom = ObjectAnimator.ofFloat(mTvContentBottom, "translationY", value, 0F);
        animatorBottom.setDuration(duration);
        animatorBottom.setInterpolator(new LinearInterpolator());// 匀速动画
        animatorBottom.start();

        animatorBottom.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.sendEmptyMessageDelayed(0, delay);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    /**
     * 轮播滚动Handler
     */
    private static class SliderScrollHandler extends Handler {
        private WeakReference<OneMarqueeLabel> mSliderView;

        SliderScrollHandler(OneMarqueeLabel sliderView) {
            mSliderView = new WeakReference<OneMarqueeLabel>(sliderView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    OneMarqueeLabel sliderView = mSliderView.get();
                    if (sliderView != null && mSliderView.get().mIsRun) {
                        try {
                            sliderView.autoSlider();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void setDataSource(DoUIModule module) throws Exception {
        JSONArray jsonArray = new JSONArray(module.getPropertyValue("text"));
        mCurrentItemIndex = 0;
        if (this.mDataSource == null) {
            init(module);
            this.mDataSource = jsonArray;
            resetData();
        } else {
            this.mDataSource = jsonArray;
            animatorTop.cancel();
            animatorBottom.cancel();
        }
        startPlay();
    }

    /**
     * Description: 开始轮播
     */
    public void startPlay() {
        if (mHandler != null) {
            mIsRun = true;
            mHandler.removeCallbacksAndMessages(null);
            if (heightSize > 0) {
                mHandler.sendEmptyMessage(0);
            } else {
                mHandler.sendEmptyMessageDelayed(0, delay);
            }
        }
    }

    /**
     * Description: 暂停轮播
     */
    public void stopPlay() {
        if (mHandler != null) {
            mIsRun = false;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void setTextColor(String _color) {
        if (mTvContentTop != null) {
            mTvContentTop.setTextColor(DoUIModuleHelper.getColorFromString(_color, Color.BLACK));
        }
        if (mTvContentBottom != null) {
            mTvContentBottom.setTextColor(DoUIModuleHelper.getColorFromString(_color, Color.BLACK));
        }
    }

    @Override
    public void setFontStyle(String _fontStyle) {
        if (mTvContentTop != null) {
            DoUIModuleHelper.setFontStyle(mTvContentTop, _fontStyle);
        }
        if (mTvContentBottom != null) {
            DoUIModuleHelper.setFontStyle(mTvContentBottom, _fontStyle);
        }
    }

    @Override
    public void setFontSize(DoUIModule module) throws Exception {
        if (mTvContentTop != null) {
            mTvContentTop.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(doUIModule, module.getPropertyValue("fontSize")));
        }
        if (mTvContentBottom != null) {
            mTvContentBottom.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(doUIModule, module.getPropertyValue("fontSize")));
        }
    }

    @Override
    public void doDispose() {
        stopPlay();
        removeAllViews();
        this.mDataSource = null;
    }
}