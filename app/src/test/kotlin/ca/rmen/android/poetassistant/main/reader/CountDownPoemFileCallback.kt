/*
 * Copyright (c) 2017 Carmen Alvarez
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

import android.print.PrintJob
import androidx.annotation.NonNull
import org.junit.Assert.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CountDownPoemFileCallback : PoemFileCallback {

    private val latch = CountDownLatch(1)
    var poemFile: PoemFile? = null
    var printJob: PrintJob? = null

    override fun onPrintJobCreated(@NonNull poemFile: PoemFile, printJob: PrintJob?) {
        latch.countDown()
        this.poemFile = poemFile
        this.printJob = printJob
    }

    fun await() {
        try {
            latch.await(5, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            fail("Timeout waiting for callback")
        }
    }
}
