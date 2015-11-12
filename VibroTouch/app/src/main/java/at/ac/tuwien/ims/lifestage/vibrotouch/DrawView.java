package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandler;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerScenario2;

/**
 * A view where objects are drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class DrawView extends View {
    private ScaleGestureDetector mScaleDetector;

    /*private float mLastTouchX, mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;*/

    private ObjectHandler objectHandler=null;

    private long lastScale=0, last3Touch=0;

    public DrawView(Context context) {
        super(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setObjectHandler(ObjectHandler objectHandler) {
        this.objectHandler=objectHandler;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if(objectHandler!=null)
            objectHandler.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        int pointerCount = ev.getPointerCount();
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            /*case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }*/

            case MotionEvent.ACTION_MOVE: {
                /*final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                if(objectHandler!=null)
                    objectHandler.handleMove(x, y, dx, dy);

                invalidate();

                mLastTouchX = x;
                mLastTouchY = y;
                */
                if (!mScaleDetector.isInProgress())
                    if(pointerCount==1 && (System.currentTimeMillis()-last3Touch)>=250) {
                        ArrayList<PointF> points=new ArrayList<>();
                        points.add(new PointF(MotionEventCompat.getX(ev, 0), MotionEventCompat.getY(ev, 0)));
                        /*points.add(new PointF(MotionEventCompat.getX(ev, 1), MotionEventCompat.getY(ev, 1)));
                        points.add(new PointF(MotionEventCompat.getX(ev, 2), MotionEventCompat.getY(ev, 2)));*/
                        float[] coords=centroid(points);
                        if(objectHandler!=null)
                            objectHandler.handleThreeFingerTap(coords[0], coords[1]);
                        last3Touch=System.currentTimeMillis();
                        invalidate();
                    }
                break;
            }

            /*case MotionEvent.ACTION_UP: {
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
            }*/
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

            if (objectHandler != null)
                if(objectHandler instanceof ObjectHandlerScenario2) {
                    objectHandler.handleScale(scale, x, y);
                } else {
                    if ((System.currentTimeMillis() - lastScale) >= 25) {
                        objectHandler.handleScale(scale, x, y);
                        lastScale = System.currentTimeMillis();
                    }
                }
            invalidate();
            return true;
        }
    }

    public float[] centroid(ArrayList<PointF> knots)  {
        float[] res=new float[2];
        float centroidX = 0, centroidY = 0;

        for(PointF knot : knots) {
            centroidX += knot.x;
            centroidY += knot.y;
        }
        res[0]=centroidX / knots.size();
        res[1]=centroidY / knots.size();
        return res;
    }
}