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

import java.net.URLEncoder;
import java.text.MessageFormat;

//base class for translating activities
public class BaseTranslatingActivity extends AppCompatActivity {
    public ProgressDialog waitDialog; //the dialog display when running long task
    public final String API_KEY = "trnsl.1.1.20181026T162625Z.b5c819a79d765111.4bc11bec1128eb4473dd11d0feafdfcb1525296b"; //Yandex Translating develop API
    public final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key={0}&text={1}&lang={2}&format=plain"; //Yandex RESTful HTTPS API end-point
    protected Spinner toSpn;    //spinner for selecting the language to translate to
    protected Spinner fromSpn;  //spinner for selecting the language to translate from
    public RequestQueue queue;  //Volley network request queue
    public SharedPreferences preferences;   //settings preference

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this); //get default settings
    }

    protected void initViews() {
        fromSpn = findViewById(R.id.fromSpn);
        toSpn = findViewById(R.id.toSpn);

        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle("Please wait");
        waitDialog.setMessage("Translating...");
        waitDialog.setCancelable(false); //don't let user cancel this dialog
    }

    @Override
    protected void onResume() {
        super.onResume();

        fromSpn.setSelection(preferences.getInt("from_language", 0)); //read previous languages setting
        toSpn.setSelection(preferences.getInt("to_language", 0));
    }


    public void toast(String text) { //show a short text at bottom of the screen
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    //start translating text
    protected void translate(String text) {
        text = URLEncoder.encode(text.trim()); //encode input text to URL format
        Log.d("TRANSLATION", MessageFormat.format("translate: {0}", text));
        String fromLanguage = LanguageHelper.LANGUAGES_CODE[fromSpn.getSelectedItemPosition()]; //read selected languages from spinner
        String toLanguage = LanguageHelper.LANGUAGES_CODE[toSpn.getSelectedItemPosition()];
        waitDialog.show(); //show the waiting dialog
        String language_arg;
        if (preferences.getBoolean("detect_language", false)) //if you want to detect input language automatically
            language_arg = toLanguage; //eg: vi to translate to Vietnamese
        else
            language_arg = fromLanguage + "-" + toLanguage; //eg: en-vi to translate from English to Vietnamese

        String url = MessageFormat.format(BASE_URL, API_KEY, text, language_arg); //combine the final URL
        queue.add(new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) { //add the url request to queue
                waitDialog.dismiss(); //we get the result so close the waiting dialog
                try { //maybe the result is malformed so we need to try
                    JSONObject result = new JSONObject(response); //convert to json object
                    String language = result.getString("lang"); //get language pair, eg: en-vi
                    String fromLanguge = language.split("-")[0]; //extract languages
                    String toLanguage = language.split("-")[1];
                    fromSpn.setSelection(LanguageHelper.getIndex(fromLanguge)); //change the spinner to translated languages
                    toSpn.setSelection(LanguageHelper.getIndex(toLanguage));
                    JSONArray lines = result.getJSONArray("text"); //the actual translated text, it is a array of lines of text
                    StringBuilder buf = new StringBuilder(); // so we need to join them into one string
                    for (int i = 0; i < lines.length(); i++) {
                        buf.append(lines.getString(i));
                    }
                    afterTranslate(buf.toString()); //show the result
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { //something when wrong, may be malformed json, network error...
                waitDialog.dismiss();
                error.printStackTrace();
                toast(error.getMessage());
            }
        }));
    }

    //the way we treat the translated text will be defined in child class
    protected void afterTranslate(String result) {

    }
}
