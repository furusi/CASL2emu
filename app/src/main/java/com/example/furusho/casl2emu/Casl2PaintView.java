package com.example.furusho.casl2emu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class Casl2PaintView extends View {

    Paint paint = new Paint();

    public Casl2PaintView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(100,100,300,300,paint);
    }
}
