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

package ca.rmen.android.poetassistant

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.net.Uri
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.util.Log
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Locale

class Favorites constructor(private val favoriteDao: FavoriteDao) {
    companion object {
        private val TAG = Constants.TAG + Favorites::class.java.simpleName
    }

    fun getIsFavoriteLiveData(word: String): LiveData<Boolean> {
        return Transformations.map(favoriteDao.getCountLiveData(word), { count -> count > 0 })
    }

    fun getFavoritesLiveData(): LiveData<List<Favorite>> = favoriteDao.favoritesLiveData

    @WorkerThread
    fun getFavorites(): Set<String> {
        return favoriteDao.favorites.map(Favorite::getWord).toSet()
    }

    @WorkerThread
    @Throws(IOException::class)
    fun exportFavorites(context: Context, uri: Uri) {
        val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("Can't open null output stream")
        BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
            val favorites = getFavorites()
            favorites.forEach {
                writer.write(it)
                writer.newLine()
            }
        }
    }

    @MainThread
    fun saveFavorite(word: String, isFavorite: Boolean) {
        if (isFavorite) Schedulers.io().scheduleDirect({ favoriteDao.insert(Favorite(word)) })
        else removeFavorite(word)
    }

    @WorkerThread
    @Throws(IOException::class)
    fun importFavorites(context: Context, uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Can't open null input stream")
        val favorites = getFavorites().toMutableList()
        val favoritesToAdd = HashSet<Favorite>()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.forEachLine { line ->
                if (!TextUtils.isEmpty(line)) {
                    val favorite = line.trim().toLowerCase(Locale.getDefault())
                    if (!TextUtils.isEmpty(favorite) && !favorites.contains(favorite)) {
                        favorites.add(favorite)
                        favoritesToAdd.add(Favorite(favorite))
                    }
                }
            }
            favoriteDao.insertAll(favoritesToAdd)
        }
    }

    @MainThread
    private fun removeFavorite(favorite: String) {
        Log.v(TAG, "removeFavorite $favorite")
        Schedulers.io().scheduleDirect({ favoriteDao.delete(Favorite(favorite)) })
    }

    @MainThread
    fun clear() {
        Schedulers.io().scheduleDirect(favoriteDao::deleteAll)
    }
}
