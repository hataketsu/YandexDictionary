package com.worker.yandexdictionary;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

public class BaseCustomActivity extends AppCompatActivity {
    public ProgressDialog waitDialog;
    public final String API_KEY = "trnsl.1.1.20181026T162625Z.b5c819a79d765111.4bc11bec1128eb4473dd11d0feafdfcb1525296b";
    public final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key={0}&text={1}&lang={2}&format=plain";
    protected Spinner toSpn;
    protected Spinner fromSpn;
    public RequestQueue queue;
    public SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        init();
    }

    protected void initViews() {
        fromSpn = findViewById(R.id.fromSpn);
        toSpn = findViewById(R.id.toSpn);

    }

    @Override
    protected void onResume() {
        super.onResume();

        fromSpn.setSelection(preferences.getInt("from_language", 0));
        toSpn.setSelection(preferences.getInt("to_language", 0));
    }

    private void init() {
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle("Please wait");
        waitDialog.setMessage("Translating...");
        waitDialog.setCancelable(false);
    }


    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void translate(String text) {
        Log.d("TRANSLATION", MessageFormat.format("translate: {0}", text));
        String fromLanguage = LanguageHelper.LANGUAGES_CODE[fromSpn.getSelectedItemPosition()];
        String toLanguage = LanguageHelper.LANGUAGES_CODE[toSpn.getSelectedItemPosition()];
        waitDialog.show();
        String language_arg;
        if (preferences.getBoolean("detect_language", false))
            language_arg = toLanguage;
        else
            language_arg = fromLanguage + "-" + toLanguage;

        queue.add(new StringRequest(MessageFormat.format(BASE_URL, API_KEY, text, language_arg), new Response.Listener<String>() {
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
                    afterTranslate(buf.toString());
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

    protected void afterTranslate(String result) {

    }
}
