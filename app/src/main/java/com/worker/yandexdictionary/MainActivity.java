package com.worker.yandexdictionary;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends BaseCustomActivity {

    private Button translateBtn;
    private EditText inputED;
    private TextView outputTV;

    private ImageButton inputCaptureBtn;
    private ImageButton readInputBtn;
    private ImageButton readOutputBtn;
    private TextToSpeech textToSpeech;
    private View inputRecordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermission();
        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            readInputBtn.setVisibility(View.GONE);
            readOutputBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
                readInputBtn.setVisibility(View.VISIBLE);
                readOutputBtn.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void initViews() {
        super.initViews();
        translateBtn = findViewById(R.id.translateBtn);
        inputCaptureBtn = findViewById(R.id.inputCaptureBtn);
        readInputBtn = findViewById(R.id.readInputBtn);
        readOutputBtn = findViewById(R.id.readOutputBtn);
        inputRecordBtn = findViewById(R.id.inputRecordBtn);
        readInputBtn.setVisibility(View.GONE);
        readOutputBtn.setVisibility(View.GONE);
        inputED = findViewById(R.id.inputED);
        outputTV = findViewById(R.id.outputTV);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputED.getText().toString().trim();
                translate(inputText);
            }
        });

        inputCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TranslateImageActivity.class));
            }
        });
        readInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(inputED.getText().toString().trim(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        readOutputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(outputTV.getText().toString().trim(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        inputRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
        fromSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                preferences.edit().putInt("from_language", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        toSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                preferences.edit().putInt("to_language", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 213: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputED.setText(result.get(0));
                }
                break;
            }

        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Input what you want to translate");
        try {
            startActivityForResult(intent, 213);
        } catch (Exception e) {
            toast(e.getMessage());
        }
    }

    private void askForPermission() {
        PermissionUtil.with(this).request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, Manifest.permission.INTERNET).onAllGranted(new Func() {
            @Override
            protected void call() {

            }
        }).ask(134);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void afterTranslate(String result) {
        outputTV.setText(result);
    }
}
