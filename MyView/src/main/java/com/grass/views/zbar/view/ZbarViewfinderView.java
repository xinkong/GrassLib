package com.grass.views.zbar.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.ResultPoint;
import com.grass.views.R;
import com.grass.views.utils.DisplayUtil;

import java.util.Collection;
import java.util.HashSet;


/**
 * Created by fengwei on 2017-06-04.
 */

public class ZbarViewfinderView extends View {
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;
    private Rect frame=new Rect();
    private int measureedWidth;
    private int measureedHeight;
    private int lineType;
    private boolean ismargen;
    public ZbarViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);

        scanLight = BitmapFactory.decodeResource(resources,
                R.drawable.scan_light);

        initInnerRect(context, attrs);
    }

    /**
     * 初始化内部框的大小
     * @param context
     * @param attrs
     */
    private void initInnerRect(Context context, AttributeSet attrs) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        measureedWidth=display.getWidth();
        measureedHeight=display.getHeight();
        Point screenResolution=new Point(measureedWidth,measureedHeight);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.innerrects);

        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.innerrects_inner_margintop, -1);
        if (innerMarginTop != -1) {
//            CameraManager.FRAME_MARGINTOP = (int) innerMarginTop;
        }

        // 扫描框的宽度
        int width = (int) ta.getDimension(R.styleable.innerrects_inner_width, DisplayUtil.screenWidthPx / 2);

        // 扫描框的高度
        int height= (int) ta.getDimension(R.styleable.innerrects_inner_height, DisplayUtil.screenWidthPx / 2);

        // 扫描框边角颜色
        innercornercolor = ta.getColor(R.styleable.innerrects_inner_corner_color, Color.parseColor("#45DDDD"));
        // 扫描框边角长度
        innercornerlength = (int) ta.getDimension(R.styleable.innerrects_inner_corner_length, 65);
        // 扫描框边角宽度
        innercornerwidth = (int) ta.getDimension(R.styleable.innerrects_inner_corner_width, 15);
       //扫描框距离左边的距离
        int leftOffset = (screenResolution.x - width) / 2;
        //扫描框的整个位置
        frame.set(leftOffset,(int)innerMarginTop,leftOffset+width,(int)(innerMarginTop+height));
        // 扫描bitmap
        Drawable drawable = ta.getDrawable(R.styleable.innerrects_inner_scan_bitmap);
        if (drawable != null) {
        }
        //扫描线的类型 0 固定中间位置  1 移动扫描线  2 移动扫描线图片
        lineType= ta.getInt(R.styleable.innerrects_inner_scan_line_type, 0);
        // 扫描控件
        scanLight = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.innerrects_inner_scan_bitmap, R.drawable.scan_light));
        // 扫描速度
        SCAN_VELOCITY = ta.getInt(R.styleable.innerrects_inner_scan_speed, 5);

        isCircle = ta.getBoolean(R.styleable.innerrects_inner_scan_iscircle, true);
        ismargen= ta.getBoolean(R.styleable.innerrects_inner_scan_ismargen, false);
        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
//        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {

            drawFrameBounds(canvas, frame);

            drawScanLight(canvas, frame);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentPossible) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                    }
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentLast) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                    }
                }
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    // 扫描线移动的y
    private int scanLineTop;
    // 扫描线移动速度
    private int SCAN_VELOCITY;
    // 扫描线
    private Bitmap scanLight;
    // 是否展示小圆点
    private boolean isCircle;

    /**
     * 绘制移动扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawScanLight(Canvas canvas, Rect frame) {

        if (scanLineTop == 0) {
            scanLineTop = frame.top;
        }

        if (scanLineTop >= frame.bottom - 30) {
            scanLineTop = frame.top;
        } else {
            scanLineTop += SCAN_VELOCITY;
        }
        Rect scanRect = new Rect(frame.left, scanLineTop, frame.right,
                scanLineTop + 30);
        paint.setStrokeWidth((float) 5.0);

        switch (lineType){
            case 0:
                canvas.drawLine((float) frame.left+120,(float)(frame.top+(frame.bottom-frame.top)/2) ,(float)frame.right-120,(float)(frame.top+(frame.bottom-frame.top)/2),paint);
                break;
            case 1:
                canvas.drawLine((float) frame.left+120,(float)scanLineTop ,(float)frame.right-120,(float)scanLineTop,paint);
                break;
            case 2:
                canvas.drawBitmap(scanLight, null, scanRect, paint);
                break;
        }

//        if(ismargen){
//            canvas.drawLine((float) frame.left+120,(float)scanLineTop ,(float)frame.right-120,(float)scanLineTop,paint);
//        }else{
//            canvas.drawLine((float) frame.left+120,(float)(frame.top+(frame.bottom-frame.top)/2) ,(float)frame.right-120,(float)(frame.top+(frame.bottom-frame.top)/2),paint);
//        }
        //动态扫描线
//        canvas.drawLine((float) frame.left+120,(float)scanLineTop ,(float)frame.right-120,(float)scanLineTop,paint);
        //扫描线固定在中间位置
//        canvas.drawLine((float) frame.left+120,(float)(frame.top+(frame.bottom-frame.top)/2) ,(float)frame.right-120,(float)(frame.top+(frame.bottom-frame.top)/2),paint);
//        canvas.drawBitmap(scanLight, null, scanRect, paint);
    }


    // 扫描框边角颜色
    private int innercornercolor;
    // 扫描框边角长度
    private int innercornerlength;
    // 扫描框边角宽度
    private int innercornerwidth;

    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {

        /*paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(frame, paint);*/

        paint.setColor(innercornercolor);
        paint.setStyle(Paint.Style.FILL);

        int corWidth = innercornerwidth;
        int corLength = innercornerlength;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, paint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, paint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, paint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, paint);
    }

    public Rect getScanImageRect(int w, int h)
    {
        Rect rect = new Rect();
        Point screenResolution=new Point(measureedWidth,measureedHeight);
        float tempw = w / (float) screenResolution.x;
//        float tempw =1;
        rect.left = (int)(frame.left*tempw);
        rect.right = (int)(frame.right*tempw);

        float temp = h / (float) screenResolution.y;
//        float temp =1;
        rect.top = (int) (frame.top * temp);
        rect.bottom = (int) (frame.bottom * temp);

        return rect;
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }



}
