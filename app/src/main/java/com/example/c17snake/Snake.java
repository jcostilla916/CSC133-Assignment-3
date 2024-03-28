package com.example.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import java.util.ArrayList;

class Snake {

    // The location in the grid of all the segments
    private ArrayList<Point> segmentLocations;

    // How big is each segment of the snake?
    private int segmentSize;

    // How big is the entire grid
    private Point moveRange;

    // Where is the centre of the screen horizontally in pixels?
    private int halfWayPoint;

    // For tracking movement Heading
    private enum Heading {
        UP, RIGHT, DOWN, LEFT
    }

    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // Bitmaps for each direction the head can face
    private Bitmap bitmapHeadRight;
    private Bitmap bitmapHeadLeft;
    private Bitmap bitmapHeadUp;
    private Bitmap bitmapHeadDown;

    // Bitmap for the body
    private Bitmap bitmapBody;

    Snake(Context context, Point moveRange, int segmentSize) {
        // Initialize ArrayList
        segmentLocations = new ArrayList<>();

        // Initialize segmentSize and moveRange
        this.segmentSize = segmentSize;
        this.moveRange = moveRange;

        // Load and scale bitmaps
        bitmapHeadRight = loadAndScaleBitmap(context, R.drawable.head);
        bitmapHeadLeft = flipBitmap(bitmapHeadRight);
        bitmapHeadUp = rotateBitmap(bitmapHeadRight, -90);
        bitmapHeadDown = rotateBitmap(bitmapHeadRight, 180);

        bitmapBody = loadAndScaleBitmap(context, R.drawable.body);

        // Calculate halfway point across the screen
        halfWayPoint = moveRange.x * segmentSize / 2;
    }

    // Method to load and scale bitmap
    private Bitmap loadAndScaleBitmap(Context context, int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        return Bitmap.createScaledBitmap(bitmap, segmentSize, segmentSize, false);
    }

    // Method to flip bitmap horizontally
    private Bitmap flipBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, segmentSize, segmentSize, matrix, true);
    }

    // Method to rotate bitmap
    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, segmentSize, segmentSize, matrix, true);
    }

    // Method to reset snake for a new game
    void reset(int width, int height) {
        heading = Heading.RIGHT;
        segmentLocations.clear();
        segmentLocations.add(new Point(width / 2, height / 2));
    }

    // Method to move the snake
    void move() {
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            segmentLocations.get(i).set(segmentLocations.get(i - 1).x, segmentLocations.get(i - 1).y);
        }

        Point head = segmentLocations.get(0);
        switch (heading) {
            case UP:
                head.y--;
                break;
            case RIGHT:
                head.x++;
                break;
            case DOWN:
                head.y++;
                break;
            case LEFT:
                head.x--;
                break;
        }
    }

    // Method to detect if snake has died
    boolean detectDeath() {
        Point head = segmentLocations.get(0);
        if (head.x == -1 || head.x > moveRange.x || head.y == -1 || head.y > moveRange.y)
            return true;

        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            if (head.equals(segmentLocations.get(i)))
                return true;
        }
        return false;
    }

    // Method to check if snake has eaten the apple
    boolean checkDinner(Point location) {
        if (segmentLocations.get(0).equals(location)) {
            segmentLocations.add(new Point(-10, -10));
            return true;
        }
        return false;
    }

    // Method to draw snake on canvas
    void draw(Canvas canvas, Paint paint) {
        if (!segmentLocations.isEmpty()) {
            Point head = segmentLocations.get(0);
            switch (heading) {
                case RIGHT:
                    canvas.drawBitmap(bitmapHeadRight, head.x * segmentSize, head.y * segmentSize, paint);
                    break;
                case LEFT:
                    canvas.drawBitmap(bitmapHeadLeft, head.x * segmentSize, head.y * segmentSize, paint);
                    break;
                case UP:
                    canvas.drawBitmap(bitmapHeadUp, head.x * segmentSize, head.y * segmentSize, paint);
                    break;
                case DOWN:
                    canvas.drawBitmap(bitmapHeadDown, head.x * segmentSize, head.y * segmentSize, paint);
                    break;
            }

            for (int i = 1; i < segmentLocations.size(); i++) {
                canvas.drawBitmap(bitmapBody, segmentLocations.get(i).x * segmentSize, segmentLocations.get(i).y * segmentSize, paint);
            }
        }
    }

    // Method to handle changing direction
    void switchHeading(MotionEvent motionEvent) {
        if (motionEvent.getX() >= halfWayPoint) {
            switch (heading) {
                case UP:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.UP;
                    break;
            }
        } else {
            switch (heading) {
                case UP:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.UP;
                    break;
            }
        }
    }
}
