package com.example.c17snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable {

    // Objects for the game loop/thread
    private Thread mThread = null;
    private long mNextFrameTime;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // Sound effects
    private SoundPool mSP;
    private int mEatSoundID = -1;
    private int mCrashSoundID = -1;

    private int screenWidth;
    private int screenHeight;

    // Game area dimensions
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // Score tracking
    private int mScore;

    // Drawing objects
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    private Snake mSnake;
    private Apple mApple;

    private Bitmap mBitmapBackground;

    private Rect pauseBtn = new Rect(991, 50, 1091, 150);

    public SnakeGame(Context context, Point size) {
        super(context);

        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        mBitmapBackground = BitmapFactory.decodeResource(getResources(), R.drawable.bgred);
        mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, size.x, size.y, false);
        screenWidth = size.x;
        screenHeight = size.y;

        // Initialize SoundPool
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

        // Load sound effects
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("get_apple.ogg");
            mEatSoundID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashSoundID = mSP.load(descriptor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Initialize game objects
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
    }

    public void newGame() {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused) {
                if (updateRequired()) {
                    update();
                }
            }
            draw();
        }
    }

    public boolean updateRequired() {
        final long TARGET_FPS = 10;
        final long MILLIS_PER_SECOND = 1000;
        if (mNextFrameTime <= System.currentTimeMillis()) {
            mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;
            return true;
        }
        return false;
    }

    public void update() {
        mSnake.move();
        if (mSnake.checkDinner(mApple.getLocation())) {
            mApple.spawn();
            mScore++;
            mSP.play(mEatSoundID, 1, 1, 0, 0, 1);
        }
        if (mSnake.detectDeath()) {
            mSP.play(mCrashSoundID, 1, 1, 0, 0, 1);
            mPaused = true;
        }
    }

    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawBitmap(mBitmapBackground, 0, 0, null);
            mPaint.setColor(Color.WHITE);
            mPaint.setTextSize(80);
            mCanvas.drawText("" + mScore, 20, 120, mPaint);
            float nameTextWidth = mPaint.measureText("Eva and Jorge");
            mCanvas.drawText("Eva and Jorge", screenWidth - nameTextWidth - 20, 120, mPaint);
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);
            drawPauseButton();
            if (mPaused) {
                drawTapToPlayText();
            }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void drawPauseButton() {
        mPaint.setTextSize(40);
        String buttonText = mPlaying && !mPaused ? "Pause" : "Resume";
        float buttonTextWidth = mPaint.measureText(buttonText);
        float buttonTextX = pauseBtn.centerX() - (buttonTextWidth / 2);
        float buttonTextY = pauseBtn.centerY() - ((mPaint.descent() + mPaint.ascent()) / 2);
        pauseBtn.left = (int) (screenWidth / 2 - buttonTextWidth / 2 - 20);
        pauseBtn.right = (int) (pauseBtn.left + buttonTextWidth + 40);
        mPaint.setColor(Color.argb(128, 255, 255, 255));
        mCanvas.drawRect(pauseBtn, mPaint);
        mPaint.setColor(Color.BLACK);
        mCanvas.drawText(buttonText, buttonTextX, buttonTextY, mPaint);
    }

    private void drawTapToPlayText() {
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(150);
        String tapToPlay = "Tap to Play";
        float tapPlayTextWidth = mPaint.measureText(tapToPlay);
        float startX = (screenWidth - tapPlayTextWidth) / 2;
        float startY = screenHeight / 2;
        mCanvas.drawText(tapToPlay, startX, startY, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int eventType = motionEvent.getAction();
        if (eventType == MotionEvent.ACTION_UP) {
            if (mPaused) {
                mPaused = false;
                newGame();
                return true;
            }
            if ((pauseBtn.contains(x, y)) && mPlaying) {
                mPlaying = !mPlaying;
                return true;
            } else if ((pauseBtn.contains(x, y)) && !mPlaying) {
                resume();
            } else {
                mSnake.switchHeading(motionEvent);
            }
        }
        return true;
    }

    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
