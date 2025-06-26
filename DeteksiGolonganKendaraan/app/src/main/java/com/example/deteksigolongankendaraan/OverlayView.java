package com.example.deteksigolongankendaraan; // Pastikan ini sesuai dengan nama package Anda

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;

// Impor-impor PENTING dari ML Kit
import com.google.mlkit.vision.objects.DetectedObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale; // <-- IMPORT YANG DITAMBAHKAN

public class OverlayView extends View {

    private List<DetectedObject> detectedObjects = new LinkedList<>();
    private Paint boxPaint;
    private Paint textPaint;

    // Variabel untuk transformasi koordinat yang lebih akurat
    private float scale = 1.0f;
    private float offsetX = 0f;
    private float offsetY = 0f;

    // Dimensi gambar asli dari ImageAnalysis
    private int lastImageWidth = 0;
    private int lastImageHeight = 0;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6.0f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36.0f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(5.0f, 2.0f, 2.0f, Color.BLACK);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (lastImageWidth > 0 && lastImageHeight > 0) {
            calculateTransformation(lastImageWidth, lastImageHeight);
        }
    }

    private void calculateTransformation(int imageWidth, int imageHeight) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth == 0 || viewHeight == 0) {
            this.scale = 1.0f;
            this.offsetX = 0f;
            this.offsetY = 0f;
            return;
        }

        this.lastImageWidth = imageWidth;
        this.lastImageHeight = imageHeight;

        float scaleX = viewWidth / (float) imageWidth;
        float scaleY = viewHeight / (float) imageHeight;

        this.scale = Math.max(scaleX, scaleY); // FILL_CENTER logic

        this.offsetX = (viewWidth - (imageWidth * this.scale)) / 2f;
        this.offsetY = (viewHeight - (imageHeight * this.scale)) / 2f;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (detectedObjects.isEmpty()) {
            return;
        }

        for (DetectedObject detectedObject : detectedObjects) {
            Rect boundingBox = detectedObject.getBoundingBox();

            RectF onScreenRect = new RectF(
                    (boundingBox.left * scale) + offsetX,
                    (boundingBox.top * scale) + offsetY,
                    (boundingBox.right * scale) + offsetX,
                    (boundingBox.bottom * scale) + offsetY
            );

            canvas.drawRect(onScreenRect, boxPaint);

            if (!detectedObject.getLabels().isEmpty()) {
                DetectedObject.Label label = detectedObject.getLabels().get(0);
                // Menggunakan Locale.getDefault() untuk format string
                String labelText = String.format(Locale.getDefault(), "%s (%.2f)",
                        label.getText(),
                        label.getConfidence()
                );
                canvas.drawText(
                        labelText,
                        onScreenRect.left + 5,
                        onScreenRect.top - 10,
                        textPaint
                );
            }
        }
    }

    public void updateResults(List<DetectedObject> results, int imageWidth, int imageHeight) {
        this.detectedObjects = results;
        calculateTransformation(imageWidth, imageHeight);
        invalidate();
    }

    public void clear() {
        this.detectedObjects.clear();
        calculateTransformation(0,0);
        invalidate();
    }
}