package com.example.c17snake;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends Activity {

    // Declare an instance of SnakeGame
    private SnakeGame mSnakeGame;

    // Set up the game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the display object to obtain screen dimensions
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object to store screen size
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeGame class passing the context and screen size
        mSnakeGame = new SnakeGame(this, size);

        // Set SnakeGame as the content view of the activity
        setContentView(mSnakeGame);
    }

    // Resume the game when the activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();
    }

    // Pause the game when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();
    }
}
