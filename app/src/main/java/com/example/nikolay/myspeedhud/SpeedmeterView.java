package com.example.nikolay.myspeedhud;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Locale;

public class SpeedmeterView extends View {

    private Paint needlePaint;
    private Path needlePath;
    private Paint needleScrewPaint;

    private float canvasCenterX;
    private float canvasCenterY;
    private float canvasWidth;
    private float canvasHeight;
    private float needleTailLength;
    private float needleWidth;
    private float needleLength;
    private RectF rimRect;
    private Paint rimPaint;
    private Paint rimCirclePaint;
    private RectF faceRect;
    private Paint facePaint;
    private Paint scalePaint;
    private RectF scaleRect;

    private int nicksCount = 120;
    private float degreesPerNick = nicksCount / 360;
    private float unitsPerNIck = 5;
    private float minValue = 0;
    private float maxValue = 240;
    private boolean intScale = true;
    private float initialValue = 0;
    private float value = 0;
    private float needleValue = 0;

    private float needleStep;

    private float centerValue;
    private float labelRadius;

    private int longNickInterval = 10;

    private int updateTimeInterval = 5;
    private float needleStepFactor = 3f;

    private Paint labelPaint;
    private long lastMoveTime;
    private int faceColor;
    private int scaleColor;
    private int needleColor;
    private int rimColor;
    private int labelColor;
    private Paint textPaint;

    private String topText = "";
    private String distance = "";
    public static final String distanceFormat = "%1$.2f %2$s";

    private static final String TAG = SpeedmeterView.class.getSimpleName();
    private float labelTextSize;
    private float textSize;

    public SpeedmeterView(Context context) {
        super(context);
        initValues();
        initPaint();
    }

