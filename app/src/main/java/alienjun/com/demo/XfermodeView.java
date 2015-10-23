package alienjun.com.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alienjunx on 15/10/22.
 */
public class XfermodeView extends View {
    private int width, height;
    private int bitmapSize;
    private int offset;
    private int paddingTop;
    private Paint xfermodePaint;
    private Paint paint;

    public XfermodeView(Context context) {
        super(context, null);
    }

    public XfermodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        xfermodePaint =  new Paint();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);

        setXfermode(MODE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        bitmapSize = w / 2;
        offset = bitmapSize / 3;
        //创建原图和目标图
        srcBitmap = makeSrc(bitmapSize, bitmapSize);
        dstBitmap = makeDst(bitmapSize, bitmapSize);
        xfermodeBitmap = createXfermodeBitmap();
    }

    //PorterDuff模式常量 可以在此更改不同的模式测试
    private static final PorterDuff.Mode MODE = PorterDuff.Mode.SRC_IN;
    private Bitmap srcBitmap, dstBitmap;


    //创建一个圆形bitmap，作为dst图
    private Bitmap makeDst(int width, int height) {
        Bitmap bm = createBitamp(width, height);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFCC44);
        c.drawOval(new RectF(0, 0, width / 3 * 2, height / 3 * 2), p);
        return bm;
    }

    // 创建一个矩形bitmap，作为src图
    private Bitmap makeSrc(int width, int height) {
        Bitmap bm = createBitamp(width, height);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFF66AAFF);
        c.drawRect(width / 3, height / 3, width, height, p);
        return bm;
    }

    private Bitmap createBitamp(int w, int h){
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(-offset/2, 0);

        canvas.drawBitmap(srcBitmap, 0, -offset, null);
        canvas.drawText("src", offset, offset, paint);

        canvas.drawBitmap(dstBitmap, 4 * offset, 0, null);
        canvas.drawText("dst", 4 * offset, offset, paint);

        canvas.restore();

        canvas.drawBitmap(xfermodeBitmap, 0, bitmapSize, null);
    }


    private Bitmap xfermodeBitmap;
    private Bitmap createXfermodeBitmap(){
        if(bitmapSize <= 0){
            return null;
        }
        Bitmap bitmap = createBitamp(bitmapSize, bitmapSize);
        Canvas canvas = new Canvas(bitmap);

        // 两个图片大小一致，且重叠？
        canvas.drawBitmap(dstBitmap, 0, 0, null);
        canvas.drawBitmap(srcBitmap, 0, 0, xfermodePaint);
        return bitmap;
    }

    private PorterDuffXfermode porterDuffXfermode;
    public void setXfermode(PorterDuff.Mode mode) {
        if(mode == null){
            return;
        }
        porterDuffXfermode = new PorterDuffXfermode(mode);
        xfermodePaint.setXfermode(porterDuffXfermode);
        xfermodeBitmap = createXfermodeBitmap();
        invalidate();
    }
}
