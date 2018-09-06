package com.example.naruto.test01;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @Purpose 具有遮罩层的按钮，按下后可以在文本上方显示遮罩层，无需selector,支持圆角和描边，无需自定义shape
 * @Author Naruto Yang
 * @CreateDate ${2018-06-07}
 * @Note
 */
public class MaskLayerButton extends AppCompatButton {
    private Context context;
    private final static int DEFAULT_MASK_LAYER_COLOR = 0x21000000;
    private final static int DEFAULT_STROKE_WIDTH = 1;//单位：dp
    private boolean isOnPress = false;//是否处于按下状态
    private boolean isNeedMaskLayer;//是否需要遮罩层
    private int maskLayerColor;//遮罩层颜色
    private float strokeWidth;//描边画笔宽度//单位：px
    private int strokeColor;//描边颜色
    private int radius;//圆角半径


    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public MaskLayerButton(Context context) {
        super(context);
        this.context = context;
    }

    public MaskLayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public MaskLayerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        /**
         * 通过这个方法，将attrs.xml中定义的declare-styleable的所有属性的值存储到TypedArray中
         */
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MaskLayerButton);
        //从TypedArray中取出对应的值来为要设置的属性赋值
        radius = ta.getDimensionPixelSize(R.styleable.MaskLayerButton_radius, 0);
        isNeedMaskLayer = ta.getBoolean(R.styleable.MaskLayerButton_needMaskLayer, true);
        maskLayerColor = ta.getColor(R.styleable.MaskLayerButton_maskLayerColor, DEFAULT_MASK_LAYER_COLOR);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.MaskLayerButton_strokeWidth, dip2px(DEFAULT_STROKE_WIDTH));
        strokeColor = ta.getColor(R.styleable.MaskLayerButton_strokeColor, -1);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        Drawable background = getBackground();
        if (radius > 0 && background != null) {//如果圆角半径>0且背景不为空，需要绘制圆角背景，需在父类调用onDraw()前，避免覆盖文字
            paint.reset();
            Rect rect = canvas.getClipBounds();
            Bitmap bitmap;
            if (background instanceof ColorDrawable) {
                ColorDrawable colordDrawable = (ColorDrawable) background;
                int color = colordDrawable.getColor();

                //生成纯色bitmap
                bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(color);//填充颜色
            } else {
                bitmap = ((BitmapDrawable) background).getBitmap();
            }
            Bitmap roundBitmap = getRoundBitmap(bitmap);
            setBackgroundDrawable(new BitmapDrawable(roundBitmap));//用圆角化后的背景替换原有背景
        }
        super.onDraw(canvas);
        if (strokeColor != -1) {//描边
            drawMaskLayerOrContour(paint, canvas, false);
        }
        if (isNeedMaskLayer) {
            if (isOnPress) {
                drawMaskLayerOrContour(paint, canvas, true);
            }
        }
    }

    /**
     * 绘制遮罩层或描边
     *
     * @param paint
     * @param canvas
     * @param isMaskLayer
     */
    private void drawMaskLayerOrContour(Paint paint, Canvas canvas, boolean isMaskLayer) {
        paint.reset();
        if (isMaskLayer) {//遮罩层
            paint.setColor(maskLayerColor);
            paint.setStyle(Paint.Style.FILL);
        } else {//描边
            paint.setColor(strokeColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
        if (radius > 0) {//圆角矩形
            paint.setAntiAlias(true);
            RectF rf = new RectF(canvas.getClipBounds());
            if (!isMaskLayer) {
                float padding = strokeWidth / 2;
                rf.bottom -= padding;
                rf.right -= padding;
                rf.top += padding;
                rf.left += padding;
            }
            canvas.drawRoundRect(rf, radius, radius, paint);
        } else {//矩形
            canvas.drawRect(canvas.getClipBounds(), paint);
        }
    }

    /**
     * 获取圆角矩形图片方法
     *
     * @param bitmap
     * @return Bitmap
     * @author caizhiming
     */
    private Bitmap getRoundBitmap(Bitmap bitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        final int color = 0xff424242;

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();

        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outputBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isNeedMaskLayer) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isOnPress = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isTouchOnView(event)) {
                        resetOnPressState();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    resetOnPressState();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 触摸点是否在当前控件内(圆角问题暂不考虑)
     *
     * @param event
     * @return
     */
    public boolean isTouchOnView(MotionEvent event) {
        //当前手指坐标
        float eventRawX = event.getRawX();
        float eventRawY = event.getRawY();

        //获取控件在屏幕的位置
        int[] location = new int[2];
        this.getLocationOnScreen(location);

        //控件相对于屏幕的坐标
        int x = location[0];
        int y = location[1];

        return (eventRawX >= x && eventRawX <= x + getWidth()) && (eventRawY >= y && eventRawY <= y + getHeight());
    }

    /**
     * 重置状态
     */
    public void resetOnPressState() {
        if (isOnPress) {
            isOnPress = false;
            invalidate();
        }
    }


}
