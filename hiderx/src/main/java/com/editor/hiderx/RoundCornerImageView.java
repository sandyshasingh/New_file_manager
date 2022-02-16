package com.editor.hiderx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.editor.hiderx.R;


@SuppressLint("AppCompatCustomView")
public class RoundCornerImageView extends ImageView {

    private float radius = 0.0f;
    private Path path;
    private RectF rect;

    public RoundCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRadius(context,attrs);
        init();
    }

    private void setRadius(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);

        radius = (float) typedArray.getDimension(R.styleable.RoundCornerImageView_corner_radius,
                0);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setRadius(context, attrs);
        init();
    }

    private void init() {
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}