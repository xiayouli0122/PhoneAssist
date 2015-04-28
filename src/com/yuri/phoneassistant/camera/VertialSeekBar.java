package com.yuri.phoneassistant.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * @ClassName: VerticalSeekBar
 * @Description: 自定义垂直seekbar
 * @author： wen
 * @version： v1.0
 */
public class VertialSeekBar extends SeekBar {

	public VertialSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VertialSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VertialSeekBar(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.rotate(-90);
		canvas.translate(-getHeight(), 0);
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			setProgress(getMax()
					- (int) (getMax() * event.getY() / getHeight()));
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return true;
	}

}
