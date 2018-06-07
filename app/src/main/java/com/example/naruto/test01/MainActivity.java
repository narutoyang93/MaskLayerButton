package com.example.naruto.test01;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConstraintLayout editLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        addSoftInputToggleListenerForScrollView((View) editLayout.getParent(), editLayout, findViewById(R.id.line));
    }

    /**
     * 此方法用于处理软键盘遮挡布局控件的问题，当软键盘弹出时，将布局往上滚动
     *
     * @param parentView 需要滚动的外层布局
     * @param childView  当软键盘弹出时，parentView中最靠近（垂直方向）软键盘的一个子控件
     */
    public void addSoftInputToggleListenerForScrollView(final View parentView, final View childView, final View line) {
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public boolean isHasScroll = false;//布局是否已经滚动过

            @Override
            public void onGlobalLayout() {
                System.out.println("--->onGlobalLayout");
                Rect rect = new Rect();

                parentView.getWindowVisibleDisplayFrame(rect);
                System.out.println("--->rect.bottom=" + rect.bottom);
                if (rect.bottom < parentView.getRootView().getHeight() * 2 / 3) {//键盘已弹出
                    int[] location = new int[2];
                    childView.getLocationInWindow(location);
                    int scrollHeight = (location[1] + childView.getHeight()) - rect.bottom;
                    if (scrollHeight > 0) {//控件被遮挡
                        System.out.println("--->滚动布局");
                        parentView.scrollTo(0, scrollHeight);//往上滚动
                        if (line != null) {
                            line.setVisibility(View.VISIBLE);
                        }
                        isHasScroll = true;
                    }
                } else {//键盘已隐藏
                    if (isHasScroll) {//键盘弹出时布局有滚动过
                        System.out.println("--->滚回原位");
                        parentView.scrollTo(0, 0);//滚回原位
                        if (line != null) {
                            line.setVisibility(View.GONE);
                        }
                        isHasScroll = false;
                    }
                }
            }
        });
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
