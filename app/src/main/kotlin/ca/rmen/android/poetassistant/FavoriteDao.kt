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

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM FAVORITE")
    fun getFavorites(): Array<Favorite>

    @Query("SELECT * FROM FAVORITE")
    fun getFavoritesLiveData(): LiveData<List<Favorite>>

    @Query("SELECT COUNT(*) FROM FAVORITE WHERE WORD = :word")
    fun getCountLiveData(word: String): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(favorites: Array<Favorite>)

    @Delete
    fun delete(word: Favorite)

    @Query("DELETE FROM FAVORITE")
    fun deleteAll()
}
