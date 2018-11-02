package com.worker.yandexdictionary;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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

//translate image
public class ImageTranslatingActivity extends BaseTranslatingActivity {

    private boolean useCloudOCR = false; //use cloud OCR flag
    private PhotoView imageIV; //the view shows the image
    private Paint blockPaint; //define how we draw the text block
    private Map<Rect, String> boxToText = new HashMap<>(); //map from each text block's position to its text content
    private int bitmapWidth; //the image actual size
    private int bitmapHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_image); //fetch the view
        getSupportActionBar().setTitle("Translate image");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button at top-left
        }
        blockPaint = new Paint(); //setup the paint
        blockPaint.setColor(Color.YELLOW); //it's 1/3 see-through yellow
        blockPaint.setAlpha(0xff / 3);
        blockPaint.setAntiAlias(true); //make the edges smoother
        super.initViews();
        imageIV = findViewById(R.id.imageIV);
        imageIV.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) { //listen to any tap on the image
                int r_x = (int) (x * bitmapWidth); //x,y is the scaled position, like 0.343
                int r_y = (int) (y * bitmapHeight); //so needs to be multiply with image size to get real position
                Log.d("POSITION", MessageFormat.format("x: {0} y:{1}", r_x, r_y));
                for (Map.Entry<Rect, String> box : boxToText.entrySet()) { //loop through all text blocks
                    if (box.getKey().contains(r_x, r_y)) {//,if the tap is inside any block
                        translate(box.getValue());  //get the block's text and translate it
                    }
                }
            }
        });
        takeNewImage(); //select a image at start
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        useCloudOCR = preferences.getBoolean("use_cloud_ocr", false); //read flag settings
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //called when menu items are clicked
        int id = item.getItemId();
        if (id == android.R.id.home) { //home button now is the back button
            finish(); //close the activity
            return true;
        } else if (id == R.id.take_picture) { //take new picture
            takeNewImage();
        }
        return super.onOptionsItemSelected(item);
    }

    private void takeNewImage() { //show the dialog to select image from camera or gallery
        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult pickResult) {
                Throwable error = pickResult.getError();
                if (error == null) { //everything seems to be ok
                    Bitmap bitmap = pickResult.getBitmap(); //get the bitmap
                    final Bitmap drawBitmap = bitmap.copy(bitmap.getConfig(), true); //need to copy to new mutable bitmap so we can draw on it
                    imageIV.setImageBitmap(bitmap); //show the bitmap to the screen
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap); //wrap bitmap to recognize
                    FirebaseVisionTextRecognizer recognizer; //choose the recognizer
                    waitDialog.show(); //start waiting
                    if (useCloudOCR)
                        recognizer = FirebaseVision.getInstance().getCloudTextRecognizer(); //upload image to server to recognize, slower but get higher accuracy
                    else
                        recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer(); //local on-device recognizer, faster and free
                    final Canvas canvas = new Canvas(drawBitmap);

                    recognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText result) { //we get the recognition result
                            boxToText.clear(); //clear the mapping table
                            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) { //loop through text blocks
                                canvas.drawRect(block.getBoundingBox(), blockPaint); //draw the text block by yellow
                                boxToText.put(block.getBoundingBox(), block.getText()); //save it to mapping table
                            }
                            imageIV.setImageBitmap(drawBitmap); //show the image
                            bitmapWidth = drawBitmap.getWidth(); //save image's size
                            bitmapHeight = drawBitmap.getHeight();
                            waitDialog.dismiss(); //close waiting dialog
                        }
                    });


                } else {
                    error.printStackTrace();
                    toast(error.getMessage()); //show error
                }

            }
        }).show(this); //start selecting image
    }

    @Override
    protected void afterTranslate(String result) { //show translated result in a dialog
        new AlertDialog.Builder(this).setMessage(result).setTitle("Translation result").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        }).create().show();
    }
}
