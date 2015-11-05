package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.view.MotionEventCompat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandler;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Activity where the different scenarios are drawn.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ScenarioActivity extends BaseActivity {
    private ObjectHandler objectHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Testcase testcase=null;
        try {
            testcase = testcases.get(getIntent().getIntExtra("testcase", 0)-1);
        } catch(Exception e){
            e.printStackTrace();
        }
        objectHandler=new ObjectHandler(testcase);

        DrawView drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.parseColor("#f9f9f9"));
        setContentView(drawView);
    }
    @Override
    public void onPause() {
        super.onPause();
        if(objectHandler!=null)
            objectHandler.stopThreads();

        /*TODO
        try {
            XmlHelper.writeTestCase1Result(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath, 1, 2, 3, 4, 5, 6, 7);
        } catch (Exception e) {
            Toast.makeText(ScenarioActivity.this, "Something went wrong while saving.", Toast.LENGTH_SHORT).show();
        }
        */
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
                invalidate();
            }
        }
    }
}
