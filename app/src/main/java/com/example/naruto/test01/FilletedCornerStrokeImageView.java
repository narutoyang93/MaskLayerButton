package com.example.naruto.test01;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/9/8 0008
 * @Note
 */
public class FilletedCornerStrokeImageView extends android.support.v7.widget.AppCompatImageView {
    private final static int DEFAULT_STROKE_WIDTH = 1;//单位：dp
    private final static int DEFAULT_STROKE_DASH_SIZE = 3;//默认虚线间隔，单位：dp
    private final static int STROKE_TYPE_SOLID = 0;//实线
    private final static int STROKE_TYPE_DASH = 1;//虚线
    private float strokeWidth;//描边画笔宽度//单位：px
    private int strokeColor;//描边颜色
    private int radius;//圆角半径
    private int strokeType;//描边类型
    private int strokeDashSize;//虚线间隔
    private float constraintRadiusWithWidth_percent;//圆角半径相对于控件宽度的比例
    private float constraintRadiusWithHeight_percent;//圆角半径相对于控件高度的比例
    private Context context;
    float width, height;

    public FilletedCornerStrokeImageView(Context context) {
        super(context);
        this.context = context;
    }

    public FilletedCornerStrokeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public FilletedCornerStrokeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void init(AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FilletedCornerStrokeImageView);
        //从TypedArray中取出对应的值来为要设置的属性赋值
        radius = ta.getDimensionPixelSize(R.styleable.FilletedCornerStrokeImageView_radius, 0);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.FilletedCornerStrokeImageView_strokeWidth, dip2px(DEFAULT_STROKE_WIDTH));
        strokeColor = ta.getColor(R.styleable.FilletedCornerStrokeImageView_strokeColor, -1);
        strokeType = ta.getInt(R.styleable.FilletedCornerStrokeImageView_strokeType, STROKE_TYPE_SOLID);
        strokeDashSize = ta.getDimensionPixelSize(R.styleable.FilletedCornerStrokeImageView_strokeDashSize, dip2px(DEFAULT_STROKE_DASH_SIZE));
        constraintRadiusWithWidth_percent = ta.getFloat(R.styleable.MaskLayerButton_constraintRadiusWithWidth_percent, 0);
        constraintRadiusWithHeight_percent = ta.getFloat(R.styleable.MaskLayerButton_constraintRadiusWithHeight_percent, 0);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        if (radius == 0) {
            if (constraintRadiusWithWidth_percent > 0) {
                setRadiusByPercent(true);
            } else if (constraintRadiusWithHeight_percent > 0) {
                setRadiusByPercent(false);
            }
        }

        if (width > radius && height > radius) {
            Path path = new Path();
            path.moveTo(radius, 0);
            path.lineTo(width - radius, 0);
            path.quadTo(width, 0, width, radius);
            path.lineTo(width, height - radius);
            path.quadTo(width, height, width - radius, height);
            path.lineTo(radius, height);
            path.quadTo(0, height, 0, height - radius);
            path.lineTo(0, radius);
            path.quadTo(0, 0, radius, 0);
            canvas.clipPath(path);
        }
        super.onDraw(canvas);

/*        Drawable background = getBackground();
        if (radius > 0 && background != null) {//如果圆角半径>0且背景不为空，需要绘制圆角背景
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
        }*/

        if (strokeColor != -1) {//描边
            drawStroke(paint, canvas);
        }
    }

    /**
     * 根据比例设置圆角半径
     *
     * @param isByWidth
     */
    private void setRadiusByPercent(boolean isByWidth) {
        int base = 0;
        float percent = 0;
        if (isByWidth) {
            base = getWidth();
            percent = constraintRadiusWithWidth_percent;
        } else {
            base = getHeight();
            percent = constraintRadiusWithHeight_percent;
        }
        if (percent > 0) {
            if (percent > 1) {
                percent = 1;
            }
            radius = (int) (base * percent);
        }
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

    /**
     * 绘制遮罩层或描边
     *
     * @param paint
     * @param canvas
     */
    private void drawStroke(Paint paint, Canvas canvas) {
        paint.reset();
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeJoin(Paint.Join.ROUND);

        RectF rf = new RectF(canvas.getClipBounds());
        float padding = strokeWidth / 2;
        rf.bottom -= padding;
        rf.right -= padding;
        rf.top += padding;
        rf.left += padding;
        canvas.drawRoundRect(rf, radius, radius, paint);
        if (strokeType == STROKE_TYPE_DASH) {
            paint.setPathEffect(new DashPathEffect(new float[]{strokeDashSize, strokeDashSize}, 0));
        }
        if (radius > 0) {//圆角矩形
            paint.setAntiAlias(true);
        } else {//矩形
            canvas.drawRect(canvas.getClipBounds(), paint);
        }
    }
}
