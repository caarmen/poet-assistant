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

package ca.rmen.android.poetassistant

interface Threading {

    /**
     * Run the given task on the ui thread
     */
    fun executeForeground(body: () -> Unit)

    /**
     * Run the given background task on a background thread. If a foreground task is specified, it will be
     * called on the UI thread with the successful result of the background task. If the background
     * task throws a Throwable and the error task is specified, the error task will be called with the
     * throwable, on the UI thread.
     */
    fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)? = null, errorTask: ((Throwable) -> Unit)? = null)
}