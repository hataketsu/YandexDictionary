package com.worker.yandexdictionary;

import java.util.HashMap;

public class LanguageHelper {
    public static final String[] LANGUAGES_NAME = {"Azerbaijan","Albanian","Amharic","English","Arabic","Armenian","Afrikaans","Basque","Bashkir","Belarusian","Bengali","Burmese","Bulgarian","Bosnian","Welsh","Hungarian","Vietnamese","Haitian","Galician","Dutch","Hill Mari","Greek","Georgian","Gujarati","Danish","Hebrew","Yiddish","Indonesian","Irish","Italian","Icelandic","Spanish","Kazakh","Kannada","Catalan","Kyrgyz","Chinese","Korean","Xhosa","Khmer","Laotian","Latin","Latvian","Lithuanian","Luxembourgish","Malagasy","Malay","Malayalam","Maltese","Macedonian","Maori","Marathi","Mari","Mongolian","German","Nepali","Norwegian","Punjabi","Papiamento","Persian","Polish","Portuguese","Romanian","Russian","Cebuano","Serbian","Sinhala","Slovakian","Slovenian","Swahili","Sundanese","Tajik","Thai","Tagalog","Tamil","Tatar","Telugu","Turkish","Udmurt","Uzbek","Ukrainian","Urdu","Finnish","French","Hindi","Croatian","Czech","Swedish","Scottish","Estonian","Esperanto","Javanese","Japanese"};
    public static final String[] LANGUAGES_CODE = {"az", "sq", "am", "en", "ar", "hy", "af", "eu", "ba", "be", "bn", "my", "bg", "bs", "cy", "hu", "vi", "ht", "gl", "nl", "mrj", "el", "ka", "gu", "da", "he", "yi", "id", "ga", "it", "is", "es", "kk", "kn", "ca", "ky", "zh", "ko", "xh", "km", "lo", "la", "lv", "lt", "lb", "mg", "ms", "ml", "mt", "mk", "mi", "mr", "mhr", "mn", "de", "ne", "no", "pa", "pap", "fa", "pl", "pt", "ro", "ru", "ceb", "sr", "si", "sk", "sl", "sw", "su", "tg", "th", "tl", "ta", "tt", "te", "tr", "udm", "uz", "uk", "ur", "fi", "fr", "hi", "hr", "cs", "sv", "gd", "et", "eo", "jv", "ja"};
    public static final HashMap<String, String> NAME_TO_CODE = new HashMap<>();
    public static final HashMap<String, String> CODE_TO_NAME = new HashMap<>();

    static {
        for (int i = 0; i < LANGUAGES_NAME.length; i++) {
            NAME_TO_CODE.put(LANGUAGES_NAME[i], LANGUAGES_CODE[i]);
            CODE_TO_NAME.put(LANGUAGES_CODE[i], LANGUAGES_NAME[i]);
        }
    }

    public static int getIndex(String code) {
        for (int i = 0; i < LANGUAGES_CODE.length; i++) {
            if (LANGUAGES_CODE[i].equals(code)) {
                return i;
            }
        }
        return -1;
    }
}