    public SpeedmeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(context, attrs);
        initValues();
        initPaint();
    }

    public SpeedmeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttrs(context, attrs);
        initValues();
        initPaint();
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpeedmeterView, 0, 0);
        nicksCount = a.getInt(R.styleable.SpeedmeterView_nicksCount, nicksCount);
        degreesPerNick = 360.0f / nicksCount;
        unitsPerNIck = a.getFloat(R.styleable.SpeedmeterView_unitsPerNick, unitsPerNIck);
        longNickInterval = a.getInt(R.styleable.SpeedmeterView_longNickInterval, 10);
        minValue = a.getFloat(R.styleable.SpeedmeterView_minValue, minValue);
        maxValue = a.getFloat(R.styleable.SpeedmeterView_maxValue, maxValue);
        intScale = a.getBoolean(R.styleable.SpeedmeterView_intScale, intScale);
        initialValue = a.getFloat(R.styleable.SpeedmeterView_initialValue, initialValue);
        faceColor = a.getColor(R.styleable.SpeedmeterView_faceColor, Color.argb(0xff, 0xff, 0xff, 0xff));
        scaleColor = a.getColor(R.styleable.SpeedmeterView_scaleColor, 0x9f004d0f);
        needleColor = a.getColor(R.styleable.SpeedmeterView_needleColor, Color.RED);
        rimColor = a.getColor(R.styleable.SpeedmeterView_rimColor, Color.argb(0x4f, 0x33, 0x36, 0x33));
        labelColor = a.getColor(R.styleable.SpeedmeterView_labelColor, scaleColor);
        topText = a.getString(R.styleable.SpeedmeterView_topText) == null ? topText : fromHtml(a.getString(R.styleable.SpeedmeterView_topText)).toString();
        labelTextSize = a.getFloat(R.styleable.SpeedmeterView_labelTextSize, 0);
        textSize = a.getFloat(R.styleable.SpeedmeterView_textSize, 0);
        a.recycle();

        validate();
    }

    private void initPaint() {
        setSaveEnabled(true);

        rimPaint = new Paint();
        rimPaint.setAntiAlias(true);
        rimPaint.setColor(rimColor);

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(rimColor);
        rimCirclePaint.setStrokeWidth(0.005f);

        facePaint = new Paint();
        facePaint.setAntiAlias(true);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setColor(faceColor);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setAntiAlias(true);
        scalePaint.setColor(scaleColor);

        labelPaint = new Paint();
        labelPaint.setColor(labelColor);
        labelPaint.setTypeface(Typeface.SANS_SERIF);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint();
        textPaint.setColor(scaleColor);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint();
        needlePaint.setColor(needleColor);
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePaint.setAntiAlias(true);

        needlePath = new Path();

        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
    }

    private void initValues() {
        validate();
        needleStep = needleStepFactor * unitsPerNIck/degreesPerNick;
        centerValue = (minValue + maxValue) / 2;
        needleValue = value = initialValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRim(canvas);
        drawFace(canvas);
        drawScale(canvas);
        drawLabels(canvas);
        drawTexts(canvas);
        canvas.rotate(scaleToCanvasDegrees(valueToDegrees(needleValue)), canvasCenterX, canvasCenterY);
        canvas.drawPath(needlePath, needlePaint);
        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasWidth / 61f, needleScrewPaint);

        if (needsToMove()) {
            invalidate();
            moveNeedle();
        }
    }

    private void moveNeedle() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastMoveTime;

        if (deltaTime >= updateTimeInterval) {
            if (Math.abs(value - needleValue) <= needleStep) {
                needleValue = value;
            } else {
                if (value > needleValue) {
                    needleValue += 2 * unitsPerNIck / degreesPerNick;
                } else {
                    needleValue -= 2 * unitsPerNIck / degreesPerNick;
                }
            }
            lastMoveTime = System.currentTimeMillis();
        }
    }

    private void drawTexts(Canvas canvas) {
        drawTextCentered(topText, canvasCenterX, canvasCenterY - (canvasHeight / 6.5f), textPaint, canvas);
        drawTextCentered(distance, canvasCenterX, canvasCenterY + (canvasHeight / 6.5f), textPaint, canvas);
    }

    private void drawLabels(Canvas canvas) {
        for (int i = 0; i < nicksCount; i += longNickInterval) {
            float value = nicksToUnits(i);
            if (value >= minValue && value <= maxValue) {
                float scaleAngle = i * degreesPerNick;
                float scaleAngleRads = (float) Math.toRadians(scaleAngle);
                Log.d(TAG, "i = " + i + ", angle = " + scaleAngle + ", value = " + value);
                float deltaX = labelRadius * (float) Math.sin(scaleAngleRads);
                float deltaY = labelRadius * (float) Math.cos(scaleAngleRads);
                String valueLabel;
                if (intScale) {
                    valueLabel = String.valueOf((int) value);
                } else {
                    valueLabel = String.valueOf(value);
                }
                drawTextCentered(valueLabel, canvasCenterX + deltaX, canvasCenterY - deltaY, labelPaint, canvas);
            }
        }
    }

    private void drawTextCentered(String text, float x, float y, Paint paint, Canvas canvas) {
        //TODO uncomment
        //float yPos = (y - ((paint.descent() + paint.ascent()) / 2f));
        canvas.drawText(text, x, y, paint);
    }

    private void drawScale(Canvas canvas) {
        canvas.save();
        for (int i = 0; i < nicksCount; ++i) {
            float y1 = scaleRect.top;
            float y2 = y1 + (0.020f * canvasHeight);
            float y3 = y1 + (0.060f * canvasHeight);
            float y4 = y1 + (0.030f * canvasHeight);

            float value = nicksToUnits(i);

            if (value >= minValue && value <= maxValue) {
                canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y2, scalePaint);

                if (i % longNickInterval == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y3, scalePaint);
                }

                if (i % (longNickInterval / 2) == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y4, scalePaint);
                }
            }

            canvas.rotate(degreesPerNick, 0.5f * canvasWidth, 0.5f * canvasHeight);
            Log.d(TAG, value + " ");
        }
        canvas.restore();
    }

    private float nicksToUnits(int i) {
        float rawValue = ((i < nicksCount / 2) ? i : (i - nicksCount)) * unitsPerNIck;
        return rawValue + centerValue;
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(faceRect, facePaint);
        canvas.drawOval(faceRect, rimCirclePaint);
    }

    private void drawRim(Canvas canvas) {
        canvas.drawOval(rimRect, rimPaint);
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasWidth = (float) w;
        canvasHeight = (float) h;
        canvasCenterX = w / 2f;
        canvasCenterY = h / 2f;
        needleTailLength = canvasWidth / 12f;
        needleWidth = canvasWidth / 98f;
        needleLength = (canvasWidth / 2f) * 0.8f;

        needlePaint.setStrokeWidth(canvasWidth / 197f);
        setNeedle();

        rimRect = new RectF(canvasWidth * .05f, canvasHeight * .05f, canvasWidth * 0.95f, canvasHeight * 0.95f);

        float rimSize = 0.02f * canvasWidth;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        scalePaint.setStrokeWidth(0.005f * canvasWidth);
        scalePaint.setTextSize(0.045f * canvasWidth);
        scalePaint.setTextScaleX(0.8f * canvasWidth);

        float scalePosition = 0.015f * canvasWidth;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);

        labelRadius = (canvasCenterX - scaleRect.left) * 0.70f;

        if (labelTextSize > 0) {
            labelPaint.setTextSize(labelTextSize);
        } else {
            labelPaint.setTextSize(w / 18f);
        }

        if (textSize > 0) {
            textPaint.setTextSize(textSize);
        } else {
            textPaint.setTextSize(w / 14f);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void setNeedle() {
        needlePath.reset();
        needlePath.moveTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY - (needleWidth / 2));
        needlePath.lineTo(canvasCenterX + needleLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY + (needleWidth / 2));
        needlePath.lineTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.addCircle(canvasCenterX, canvasCenterY, canvasWidth / 49f, Path.Direction.CW);
        needlePath.close();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        //Must call this
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat("value", value);
        bundle.putFloat("needleValue", needleValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            value = bundle.getFloat("value");
            needleValue = bundle.getFloat("needleValue");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private float valueToDegrees(float value) {
        // these are scaled degrees, 0 is on top-middle
        return ((value - centerValue) / unitsPerNIck) * degreesPerNick;
    }

    private float scaleToCanvasDegrees(float degrees) {
        return degrees - 90;
    }

    private boolean needsToMove() {
        return Math.abs(needleValue - value) > 0;
    }

    private void validate() {
        if (nicksCount % longNickInterval != 0) {
            Log.w(TAG, getResources().getString(R.string.invalid_number_of_nicks, nicksCount, longNickInterval));
        }
        float sum = minValue + maxValue;
        int intSum = Math.round(sum);
        if ((maxValue >=1 && (sum != intSum || (intSum & 1) != 0)) || minValue >= maxValue) {
            Log.w(TAG, getResources().getString(R.string.invalid_min_max_ratio, minValue, maxValue));
        }
        if (Math.round(sum % unitsPerNIck) != 0) {
            Log.w(TAG, getResources().getString(R.string.invalid_min_max, minValue, maxValue, unitsPerNIck));
        }
    }

    private static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    //PUBLIC METHODS

    public void setdistance(double distance, String units){
        this.distance = String.format(Locale.ENGLISH, distanceFormat , distance, units);
        invalidate();
    }

    public void setUpdateTimeInterval(int interval) {
        invalidate();
        updateTimeInterval = interval;
    }

    public void moveToValue(float value) {
        if(value >= maxValue){
            this.value = maxValue;
        } else if (value <= minValue){
            this.value = minValue;
        } else {
            this.value = value;
        }
        invalidate();
    }


    public void setNeedleStepFactor(float factor) {
        needleStepFactor = factor;
    }


    public void setValue(float value) {
        if(value >= maxValue){
            value = maxValue;
        } else if (value <= minValue){
            value = minValue;
        }
        invalidate();
        needleValue = this.value = value;
    }


}
