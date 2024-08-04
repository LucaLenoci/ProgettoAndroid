package com.example.progetto_mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

    private Paint paint;
    private float centerX, centerY;
    private float radius = 100;
    private float joystickX, joystickY;
    private float joystickRadius = 50;
    private JoystickListener joystickListener;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        // Draw the joystick background
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Draw the joystick itself
        paint.setColor(Color.LTGRAY);
        canvas.drawCircle(joystickX + centerX, joystickY + centerY, joystickRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - centerX;
        float y = event.getY() - centerY;
        double distance = Math.sqrt(x * x + y * y);

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (distance < radius) {
                joystickX = x;
                joystickY = y;
            } else {
                joystickX = (float) (radius * x / distance);
                joystickY = (float) (radius * y / distance);
            }
            if (joystickListener != null) {
                joystickListener.onJoystickMoved(joystickX / radius, joystickY / radius);
            }
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            joystickX = 0;
            joystickY = 0;
            if (joystickListener != null) {
                joystickListener.onJoystickMoved(0, 0);
            }
            invalidate();
        }
        return true;
    }

    public interface JoystickListener {
        void onJoystickMoved(float x, float y);
    }

    public void setJoystickListener(JoystickListener listener) {
        this.joystickListener = listener;
    }
}