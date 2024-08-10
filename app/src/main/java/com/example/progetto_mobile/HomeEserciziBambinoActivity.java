package com.example.progetto_mobile;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class HomeEserciziBambinoActivity extends AppCompatActivity {

    private ImageView movableObject;
    private float centerX, centerY;
    private List<Button> buttons;  // List to hold all the buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esercizi_giornalieri_bambino);

        JoystickView joystick = findViewById(R.id.joystick);
        movableObject = findViewById(R.id.movable_object);

        // Initialize the list of buttons and add your buttons to the list
        buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.button1));  // Add your buttons by their IDs
        buttons.add(findViewById(R.id.button2));
        buttons.add(findViewById(R.id.button3));
        buttons.add(findViewById(R.id.button4));

        // Set onClickListeners for each button to navigate to a different view
        for (int i = 0; i < buttons.size(); i++) {
            final int buttonNumber = i + 1; // Button numbers are 1-based
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToView(buttonNumber);
                }
            });
        }

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
        float deltaX = x * 10; // Adjust the multiplier to control speed
        float deltaY = y * 10; // Adjust the multiplier to control speed

        // Calculate the new position
        float newX = movableObject.getX() + deltaX;
        float newY = movableObject.getY() + deltaY;

        // Get the parent layout dimensions
        RelativeLayout parentLayout = (RelativeLayout) movableObject.getParent();
        int parentWidth = parentLayout.getWidth();
        int parentHeight = parentLayout.getHeight();

        // Ensure the object stays within the bounds of the parent layout
        newX = Math.max(0, Math.min(newX, parentWidth - movableObject.getWidth()));
        newY = Math.max(0, Math.min(newY, parentHeight - movableObject.getHeight()));

        // Move the object
        movableObject.setX(newX);
        movableObject.setY(newY);

        // Check for collisions with each button
        for (Button button : buttons) {
            if (isColliding(movableObject, button)) {
                button.performClick();  // Trigger button click if colliding
            }
        }
    }

    private boolean isColliding(View object1, View object2) {
        // Get the locations of both objects on the screen
        int[] location1 = new int[2];
        int[] location2 = new int[2];

        object1.getLocationOnScreen(location1);
        object2.getLocationOnScreen(location2);

        // Get the actual width and height of both objects
        int object1Width = object1.getWidth();
        int object1Height = object1.getHeight();
        int object2Width = object2.getWidth();
        int object2Height = object2.getHeight();

        // Calculate the top-left and bottom-right corners of the bounding boxes
        int x1 = location1[0] + object1Width / 4; // Add an offset to shrink the bounding box
        int y1 = location1[1] + object1Height / 4;
        int object1Right = x1 + (object1Width / 2); // Shrink width by half
        int object1Bottom = y1 + (object1Height / 2); // Shrink height by half

        int x2 = location2[0] + object2Width / 4; // Add an offset to shrink the bounding box
        int y2 = location2[1] + object2Height / 4;
        int object2Right = x2 + (object2Width / 2); // Shrink width by half
        int object2Bottom = y2 + (object2Height / 2); // Shrink height by half

        // Check if the bounding boxes overlap
        return x1 < object2Right &&
                object1Right > x2 &&
                y1 < object2Bottom &&
                object1Bottom > y2;
    }



    private void navigateToView(int buttonNumber) {
        Intent intent;
        switch (buttonNumber) {
            case 1:
                intent = new Intent(HomeEserciziBambinoActivity.this, MainActivity.class);
                break;
            case 2:
                intent = new Intent(HomeEserciziBambinoActivity.this, DenominazioneImmaginiActivity.class);
                break;
            case 3:
                intent = new Intent(HomeEserciziBambinoActivity.this, MainActivity.class);
                break;
            case 4:
                intent = new Intent(HomeEserciziBambinoActivity.this, LoginActivity.class);
                break;
            default:
                return; // No valid button number
        }
        startActivity(intent);
    }




}
