package com.example.c17snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


class SnakeGame extends SurfaceView implements Runnable{

    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    private int screenWidth;
    private int screenHeight;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;

    private Typeface typeface = getResources().getFont(R.font.rock_salt);
    private Bitmap mBitmapBackground;

    private Rect pauseBtn = new Rect(991, 50, 1091, 150);

    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);

        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;
        mBitmapBackground = BitmapFactory.decodeResource(getResources(), R.drawable.bgred);
        mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, size.x, size.y, false);
        screenWidth = size.x;
        screenHeight = size.y;

        // Initialize the SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }

        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

    }


    // Called to start a new game
    public void newGame() {

        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);

        // Get the apple ready for dinner
        mApple.spawn();

        // Reset the mScore
        mScore = 0;

        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }


    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    public void update() {

        // Move the snake
        mSnake.move();

        // Did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();

            // Add to  mScore
            mScore = mScore + 1;

            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);

            mPaused = true;
        }

    }


    // Do all the drawing
    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            // Draw the background
            mCanvas.drawBitmap(mBitmapBackground, 0, 0, null);
            mPaint.setTypeface(typeface);

            // Set the size and color of the mPaint for the text
            mPaint.setTextSize(80);
            mPaint.setColor(Color.WHITE);
            // Draw the score
            mCanvas.drawText("" + mScore, 20, 120, mPaint);

            // Correctly position "Eva and Jorge" text towards the right
            float nameTextWidth = mPaint.measureText("Eva and Jorge");
            mCanvas.drawText("Eva and Jorge", screenWidth - nameTextWidth - 20, 120, mPaint);

            // Draw the apple and the snake
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            // Define the pause/resume button characteristics
            mPaint.setTextSize(40); // Set text size for the button text
            String buttonText = mPlaying && !mPaused ? "Pause" : "Resume";
            float buttonTextWidth = mPaint.measureText(buttonText);
            float buttonTextX = pauseBtn.centerX() - (buttonTextWidth / 2);
            float buttonTextY = pauseBtn.centerY() - ((mPaint.descent() + mPaint.ascent()) / 2);

            // Adjust the pauseBtn rectangle dynamically based on the text it contains
            // This ensures the text fits well within the button
            pauseBtn.left = (int) (screenWidth / 2 - buttonTextWidth / 2 - 20); // 20 pixels padding
            pauseBtn.right = (int) (pauseBtn.left + buttonTextWidth + 40); // 40 pixels total padding

            // Draw the semi-transparent rectangle for the button
            mPaint.setColor(Color.argb(128, 255, 255, 255)); // Semi-transparent white for the button background
            mCanvas.drawRect(pauseBtn, mPaint);

            // Draw the button text
            mPaint.setColor(Color.BLACK); // Black color for text
            mCanvas.drawText(buttonText, buttonTextX, buttonTextY, mPaint);

            // If the game is paused, show "Tap to Play"
            if (mPaused) {
                mPaint.setColor(Color.WHITE); // White color for "Tap to Play"
                mPaint.setTextSize(150);
                String tapToPlay = "Tap to Play"; // Text for starting the game
                float tapPlayTextWidth = mPaint.measureText(tapToPlay);
                float startX = (screenWidth - tapPlayTextWidth) / 2;
                float startY = screenHeight / 2;
                mCanvas.drawText(tapToPlay, startX, startY, mPaint);
            }

            // Unlock the canvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        //Rect pauseBtn = new Rect(995, 150, 1095, 50);

        //int i = motionEvent.getActionIndex();
        int x = (int) motionEvent.getX();//i);
        int y = (int) motionEvent.getY();//i);


        int eventType = motionEvent.getAction();
        if (eventType == MotionEvent.ACTION_UP) {
                if (mPaused) {
                    mPaused = false;
                    newGame();

                    // Don't want to process snake direction for this tap
                    return true;
                }

                if ((pauseBtn.contains(x, y)) && mPlaying) {
                    mPlaying = !mPlaying;
                    //mPaused = !mPaused;
                    return true;
                }
                else if ((pauseBtn.contains(x, y)) && !mPlaying)  {
                    resume();
                }
                else {
                    // Let the Snake class handle the input
                    mSnake.switchHeading(motionEvent);
                    //break;
                }

        }
        return true;
    }


    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
