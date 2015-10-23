package com.handmark.pulltorefresh.library.extras;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.handmark.pulltorefresh.library.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alienjunx on 15/10/22.
 */
public class WaveAnimationView extends View {

    private Paint wavePaint;
    private Timer timer;
    private MyTimerTask waveTask;

    //波形相关
    private float waveWidth;
    private float waveHeight;
    private float density;
    private float waveMid;
    private float maxAmplitude;
    private float phaseShift;
    private float phase;
    private float frequency;//频率


    //图片相关
    private Bitmap logoBitmap;
    private PorterDuffXfermode mXfermode;
    private int logoBitWidth, logoBitHeight;
    private Rect mSrcRect,mDestRect;

    //动画移动相关
    private final float WAVE_POSITION_Y= 45.0f;//波形默认所在位置
    private float wavePositionY = WAVE_POSITION_Y;
    private Matrix matrix;
    private boolean isStop;


    public WaveAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveAnimationView);

        //波形参数初始化
        density = 1.f;
        frequency = typedArray.getFloat(R.styleable.WaveAnimationView_frequency,1.0f);
        phaseShift = -0.25f;

        //画笔
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setDither(true);
        wavePaint.setColor(Color.RED);
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setAntiAlias(true);

        //混合模式
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

        //logo图片
        BitmapDrawable originalBitmap = (BitmapDrawable) typedArray.getDrawable(R.styleable.WaveAnimationView_logoImage);
        logoBitmap = originalBitmap.getBitmap();
        logoBitWidth = logoBitmap.getWidth();
        logoBitHeight = logoBitmap.getHeight();
        mSrcRect = new Rect(0, 0, logoBitWidth, logoBitHeight);

        //用于移动波形
        matrix = new Matrix();
    }

    //刷新
    Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            phase += phaseShift;
            waveMid = waveWidth / 2.0f;
            maxAmplitude = waveHeight - 4.0f;

            if (!isStop) {
                invalidate();
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
//        start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        waveWidth = getWidth();
        waveHeight = (float)(getHeight()*0.4);
        mDestRect = new Rect(0, 0, (int)waveWidth, getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制波形
        Path wavePath = new Path();
        float endX = 0;
        for (float x = 0;x<waveWidth+density;x += density){
            endX = x;
            double scaling = -Math.pow(x / waveMid  - 1, 2) + 1;//波形中间变大
            float y = (float)(scaling * maxAmplitude * Math.sin(2 * Math.PI *(x / 50) * frequency + phase)+ (waveHeight * 0.5));
            if (x == 0)
                wavePath.moveTo(x,y);
            else
                wavePath.lineTo(x,y);
        }
        float endY = waveHeight+55;//向下多偏移以保证填充满需要显示的图片
        wavePath.lineTo(endX,endY);
        wavePath.lineTo(0,endY);
        wavePath.lineTo(0,5);


        //存为新图层
        int saveLayerCount = canvas.saveLayer(0, 0, logoBitWidth, logoBitHeight, wavePaint,
                Canvas.ALL_SAVE_FLAG);

        //绘制目标图
        canvas.drawBitmap(logoBitmap, mSrcRect, mDestRect, wavePaint);

        //设置混合模式
        wavePaint.setXfermode(mXfermode);

        //移动波形
        wavePositionY -= 0.3;
        if (wavePositionY <= -25){
            wavePositionY = WAVE_POSITION_Y;
        }

        matrix.setTranslate(0, wavePositionY);
        wavePath.transform(matrix);

        //绘制源图形
        canvas.drawPath(wavePath, wavePaint);

        //清除混合模式
        wavePaint.setXfermode(null);

        //恢复保存的图层；
        canvas.restoreToCount(saveLayerCount);
    }

    private void start(){
        if (waveTask == null) {
            waveTask = new MyTimerTask(updateHandler);
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(waveTask, 0, 10);
        }
    }
    private  void stop(){
        if (timer != null)
            timer.cancel();
        if (waveTask != null)
            waveTask.cancel();
    }

    /**
     * 开始动画
     */
    public void startAnimation(){
        wavePositionY = WAVE_POSITION_Y;
        isStop = false;
        invalidate();
        start();
        Log.d("ww", "startAnimation...");
    }

    /**
     * 结束动画
     */
    public void stopAnimation(){
        wavePositionY = WAVE_POSITION_Y;
        invalidate();
        isStop = true;
        stop();
        Log.d("ww", "stopAnimation...");
    }



    class MyTimerTask extends TimerTask
    {
        Handler handler;

        public MyTimerTask(Handler handler)
        {
            this.handler = handler;
        }

        @Override
        public void run()
        {
            handler.sendMessage(handler.obtainMessage());
        }

    }
}
