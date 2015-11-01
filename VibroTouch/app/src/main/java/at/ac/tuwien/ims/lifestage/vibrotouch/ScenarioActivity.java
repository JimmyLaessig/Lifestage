package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import at.ac.tuwien.ims.lifestage.vibrotouch.Scene.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scene.ObjectHandler;

/**
 * Activity where the different scenarios are drawn.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ScenarioActivity extends AppCompatActivity {
    private DrawView drawView;
    private ObjectHandler objectHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        objectHandler =new ObjectHandler();
        objectHandler.add(new Object(700, 200, 250, 250, Color.RED));
        objectHandler.add(new Object(400, 200, 200, 200, Color.BLACK));
        objectHandler.add(new Object(150, 200, 150, 150, Color.BLUE));

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.parseColor("#f9f9f9"));
        setContentView(drawView);
    }

    /**
     * A view where objects are drawn and interacted with.
     *
     */
    private class DrawView extends View {
        private ScaleGestureDetector mScaleDetector;
        private float mLastTouchX, mLastTouchY;

        private static final int INVALID_POINTER_ID = -1;
        private int mActivePointerId = INVALID_POINTER_ID;

        public DrawView(Context context) {
            super(context);
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            objectHandler.draw(canvas);
            canvas.restore();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            mScaleDetector.onTouchEvent(ev);

            final int action = MotionEventCompat.getActionMasked(ev);
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    objectHandler.handleMove(x, y, dx, dy);

                    invalidate();

                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                    if (pointerId == mActivePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                        mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                        mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                    }
                    break;
                }
            }
            return true;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            private float scale, x, y;
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale=detector.getScaleFactor();
                x=detector.getFocusX();
                y=detector.getFocusY();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                objectHandler.handleScale(scale, x, y);
            }
        }
    }
}
