package com.example.progetto_mobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

public class HomeBambinoActivity extends AppCompatActivity {

    private ImageView movableObject;
    private float centerX, centerY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esercizi_giornalieri_bambino);

        JoystickView joystick = findViewById(R.id.joystick);
        movableObject = findViewById(R.id.movable_object);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) movableObject.getLayoutParams();
        centerX = params.leftMargin;
        centerY = params.topMargin;

        joystick.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float x, float y) {
                moveObject(x, y);
            }
        });
    }

    private void moveObject(float x, float y) {
        float deltaX = x * 100; // Adjust the multiplier to control speed
        float deltaY = y * 100; // Adjust the multiplier to control speed

        movableObject.setX(centerX + deltaX);
        movableObject.setY(centerY + deltaY);
    }
}
