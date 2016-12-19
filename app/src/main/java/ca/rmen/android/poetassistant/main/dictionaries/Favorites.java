/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.content.Context;
import android.support.annotation.WorkerThread;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.async.AsyncSession;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.DaggerHelper;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class Favorites {

    @Inject
    DaoSession mDaoSession;

    /**
     * Subscribe to this using EventBus to know when favorites are changed.
     */
    static class OnFavoritesChanged {
        OnFavoritesChanged() {
        }
    }

    public Favorites(Context context) {
        DaggerHelper.getAppComponent(context).inject(this);
    }

    @WorkerThread
    public Set<String> getFavorites() {
        List<Favorite> favorites = mDaoSession.getFavoriteDao().loadAll();
        return StreamSupport.stream(favorites).map(Favorite::getWord).collect(Collectors.toSet());
    }

    public void addFavorite(String favorite) {
        AsyncSession asyncSession = mDaoSession.startAsyncSession();
        asyncSession.setListener(operation -> notifyChanged());
        asyncSession.insert(new Favorite(favorite));
    }

    public void removeFavorite(String favorite) {
        AsyncSession asyncSession = mDaoSession.startAsyncSession();
        asyncSession.setListener(operation -> notifyChanged());
        asyncSession.runInTx(() -> {
            mDaoSession
                    .getFavoriteDao()
                    .queryBuilder()
                    .where(FavoriteDao.Properties.Word.eq(favorite))
                    .buildDelete()
                    .executeDeleteWithoutDetachingEntities();
            mDaoSession.clear();
        });
    }

    void clear() {
        AsyncSession asyncSession = mDaoSession.startAsyncSession();
        asyncSession.setListener(operation -> notifyChanged());
        asyncSession.deleteAll(FavoriteDao.class);
    }

    private void notifyChanged() {
        EventBus.getDefault().post(new OnFavoritesChanged());
    }

}
