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
package ca.rmen.android.poetassistant.main.reader;

import android.print.PrintJob;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

class CountDownPoemFileCallback implements PoemFileCallback {

    private final CountDownLatch mLatch = new CountDownLatch(1);
    private PoemFile mPoemFile;
    private PrintJob mPrintJob;

    @Override
    public void onPoemLoaded(PoemFile poemFile) {
        mLatch.countDown();
        mPoemFile = poemFile;
    }

    @Override
    public void onPoemSaved(PoemFile poemFile) {
        mLatch.countDown();
        mPoemFile = poemFile;
    }

    @Override
    public void onPrintJobCreated(@NonNull PoemFile poemFile, PrintJob printJob) {
        mLatch.countDown();
        mPoemFile = poemFile;
        mPrintJob = printJob;
    }

    PoemFile getPoemFile() {
        return mPoemFile;
    }

    PrintJob getPrintJob() {
        return mPrintJob;
    }

    void await() {
        try {
            mLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Timeout waiting for callback");
        }
    }

    boolean wasCalled() {
        return mLatch.getCount() == 0;
    }
}
