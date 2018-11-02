package com.worker.yandexdictionary;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Locale;
//translate text
public class TextTranslatingActivity extends BaseTranslatingActivity {

    public static final int INPUT_BY_VOICE_REQUEST = 213;
    private Button translateBtn;
    private EditText inputED;
    private TextView outputTV;

    private ImageButton inputCaptureBtn;
    private ImageButton readInputBtn;
    private ImageButton readOutputBtn;
    private TextToSpeech textToSpeech;
    private ImageButton inputRecordBtn;
    private ImageButton spellCheckBtn;
    private ImageButton clearBtn;
    private SpellCheckerSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermission(); //ask for some needed permissions
        initViews(); //load all views
        TextServicesManager tsm =
                (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);
        session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, new SpellCheckerSession.SpellCheckerSessionListener() {
            @Override
            public void onGetSuggestions(SuggestionsInfo[] suggestionsInfos) {

            }

            @Override
            public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfos) {
                for (SentenceSuggestionsInfo info : sentenceSuggestionsInfos) {
                    for (int i = 0; i < info.getSuggestionsCount(); i++) {
                        SuggestionsInfo suggestionsInfo = info.getSuggestionsInfoAt(i);
                        for (int j = 0; j < suggestionsInfo.getSuggestionsCount(); j++) {
                            Log.d("SENTENCE", suggestionsInfo.getSuggestionAt(j));
                        }
                    }
                }
            }
        }, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech != null) { //we need to shutdown TTS
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            readInputBtn.setVisibility(View.GONE); //hide read buttons
            readOutputBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) { //respawn TTS
                textToSpeech.setLanguage(Locale.US);
                readInputBtn.setVisibility(View.VISIBLE); //show the read buttons
                readOutputBtn.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void initViews() {
        super.initViews();
        translateBtn = findViewById(R.id.translateBtn); //find all views
        inputCaptureBtn = findViewById(R.id.inputCaptureBtn);
        readInputBtn = findViewById(R.id.readInputBtn);
        readOutputBtn = findViewById(R.id.readOutputBtn);
        inputRecordBtn = findViewById(R.id.inputRecordBtn);
        spellCheckBtn = findViewById(R.id.spellCheckBtn);
        clearBtn = findViewById(R.id.clearBtn);
        readInputBtn.setVisibility(View.GONE);
        readOutputBtn.setVisibility(View.GONE);
        inputED = findViewById(R.id.inputED);
        outputTV = findViewById(R.id.outputTV);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputED.getText().toString().trim(); //read input and translate it
                translate(inputText);
            }
        });

        inputCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TextTranslatingActivity.this, ImageTranslatingActivity.class)); //start translating image
            }
        });
        readInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(inputED.getText().toString().trim(), TextToSpeech.QUEUE_FLUSH, null); //read out-loud the input text
            }
        });
        readOutputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(outputTV.getText().toString().trim(), TextToSpeech.QUEUE_FLUSH, null); //read out-loud the translated text
            }
        });
        inputRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        }); //input text from sound
        fromSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                preferences.edit().putInt("from_language", position).apply(); //save it when select new from language
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        toSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                preferences.edit().putInt("to_language", position).apply();//save it when select new to language
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spellCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.getSentenceSuggestions(new TextInfo[]{new TextInfo(inputED.getText().toString())}, 5);//TODO:finish this
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputED.setText("");
            }
        }); //clear input text
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INPUT_BY_VOICE_REQUEST: { //result of input by voice
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //get results
                    inputED.setText(result.get(0)); //get the best one only
                }
                break;
            }

        }
    }

    private void promptSpeechInput() { //start input text by voice
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //setup intent
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Input what you want to translate");
        try {
            startActivityForResult(intent, INPUT_BY_VOICE_REQUEST); //start recording
        } catch (Exception e) {
            e.printStackTrace();
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
    } //show the translated text to the textview
}
