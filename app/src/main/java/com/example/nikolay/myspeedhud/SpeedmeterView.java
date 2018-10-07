package com.example.nikolay.myspeedhud;

/*
* Big thanks to danon
* for code sample
 */

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class SpeedmeterView extends View {

    private static final String TAG = SpeedmeterView.class.getSimpleName();

    //параметры спидометра
    public static final double MAX_SPEED = 100.0;
    public static final double MAJOR_TICK_STEP = 20.0;//расстояние между большими делениями шкалы
    public static final int MINOR_TICKS = 3;//количество малых делений между большими
    public static final int LABEL_TEXT_SIZE_DP = 12;
    public static final int UNITS_TEXT_SIZE_DP = 24;

    //прочие поля
    private double speed = 0;
    private int defaultColor = Color.rgb(180, 180, 180);
    private LabelConverter labelConverter;
    private String unitsText = "km/h";

    //Цвета
    private Paint backgroundPaint;
    private Paint backgroundInnerPaint;
    private Paint maskPaint;
    private Paint needlePaint;
    private Paint ticksPaint;
    private Paint txtPaint;
    private Paint unitsPaint;
    private Paint colorLinePaint;

    private Bitmap mask;


    public SpeedmeterView(Context context) {
        super(context);
        init();
    }

    public SpeedmeterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setSpeed(double speed){
        if(speed > MAX_SPEED){
            speed = MAX_SPEED;
        }

        this.speed = speed;
        invalidate();
    }

    //апи не ниже 11 уровня
    @TargetApi(11)//TODO разобрать с тем, что это такое
    public ValueAnimator setSpeed(double progress, long duration, long startDelay){

        if(progress > MAX_SPEED){
            progress = MAX_SPEED;
        }

        //TODO может работать некорректно
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float v, Double aDouble, Double t1) {
                return aDouble + v * (aDouble - t1);
            }
        }, speed, progress);

        valueAnimator.setDuration(duration);
        valueAnimator.setStartDelay(startDelay);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Double value = (Double) valueAnimator.getAnimatedValue();
                if(value != null){
                    setSpeed(value);
                }
            }
        });

        valueAnimator.start();
        return valueAnimator;
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, boolean animated){
        return setSpeed(progress, 500, 200);
    }

    public void setLabelConverter(LabelConverter labelConverter){
        this.labelConverter = labelConverter;
        invalidate();
    }

    public LabelConverter getLabelConverter(){
        return labelConverter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        drawBackGround(canvas);

        drawTicks(canvas);

        drawNeedle(canvas);

    }

    @Override//TODO Что это за метод
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //measure Width
        if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST){
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure height
        if(heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST){
            height = heightSize;
        } else {
            height = -1;
        }

        if(height >= 0 && width >= 0){
            width = Math.min(width, height);
            height = width/2;
        } else if(width >= 0){
            height = width/2;
        } else if(height >= 0) {
            width = height*2;
        } else {
            width = 0;
            height = 0;
        }

        setMeasuredDimension(width, height);

    }

    private void drawNeedle(Canvas canvas){
        RectF oval = getOval(canvas, 1);
        float radiis = oval.width()*0.35f + 10;
        RectF smallOval = getOval(canvas, 0.2f);

        float angle = 10 + (float)(speed/MAX_SPEED*160);
        canvas.drawLine(//TODO разобраться в математике
                (float) (oval.centerX() + Math.cos((180 - angle)/180*Math.PI)*smallOval.width()*0.5f),
                (float) (oval.centerY() - Math.sin(angle/180*Math.PI)*smallOval.width()*0.5f),
                (float) (oval.centerX() + Math.cos((180 - angle)/180*Math.PI)*radiis),
                (float) (oval.centerY() - Math.sin(angle/180*Math.PI)*radiis),
                needlePaint);
        canvas.drawArc(smallOval, 180, 180, true, backgroundPaint);
    }

    private void drawTicks(Canvas canvas) {
        float availableAngle = 160;
        float majorStep = (float)(MAJOR_TICK_STEP/MAX_SPEED*availableAngle);
        float minorStep = majorStep/(MINOR_TICKS + 1);

        float majorTicksLength = 30;
        float minorTicksLength = majorTicksLength/2;

        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.35f;

        float currentAngle = 10;
        double curProgress = 0;

        while (currentAngle <= 170){
            canvas.drawLine(
                    (float) (oval.centerX() + Math.cos((180 - currentAngle)/180*Math.PI)*(radius - majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle/180*Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerX() + Math.cos((180 - currentAngle)/180*Math.PI)*(radius + majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle/180*Math.PI)*(radius + majorTicksLength/2)),
                    ticksPaint
            );

                    for(int i = 1; i <= MINOR_TICKS; ++i){
                        float angle = currentAngle + i*minorStep;
                        if(angle >= 170 + minorStep/2){
                            break;
                        }

                        canvas.drawLine(
                                (float) (oval.centerX() + Math.cos((180 - angle)/180*Math.PI)*(radius)),
                                (float) (oval.centerY() - Math.sin(angle/180*Math.PI)*radius),
                                (float) (oval.centerX() + Math.cos((180 - angle)/180*Math.PI)*(radius + minorTicksLength)),
                                (float) (oval.centerY() - Math.sin(angle/180*Math.PI)*(radius + minorTicksLength)),
                                ticksPaint
                        );
                    }

                    if(labelConverter != null){
                        canvas.save();
                        canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
                        float tvX = oval.centerX() + radius + majorTicksLength/2 + 8;
                        float tvY = oval.centerY();

                        canvas.rotate(+90, tvX, tvY);
                        canvas.drawText(labelConverter.getLabelFor(curProgress, MAX_SPEED), tvX, tvY, txtPaint);
                        canvas.restore();
                    }

                    currentAngle += majorStep;
                    curProgress += MAJOR_TICK_STEP;

        }

        RectF smallOval = getOval(canvas, 0.7f);
        colorLinePaint.setColor(defaultColor);
        canvas.drawArc(smallOval, 185, 170, false, colorLinePaint);
    }

    //TODO какой нахуй фактор блять????
    private RectF getOval(Canvas canvas, float factor){
        RectF oval;
        final int canvasWidth = canvas.getWidth();
        final int canvasHeight = canvas.getHeight();

        if(canvasHeight*2 >= canvasWidth){
            oval = new RectF(0, 0, canvasWidth*factor, canvasHeight*factor);
        } else {
            oval = new RectF(0, 0, canvasWidth*2*factor, canvasHeight*2*factor);
        }

        oval.offset((canvasWidth - oval.width())/2 + getPaddingLeft(), (canvasHeight*2 - oval.height())/2 + getPaddingTop());
        return oval;
    }

    private void drawBackGround(Canvas canvas){
        RectF oval = getOval(canvas, 1);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);

        RectF innerOval = getOval(canvas, 0.9f);
        canvas.drawArc(innerOval, 180, 180, true, backgroundInnerPaint);

       // Bitmap mask = Bitmap.createScaledBitmap(this.mask, (int) (oval.width()*1.1), (int) (oval.height()*1.1)/2, true);
       // canvas.drawBitmap(mask, oval.centerX() - oval.width()*1.1f/2, oval.centerY() - oval.width()*1.1f/2, maskPaint);

        canvas.drawText(unitsText, oval.centerX(), oval.centerY()/1.5f, unitsPaint);
    }

    @SuppressWarnings("NewApi")
    private void init(){
        if(!isInEditMode()){
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(127, 127, 127));

        backgroundInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundInnerPaint.setStyle(Paint.Style.FILL);
        backgroundInnerPaint.setColor(Color.rgb(127, 127, 127));

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(LABEL_TEXT_SIZE_DP);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setLinearText(true);

        unitsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unitsPaint.setColor(Color.WHITE);
        unitsPaint.setTextSize(UNITS_TEXT_SIZE_DP);
        unitsPaint.setTextAlign(Paint.Align.CENTER);
        unitsPaint.setLinearText(true);

        this.mask = BitmapFactory.decodeResource(getResources(), R.drawable.spot_mask);
        this.mask = Bitmap.createBitmap(this.mask, 0, 0, this.mask.getWidth(), this.mask.getHeight() / 2);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setDither(true);

        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(5);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(Color.argb(200, 255, 0, 0));





    }

    public interface LabelConverter {

        String getLabelFor(double progress, double maxProgress);

    }

}
