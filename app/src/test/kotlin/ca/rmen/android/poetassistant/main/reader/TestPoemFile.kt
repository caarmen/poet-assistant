/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.reader

import android.net.Uri
import android.util.Log
import android.webkit.WebView
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Environment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class TestPoemFile {
    companion object {
        private val TAG = Constants.TAG + TestPoemFile::class.java.simpleName
    }

    @get:Rule
    val hiltTestRule = HiltAndroidRule(this)

    private lateinit var mPoemFile: File
    private lateinit var mPoemUri: Uri

    @Before
    fun setup() {
        mPoemFile = File(Environment.getApplication().filesDir, "test-poem.txt")
        if (mPoemFile.exists()) {
            assertTrue(mPoemFile.delete())
            assertFalse(mPoemFile.exists())
        }
        mPoemUri = Uri.fromFile(mPoemFile)
    }

    @Test
    fun testGenerateFileName() {
        assertNull(PoemFile.generateFileName(""))
        assertNull(PoemFile.generateFileName("& 2 !,$*-)°"))
        assertEquals("Unthrifty.txt", PoemFile.generateFileName("Unthrifty loveliness, why dost thou spend"))
        assertEquals("Against-my-love.txt", PoemFile.generateFileName("Against my love shall be as I am now,"))
        assertEquals("As-a-decrepit.txt", PoemFile.generateFileName("As a decrepit father takes delight"))
        assertEquals("Canst-thou-O.txt", PoemFile.generateFileName("Canst thou, O cruel! say I love thee not,"))
        assertEquals("Farewell-thou.txt", PoemFile.generateFileName("Farewell! thou art too dear for my possessing,"))
        assertEquals("Lo-in-the.txt", PoemFile.generateFileName("Lo! in the orient when the gracious light"))
        assertEquals("Roses-are-red.txt", PoemFile.generateFileName("Roses are red,\nviolets are blue"))
        assertEquals("Róses-àré-réd.txt", PoemFile.generateFileName("Róses àré réd,\nvïólèts áré blüë"))
        assertEquals("Short.txt", PoemFile.generateFileName("Short"))
        assertEquals("abcdefgh.txt", PoemFile.generateFileName("abcdefgh"))
        assertEquals("abcdefghi.txt", PoemFile.generateFileName("abcdefghi"))
        assertEquals("Short-poem.txt", PoemFile.generateFileName("Short poem"))
        assertEquals("Short-poem.txt", PoemFile.generateFileName("Short poem"))
        assertEquals("leading-symbols.txt", PoemFile.generateFileName(",! leading symbols"))
    }

    @Test
    @Config(sdk = [30]) // TODO investigate why this doesn't work starting from 31
    fun testSave() {
        val text = "Roses are red\n"
        val callback = CountDownPoemFileCallback()
        PoemFile.save(Environment.getApplication(), mPoemUri, text, callback)
        callback.await()
        assertTrue(mPoemFile.exists())
        val poemFile = callback.poemFile
        assertNotNull(poemFile)
        assertEquals(text, poemFile.text)
        assertEquals(mPoemUri, poemFile.uri)
        assertNull(poemFile.name)
    }

    @Test
    fun testSaveError() {
        val text = "Violets are blue\n"
        val callback = CountDownPoemFileCallback()
        val uri = Uri.parse("file:///invalid/folder/poem.txt")
        PoemFile.save(Environment.getApplication(), uri, text, callback)
        callback.await()
        assertTrue(callback.wasCalled())
        assertNull(callback.poemFile)
    }

    @Test
    fun testOpenNoFile() {
        val callback = CountDownPoemFileCallback()
        Shadows.shadowOf(Environment.getApplication().contentResolver).registerInputStream(mPoemUri, object : InputStream() {
            override fun read() : Int {
                throw IOException("nothing here")
            }
        })
        PoemFile.open(Environment.getApplication(), mPoemUri, callback)
        callback.await()
        assertTrue(callback.wasCalled())
        assertNull(callback.poemFile)
    }

    @Test
    fun testOpen() {
        val text = "If you are a poet\n"
        val os = FileOutputStream(mPoemFile)
        os.write(text.toByteArray())
        os.close()

        val callback = CountDownPoemFileCallback()
        Shadows.shadowOf(Environment.getApplication().contentResolver).registerInputStream(mPoemUri, FileInputStream(mPoemFile))
        PoemFile.open(Environment.getApplication(), mPoemUri, callback)
        callback.await()
        val poemFile = callback.poemFile
        assertNotNull(poemFile)
        assertEquals(text, poemFile.text)
        assertEquals(mPoemUri, poemFile.uri)
        assertNull(poemFile.name)
    }

    @Test
    @Ignore
    fun testPrint() {
        val callback = CountDownPoemFileCallback()
        val text = "This app is for you\n"
        val title = "My Poem"
        var poemFile = PoemFile(null, title, text)

        // We need to shadow the webview from tests, otherwise onPageFinished() is never called
        val webView = WebView(Environment.getApplication())
        val shadowWebView = Shadows.shadowOf(webView)
        PoemFile.print(Environment.getApplication(), webView, poemFile, callback)
        shadowWebView.webViewClient.onPageFinished(webView, "http://example.com")

        callback.await()
        poemFile = callback.poemFile
        assertNotNull(poemFile)
        assertEquals(text, poemFile.text)
        assertEquals(title, poemFile.name)
        assertNull(poemFile.uri)

        val printJob = callback.printJob
        Log.v(TAG, "printJob = $printJob")
        /* From robolectric, no print job is actually created :(
        assertNotNull(printJob)
        */

    }

}
