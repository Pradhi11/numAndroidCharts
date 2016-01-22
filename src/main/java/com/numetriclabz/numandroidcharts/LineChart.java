package com.numetriclabz.numandroidcharts;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChart extends View {

    private Paint paint;
    private List<ChartData> values;
    private List<Float> horizontal_width_list = new ArrayList<>();
    private String description;
    private float border = 30, horstart = border * 2, circleSize = 5f, colwidth;
    private int parentHeight, parentWidth;
    private static final int INVALID_POINTER_ID = -1;
    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private Boolean gesture = false;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private Canvas canvas;
    private List<ChartData> list_cordinate = new ArrayList<>();
    private float x_cordinate, y_cordinate, height, width, maxY_values, maxX_values, graphheight, graphwidth;
    private List<ChartData> trendlines;
    private List<ChartData> trendzones;
    private AxisFormatter axisFormatter = new AxisFormatter();
    private Boolean stepline = false;
    private Boolean stepArea = false;

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        Paint paint = new Paint();
        this.paint = paint;
    }

    public void setData(List<ChartData> values) {

        if (values != null)
            this.values = values;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStepline(Boolean stepline) {
        this.stepline = stepline;
    }

    public void setStepArea(Boolean stepArea) {
        this.stepArea = stepArea;
    }

    public void setGesture(Boolean gesture) {
        this.gesture = gesture;
    }

    public void setCircleSize(Float circleSize) {
        this.circleSize = circleSize;
    }

    public void setTrendZones(List<ChartData> trendzones) {
        this.trendzones = trendzones;
    }

    public void setTrendlines(List<ChartData> trendlines) {
        this.trendlines = trendlines;
    }

    // Get the Width and Height defined in the activity xml file
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {

        intilaizeValue(canvas);

        if (gesture == true) {

            CanvasScaleFator();
        }


        axisFormatter.PlotXYLabels(graphheight, width, graphwidth, height, null, maxY_values, canvas,
                horizontal_width_list, paint, values, maxX_values, description);

        if (values != null) {

            colwidth = horizontal_width_list.get(1) - horizontal_width_list.get(0);

            list_cordinate = StoredCordinate(graphheight);

            if (trendzones != null) {
                ChartHelper chartHelper1 = new ChartHelper(trendzones, canvas, paint);
                chartHelper1.DrawTrendzone(values.size(), colwidth, graphheight, maxY_values);
            }

            if (trendlines != null) {
                ChartHelper chartHelper2 = new ChartHelper(trendlines, canvas, paint);
                chartHelper2.DrawTrendlines(graphheight, maxY_values, graphwidth);
            }

            paint.setColor(Color.parseColor(axisFormatter.getColorList().get(0)));
            paint.setStrokeWidth(2);
            if(stepline == true){
                DrawStepLines();

            }
            else if(stepArea == true){
                DrawStepArea();

            }
            else {
                DrawLines();
            }

            paint.setStrokeWidth(0);
            DrawCircle();
            DrawText();

            if (gesture == true) {
                canvas.restore();
            }
        }
    }

    private void CanvasScaleFator() {

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
    }

    private void intilaizeValue(Canvas canvas) {

        height = parentHeight - 60;
        width = parentWidth;
        AxisFormatter axisFormatter = new AxisFormatter();
        maxY_values = axisFormatter.getMaxY_Values(values);

        if (values.get(0).getLabels() == null)
            maxX_values = axisFormatter.getMaxX_Values(values);

        graphheight = height - (3 * border);
        graphwidth = width - (3 * border);
        this.canvas = canvas;
    }

    private void DrawCircle() {

        for (int i = 0; i < list_cordinate.size(); i++) {

            canvas.drawCircle(list_cordinate.get(i).getX_values(),
                    list_cordinate.get(i).getY_values(),
                    circleSize, paint);
        }
    }

    private void DrawText() {
        for (int i = 0; i < values.size(); i++) {
            canvas.drawText(values.get(i).getY_values() + "",
                    list_cordinate.get(i).getX_values() - border - circleSize,
                    list_cordinate.get(i).getY_values() - 10, paint);
        }
    }

    private List<ChartData> StoredCordinate(Float graphheight) {

        for (int i = 0; i < values.size(); i++) {

            float line_height = (graphheight / maxY_values) * values.get(i).getY_values();
            y_cordinate = (border - line_height) + graphheight;

            if (values.get(0).getLabels() == null) {

                float x_ratio = (maxX_values / (values.size() - 1));
                x_cordinate = (colwidth / x_ratio) * values.get(i).getX_values();

                list_cordinate.add(new ChartData(y_cordinate, x_cordinate + horstart));

            } else {

                x_cordinate = (i * colwidth) + horstart;
                list_cordinate.add(new ChartData(y_cordinate, x_cordinate + horstart));

            }
        }

        return list_cordinate;
    }

    private void DrawLines() {

        for (int i = 0; i < list_cordinate.size() - 1; i++) {

            canvas.drawLine(list_cordinate.get(i).getX_values(),
                    list_cordinate.get(i).getY_values(),
                    list_cordinate.get(i + 1).getX_values(),
                    list_cordinate.get(i + 1).getY_values(), paint);

        }
    }

    private void DrawStepLines() {

        for (int i = 0; i < list_cordinate.size() - 1; i++) {

            canvas.drawLine(list_cordinate.get(i).getX_values(),
                    list_cordinate.get(i).getY_values(),
                    list_cordinate.get(i).getX_values(),
                    list_cordinate.get(i + 1).getY_values(), paint);

            canvas.drawLine(list_cordinate.get(i).getX_values(),
                    list_cordinate.get(i + 1).getY_values(),
                    list_cordinate.get(i + 1).getX_values(),
                    list_cordinate.get(i + 1).getY_values(),
                    paint);

        }
    }


    private void DrawStepArea() {

        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        Path path = new Path();
        path.reset();

        path.moveTo(
                list_cordinate.get(0).getX_values(),
                graphheight + 30);


        int listSize = list_cordinate.size();

        for (int j = 0; j < listSize; j++) {

            paint.setColor(Color.parseColor(axisFormatter.getColorList().get(0)));
            paint.setAlpha(100);

            if (j != 0 && j < listSize){
                path.lineTo(list_cordinate.get(j).getX_values(),
                        list_cordinate.get(j-1).getY_values());
            }

            path.lineTo(
                    list_cordinate.get(j).getX_values(),
                    list_cordinate.get(j).getY_values());


        }


        path.lineTo(
                list_cordinate.get(listSize - 1).getX_values(),
                graphheight + 30);



        canvas.drawPath(path, paint);
        paint.setAlpha(1000);
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(.1f, Math.min(mScaleFactor, 10.0f));

            invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

}
