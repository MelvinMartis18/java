package com.application.essentials;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class SlideButton extends android.support.v7.widget.AppCompatSeekBar {

    private Drawable thumb;
    private SlideButtonListener listener;
    Paint paint = new Paint();

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String textToDraw = "Slide to Dismiss";
        paint.setColor(Color.parseColor("#d0d0d0"));
        paint.setTextSize(60f);
        paint.setAntiAlias(true);
//        paint.setShadowLayer(26f, 30, 30, Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        float textMeasure = paint.measureText(textToDraw);
        Rect bounds = new Rect();
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);

        Drawable dr = ResourcesCompat.getDrawable(getResources(), R.drawable.btn_thumb_slide, null);
        int thumbWidth = dr.getIntrinsicWidth();
        canvas.drawText(textToDraw, canvas.getWidth()/2f - bounds.width()/2 + thumbWidth/2, canvas.getHeight()/2 + bounds.height()/2, paint);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                super.onTouchEvent(event);
            } else
                return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getProgress() > 75) {
                setProgress(90);
                handleSlide();
            }
            else {
                setProgress(10);
            }
        } else {
            super.onTouchEvent(event);
        }

        return true;
    }


    private void handleSlide() {
        listener.handleSlide();
    }

    public void setSlideButtonListener(SlideButtonListener listener) {
        this.listener = listener;
    }
}