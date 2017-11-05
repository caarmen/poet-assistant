/*
 * Copyright (c) 2016-2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.reader;

import android.net.Uri;
import android.print.PrintJob;
import android.util.Log;
import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.android.poetassistant.BuildConfig;
import ca.rmen.android.poetassistant.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class TestPoemFile {

    private static final String TAG = Constants.TAG + TestPoemFile.class.getSimpleName();
    private File mPoemFile;
    private Uri mPoemUri;

    @Before
    public void setup() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> AndroidSchedulers.mainThread());
        mPoemFile = new File(RuntimeEnvironment.application.getFilesDir(), "test-poem.txt");
        if (mPoemFile.exists()) {
            assertTrue(mPoemFile.delete());
            assertFalse(mPoemFile.exists());
        }
        mPoemUri = Uri.fromFile(mPoemFile);
    }

    @Test
    public void testGenerateFileName() {
        assertNull(PoemFile.Companion.generateFileName(""));
        assertNull(PoemFile.Companion.generateFileName("& 2 !,$*-)°"));
        assertEquals("Unthrifty.txt", PoemFile.Companion.generateFileName("Unthrifty loveliness, why dost thou spend"));
        assertEquals("Against-my-love.txt", PoemFile.Companion.generateFileName("Against my love shall be as I am now,"));
        assertEquals("As-a-decrepit.txt", PoemFile.Companion.generateFileName("As a decrepit father takes delight"));
        assertEquals("Canst-thou-O.txt", PoemFile.Companion.generateFileName("Canst thou, O cruel! say I love thee not,"));
        assertEquals("Farewell-thou.txt", PoemFile.Companion.generateFileName("Farewell! thou art too dear for my possessing,"));
        assertEquals("Lo-in-the.txt", PoemFile.Companion.generateFileName("Lo! in the orient when the gracious light"));
        assertEquals("Roses-are-red.txt", PoemFile.Companion.generateFileName("Roses are red,\nviolets are blue"));
        assertEquals("Róses-àré-réd.txt", PoemFile.Companion.generateFileName("Róses àré réd,\nvïólèts áré blüë"));
        assertEquals("Short.txt", PoemFile.Companion.generateFileName("Short"));
        assertEquals("abcdefgh.txt", PoemFile.Companion.generateFileName("abcdefgh"));
        assertEquals("abcdefghi.txt", PoemFile.Companion.generateFileName("abcdefghi"));
        assertEquals("Short-poem.txt", PoemFile.Companion.generateFileName("Short poem"));
        assertEquals("Short-poem.txt", PoemFile.Companion.generateFileName("Short poem"));
        assertEquals("leading-symbols.txt", PoemFile.Companion.generateFileName(",! leading symbols"));
    }

    @Test
    public void testSave() throws FileNotFoundException {
        String text = "Roses are red\n";
        CountDownPoemFileCallback callback = new CountDownPoemFileCallback();
        PoemFile.Companion.save(RuntimeEnvironment.application, mPoemUri, text, callback);
        callback.await();
        assertTrue(mPoemFile.exists());
        PoemFile poemFile = callback.getPoemFile();
        assertNotNull(poemFile);
        assertEquals(text, poemFile.text);
        assertEquals(mPoemUri, poemFile.uri);
        assertNull(poemFile.name);
    }

    @Test
    public void testSaveError() throws FileNotFoundException {
        String text = "Violets are blue\n";
        CountDownPoemFileCallback callback = new CountDownPoemFileCallback();
        Uri uri = Uri.parse("file:///invalid/folder/poem.txt");
        PoemFile.Companion.save(RuntimeEnvironment.application, uri, text, callback);
        callback.await();
        assertTrue(callback.wasCalled());
        PoemFile poemFile = callback.getPoemFile();
        assertNull(poemFile);
    }

    @Test
    public void testOpenNoFile() throws IOException {
        CountDownPoemFileCallback callback = new CountDownPoemFileCallback();
        Shadows.shadowOf(RuntimeEnvironment.application.getContentResolver()).registerInputStream(mPoemUri, new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("nothing here");
            }
        });
        PoemFile.Companion.open(RuntimeEnvironment.application, mPoemUri, callback);
        callback.await();
        PoemFile poemFile = callback.getPoemFile();
        assertTrue(callback.wasCalled());
        assertNull(poemFile);
    }

    @Test
    public void testOpen() throws IOException {
        String text = "If you are a poet\n";
        FileOutputStream os = new FileOutputStream(mPoemFile);
        os.write(text.getBytes());
        os.close();

        CountDownPoemFileCallback callback = new CountDownPoemFileCallback();
        Shadows.shadowOf(RuntimeEnvironment.application.getContentResolver()).registerInputStream(mPoemUri, new FileInputStream(mPoemFile));
        PoemFile.Companion.open(RuntimeEnvironment.application, mPoemUri, callback);
        callback.await();
        PoemFile poemFile = callback.getPoemFile();
        assertNotNull(poemFile);
        assertEquals(text, poemFile.text);
        assertEquals(mPoemUri, poemFile.uri);
        assertNull(poemFile.name);
    }

    @Test
    public void testPrint() {
        CountDownPoemFileCallback callback = new CountDownPoemFileCallback();
        String text = "This app is for you\n";
        String title = "My Poem";
        PoemFile poemFile = new PoemFile(null, title, text);

        // We need to shadow the webview from tests, otherwise onPageFinished() is never called
        WebView webView = new WebView(RuntimeEnvironment.application);
        ShadowWebView shadowWebView = Shadows.shadowOf(webView);
        PoemFile.Companion.print(RuntimeEnvironment.application, webView, poemFile, callback);
        shadowWebView.getWebViewClient().onPageFinished(webView, "http://example.com");

        callback.await();
        poemFile = callback.getPoemFile();
        assertNotNull(poemFile);
        assertEquals(text, poemFile.text);
        assertEquals(title, poemFile.name);
        assertNull(poemFile.uri);

        PrintJob printJob = callback.getPrintJob();
        Log.v(TAG, "printJob = " + printJob);
        /* From robolectric, no print job is actually created :(
        assertNotNull(printJob);
        */
    }

}
