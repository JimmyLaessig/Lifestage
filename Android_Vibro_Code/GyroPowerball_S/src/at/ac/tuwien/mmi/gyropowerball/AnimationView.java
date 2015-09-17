package at.ac.tuwien.mmi.gyropowerball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AnimationView extends View {
    private ShapeDrawable mDrawable;
    private Paint paint_black;
    private Bitmap bmap;
    private float pos_x = -1;
    private float pos_y = -1;
    private Paint paint_grey;
    private Paint paint_white;
    private Paint paint_green;
    private Bitmap map;
    private float[] pathArray;
    private boolean isRotating;
    private Bitmap bmpOldMan_scaled, bmpOldMan_rot_scaled;
    private Matrix matrix;
    private int rotation;
    private String speechBubbleText;
    private SparkManager sparkManager;
    
    public AnimationView(Context context, AttributeSet attr) {
	super(context, attr);

	int x = 10;
	int y = 10;
	int width = 300;
	int height = 50;

	mDrawable = new ShapeDrawable(new OvalShape());
	mDrawable.getPaint().setColor(0xff74AC23);
	mDrawable.setBounds(x, y, x + width, y + height);

	paint_black = new Paint();
	paint_black.setARGB(150, 0, 0, 0);
	paint_black.setTextSize(34);
	

	paint_white = new Paint();
	paint_white.setARGB(150, 255, 255, 255);

	paint_green = new Paint();
	paint_green.setARGB(150, 50, 255, 50);

	paint_grey = new Paint();
	paint_grey.setARGB(60, 255, 0, 0);
	paint_grey.setStrokeWidth(7);
	paint_grey.setStyle(Paint.Style.STROKE);
	paint_grey.setPathEffect(new DashPathEffect(new float[] { 8, 4, 8, 4 }, 0));

	bmap = BitmapFactory.decodeResource(getResources(), R.drawable.map1);
	
	matrix = new Matrix();
	
	
	
    }

    protected void onDraw(Canvas canvas) {

	if (map == null) {
	    map = Bitmap.createScaledBitmap(bmap, getWidth(), getHeight(), true);
	    
	    Bitmap bmpOldMan_Raw = BitmapFactory.decodeResource(getResources(), R.drawable.old_man); 
	    int scaledWidth = canvas.getWidth()/15;
	    int scaledHeight = scaledWidth * bmpOldMan_Raw.getHeight() / bmpOldMan_Raw.getWidth();
	    bmpOldMan_scaled = Bitmap.createScaledBitmap(bmpOldMan_Raw, scaledWidth, scaledHeight, true);
	    
	    Bitmap bmpOldMan_rot_Raw = BitmapFactory.decodeResource(getResources(), R.drawable.old_man_rot); 
	    bmpOldMan_rot_scaled = Bitmap.createScaledBitmap(bmpOldMan_rot_Raw, scaledWidth, scaledHeight, true);

	    pathArray = new float[12];
	    pathArray[0] = getWidth() * 115 / 720;
	    pathArray[1] = getHeight() * 330 / 991;
	    pathArray[2] = getWidth() * 130 / 720;
	    pathArray[3] = getHeight() * 480 / 991;
	    pathArray[4] = getWidth() * 465 / 720;
	    pathArray[5] = getHeight() * 460 / 991;
	    pathArray[6] = getWidth() * 485 / 720;
	    pathArray[7] = getHeight() * 635 / 991;
	    pathArray[8] = getWidth() * 595 / 720;
	    pathArray[9] = getHeight() * 635 / 991;
	    pathArray[10] = getWidth() * 560 / 720;
	    pathArray[11] = getHeight() * 840 / 991;
	}

	if (pos_x == -1){
	    pos_x = pathArray[0];
	    pos_y = pathArray[1];
	}
	canvas.drawBitmap(map, 0, 0, paint_black);

	for (int i = 0; i < 10; i += 2) {
	    canvas.drawLine(pathArray[i], pathArray[i + 1], pathArray[i + 2], pathArray[i + 3],
		    paint_grey);
	}


	if (isRotating) {
	    Log.v("XX", "rotating " + rotation);
	  
	    matrix.setTranslate(pos_x-bmpOldMan_scaled.getWidth()/2, pos_y-bmpOldMan_scaled.getHeight()/2);
	    canvas.drawBitmap(bmpOldMan_rot_scaled, matrix, null);
	    
	    canvas.drawText(speechBubbleText, pos_x+bmpOldMan_scaled.getWidth()/2, pos_y-bmpOldMan_scaled.getHeight()/2, paint_black);
	} else {
	    
	  
	    matrix.setTranslate(pos_x-bmpOldMan_scaled.getWidth()/2, pos_y-bmpOldMan_scaled.getHeight()/2);
	    canvas.drawBitmap(bmpOldMan_scaled, matrix, null);
	}
    }

    public void start() {
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		for (int i = 0; i < 10; i += 2) {

		    if(i<2){
			speechBubbleText = "Links?";
		    }else if(i < 4){
			speechBubbleText = "Rechts?";
		    }else if(i < 6){
			speechBubbleText = "Links?";
		    }else if(i < 8){
			speechBubbleText = "Rechts?";
		    }else if(i < 10){
			speechBubbleText = "EI!!!";
		    }
		    float x1 = pathArray[i];
		    float y1 = pathArray[i + 1];
		    float x2 = pathArray[i + 2];
		    float y2 = pathArray[i + 3];

		    float dx = Math.abs(x2 - x1);
		    float dy = Math.abs(y2 - y1);

		    int sx = (x1 < x2) ? 1 : -1;
		    int sy = (y1 < y2) ? 1 : -1;

		    float err = dx - dy;

		    while (true) {
			pos_x = x1;
			pos_y = y1;

			AnimationView.this.postInvalidate();

			try {
			    Thread.sleep(20);
			} catch (InterruptedException e) {
			}

			if (x1 == x2 && y1 == y2) {
			    break;
			}

			float e2 = 2 * err;

			if (e2 > -dy) {
			    err = err - dy;
			    x1 = x1 + sx;
			}

			if (e2 < dx) {
			    err = err + dx;
			    y1 = y1 + sy;
			}
		    }

		    isRotating = true;
		    AnimationView.this.postInvalidate();
		 
		    if(i<2){
			sparkManager.sendCommand_LeftRotation();
		    }else if(i < 4){
			sparkManager.sendCommand_RightRotation();
		    }else if(i < 6){
			sparkManager.sendCommand_LeftRotation();
		    }else if(i < 8){
			sparkManager.sendCommand_RightRotation();
		    }else if(i < 10){
			//speechBubbleText = "DOOO";
		    }
		    
		    try {
			Thread.sleep(1800);
		    } catch (InterruptedException e) {
		    }
		    isRotating = false;
		    AnimationView.this.postInvalidate();
		}
	    }
	}).start();

    }

    public void setSparkManager(SparkManager sparkManager) {
	this.sparkManager = sparkManager;
    }

}
