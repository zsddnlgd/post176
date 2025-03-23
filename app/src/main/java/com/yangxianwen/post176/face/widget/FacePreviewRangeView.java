package com.yangxianwen.post176.face.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 触发人脸识别区域视图
 */
public class FacePreviewRangeView extends View {

    private final Paint paint;

    //模糊区域颜色
    private final int maskColor;

    //虚线颜色
    private final int lineColor;

    private Rect frame;

    public int scannerStart = 0;
    public int scannerEnd = 0;

    public FacePreviewRangeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        maskColor = 0x60000000;
        lineColor = 0xFFFFFFFF;

        paint = new Paint();
        paint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (frame == null) {
            int left = getWidth() / 4;
            int top = getHeight() / 4;
            int right = left + (getWidth() / 2);
            int bottom = top + (getHeight() / 2);
            frame = new Rect(left, top, right, bottom);
        }

        if (scannerStart == 0 || scannerEnd == 0) {
            scannerStart = frame.top;
            scannerEnd = frame.bottom;
        }

        //绘制模糊区域
        drawExterior(canvas, frame, getWidth(), getHeight());
        //绘制框选区域
        drawCorner(canvas, frame);
    }

    /**
     * 绘制框选区域
     */
    private void drawCorner(Canvas canvas, Rect frame) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(lineColor);
        paint.setStrokeWidth(2);
        canvas.drawRect(frame.left, frame.top, frame.right, frame.bottom, paint);
    }

    /**
     * 绘制模糊区域
     */
    private void drawExterior(Canvas canvas, Rect frame, int width, int height) {
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);
    }
}
