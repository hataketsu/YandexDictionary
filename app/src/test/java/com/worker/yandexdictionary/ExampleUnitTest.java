package com.worker.yandexdictionary;

import android.icu.text.MeasureFormat;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    public final String API_KEY = "trnsl.1.1.20181026T162625Z.b5c819a79d765111.4bc11bec1128eb4473dd11d0feafdfcb1525296b";
    public final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key={0}&text={1}&lang={2}&format=plain";

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_format() {
        System.out.print(MessageFormat.format(BASE_URL,API_KEY,"hello","vi"));
    }
    @Test
    public void test_split() {
        System.out.print(LanguageHelper.NAME_TO_CODE.get("English"));
    }
}