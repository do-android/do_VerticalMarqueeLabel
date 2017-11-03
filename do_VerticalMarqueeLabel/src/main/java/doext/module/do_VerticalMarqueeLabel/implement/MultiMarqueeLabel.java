package doext.module.do_VerticalMarqueeLabel.implement;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;

import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.object.DoUIModule;

/**
 * Created by feng_ on 2017/3/15.
 */

public class MultiMarqueeLabel extends ScrollView implements do_VerticalMarqueeLabel_View.DoIVerticalMarqueeLabel {

    /**
     * 轮播时间间隔
     */
    private long duration = 2000;
    private DoUIModule doUIModule;
    private Handler mHandler;
    /**
     * 滚动方向,默认朝上
     */
    private String scrollDirection = "up";

    private JSONArray mDataSource;
    Context ctx;

    public MultiMarqueeLabel(Context context, DoUIModule _doUIModule) throws Exception {
        super(context);
        ctx = context;
        doUIModule = _doUIModule;
        scrollDirection = _doUIModule.getPropertyValue("direction");
        duration = DoTextHelper.strToLong(_doUIModule.getPropertyValue("duration"), 2000);
        mHandler = new SliderScrollHandler(this);
        // 禁止scrollview滑动
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });
    }


    TextView textView;

    private void init(DoUIModule module) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mDataSource.length(); i++) {
            String item = mDataSource.getString(i);
            if (i == mDataSource.length() - 1) {
                stringBuilder.append(item);
            } else {
                stringBuilder.append(item).append("\n");
            }
        }
        if (textView == null) {
            textView = new TextView(ctx);
            addView(textView);
        }
        textView.setText(stringBuilder.toString());
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

    ObjectAnimator mAnimator;

    public void startAnimation() {
        int fromTo = getTextViewHeight(textView);
        if ("down".equals(scrollDirection)) {
            mAnimator = ObjectAnimator.ofFloat(textView, "translationY", -fromTo, height);
        } else {
            mAnimator = ObjectAnimator.ofFloat(textView, "translationY", height, -fromTo);
        }
        mAnimator.setDuration(duration);
        // 系统默认是AccelerateDecelerateInterpolator 先加速 后减速
        mAnimator.setInterpolator(new LinearInterpolator()); // 匀速
        mAnimator.setRepeatCount(-1);
        mAnimator.start();
    }

    /**
     * 轮播滚动Handler
     */
    private static class SliderScrollHandler extends Handler {
        private WeakReference<MultiMarqueeLabel> mSliderView;

        SliderScrollHandler(MultiMarqueeLabel sliderView) {
            mSliderView = new WeakReference<MultiMarqueeLabel>(sliderView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    MultiMarqueeLabel sliderView = mSliderView.get();
                    if (sliderView != null) {
                        sliderView.startAnimation();
                    }
                    break;
            }
        }
    }

    int height = 0;

    @Override
    public void setTextColor(String _color) {
        if (textView != null) {
            textView.setTextColor(DoUIModuleHelper.getColorFromString(_color, Color.BLACK));
        }
    }

    @Override
    public void setFontStyle(String _fontStyle) {
        if (textView != null) {
            DoUIModuleHelper.setFontStyle(textView, _fontStyle);
        }
    }

    @Override
    public void setFontSize(DoUIModule module) throws Exception {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
            init(module);
            startAnimation();
        } else {
            init(module);
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    @Override
    public void setDataSource(DoUIModule module) throws Exception {
        JSONArray jsonArray = new JSONArray(module.getPropertyValue("text"));
        this.mDataSource = jsonArray;
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
            init(module);
            startAnimation();
        } else {
            init(module);
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * Description: 暂停轮播
     */
    public void stopPlay() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getMeasuredHeight();
    }

    @Override
    public void doDispose() {
        stopPlay();
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mDataSource = null;
    }

    private int getTextViewHeight(TextView pTextView) {
        pTextView.onPreDraw();
        Layout layout = pTextView.getLayout();
        int desired = layout.getLineTop(pTextView.getLineCount());
        int padding = pTextView.getCompoundPaddingTop() + pTextView.getCompoundPaddingBottom();
        return desired + padding;
    }
}
