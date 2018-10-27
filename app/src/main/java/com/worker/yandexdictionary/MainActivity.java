package com.worker.yandexdictionary;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;
    private Button translateBtn;
    private EditText inputED;
    private TextView outputTV;
    public final String API_KEY = "trnsl.1.1.20181026T162625Z.b5c819a79d765111.4bc11bec1128eb4473dd11d0feafdfcb1525296b";
    public final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key={0}&text={1}&lang={2}&format=plain";
    private ProgressDialog waitDialog;
    private Spinner toSpn;
    private Spinner fromSpn;
    private ImageButton inputCaptureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        askForPermission();
        initViews();

    }

    private void initViews() {
        translateBtn = findViewById(R.id.translateBtn);
        inputCaptureBtn = findViewById(R.id.inputCaptureBtn);
        inputED = findViewById(R.id.inputED);
        outputTV = findViewById(R.id.outputTV);
        fromSpn = findViewById(R.id.fromSpn);
        toSpn = findViewById(R.id.toSpn);

        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle("Please wait");
        waitDialog.setMessage("Translating...");
        waitDialog.setCancelable(false);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputED.getText().toString().trim();
                inputText = URLEncoder.encode(inputText);
                String fromLanguage = LanguageHelper.LANGUAGES_CODE[fromSpn.getSelectedItemPosition()];
                String toLanguage = LanguageHelper.LANGUAGES_CODE[toSpn.getSelectedItemPosition()];
                waitDialog.show();
                queue.add(new StringRequest(MessageFormat.format(BASE_URL, API_KEY, inputText, fromLanguage + "-" + toLanguage), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        waitDialog.dismiss();
                        try {
                            JSONObject result = new JSONObject(response);
                            String language = result.getString("lang");
                            String fromLanguge = language.split("-")[0];
                            String toLanguage = language.split("-")[1];
                            fromSpn.setSelection(LanguageHelper.getIndex(fromLanguge));
                            toSpn.setSelection(LanguageHelper.getIndex(toLanguage));
                            JSONArray lines = result.getJSONArray("text");
                            StringBuilder buf = new StringBuilder();
                            for (int i = 0; i < lines.length(); i++) {
                                buf.append(lines.getString(i));
                            }
                            outputTV.setText(buf.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            toast(e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        waitDialog.dismiss();
                        toast(error.getMessage());
                    }
                }));
            }
        });

        inputCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult pickResult) {
                        Throwable error = pickResult.getError();
                        if (error == null) {
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(pickResult.getBitmap());
                            FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText result) {
                                    StringBuilder buf = new StringBuilder();
                                    for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                                        for (FirebaseVisionText.Line line : block.getLines())
                                            buf.append(line.getText()).append("\n");
                                    }
                                    inputED.setText(buf);
                                }
                            });
                        } else {
                            toast(error.getMessage());
                        }

                    }
                }).show(MainActivity.this);
            }
        });

    }

    private void askForPermission() {
        PermissionUtil.with(this).request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, Manifest.permission.INTERNET).onAllGranted(new Func() {
            @Override
            protected void call() {

            }
        }).ask(134);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
