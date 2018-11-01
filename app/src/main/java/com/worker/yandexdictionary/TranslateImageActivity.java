package com.worker.yandexdictionary;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TranslateImageActivity extends BaseCustomActivity {

    private SharedPreferences preferences;
    private boolean useCloudOCR = false;
    private PhotoView imageIV;
    private Paint blockPaint;
    private Paint textPaint;
    private Map<Rect, String> boxToText = new HashMap<>();
    private int bitmapWidth;
    private int bitmapHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_translate_image);
        getSupportActionBar().setTitle("Translate image");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        blockPaint = new Paint();
        blockPaint.setColor(Color.YELLOW);
        blockPaint.setAlpha(0xff / 3);
        blockPaint.setAntiAlias(true);
        super.initViews();
        imageIV = findViewById(R.id.imageIV);
        imageIV.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                int r_x = (int) (x * bitmapWidth);
                int r_y = (int) (y * bitmapHeight);
                Log.d("POSITION", MessageFormat.format("x: {0} y:{1}", r_x, r_y));
                for (Map.Entry<Rect, String> box : boxToText.entrySet()) {
                    Log.d("POSITION", MessageFormat.format("box {0}", box.getKey().flattenToString()));
                    if (box.getKey().contains(r_x, r_y)) {
                        translate(box.getValue());
                    }
                }
            }
        });
        takeNewImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        useCloudOCR = preferences.getBoolean("use_cloud_ocr", false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.take_picture) {
            takeNewImage();
        }
        return super.onOptionsItemSelected(item);
    }

    private void takeNewImage() {
        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult pickResult) {
                Throwable error = pickResult.getError();
                if (error == null) {
                    Bitmap bitmap = pickResult.getBitmap();
                    final Bitmap drawBitmap = bitmap.copy(bitmap.getConfig(), true);
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                    FirebaseVisionTextRecognizer recognizer;
                    imageIV.setImageBitmap(bitmap);
                    waitDialog.show();
                    if (useCloudOCR)
                        recognizer = FirebaseVision.getInstance().getCloudTextRecognizer();
                    else
                        recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                    final Canvas canvas = new Canvas(drawBitmap);

                    recognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText result) {
                            StringBuilder buf = new StringBuilder();
                            boxToText.clear();
                            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                                canvas.drawRect(block.getBoundingBox(), blockPaint);
                                boxToText.put(block.getBoundingBox(), block.getText());
                                buf.append(block.getText());
                            }
                            imageIV.setImageBitmap(drawBitmap);
                            bitmapWidth = drawBitmap.getWidth();
                            bitmapHeight = drawBitmap.getHeight();
                            waitDialog.dismiss();
                        }
                    });


                } else {
                    toast(error.getMessage());
                }

            }
        }).show(this);
    }

    @Override
    protected void afterTranslate(String result) {
        toast(result);
    }
}
