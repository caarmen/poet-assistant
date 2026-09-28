/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.di

import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.EmbeddedDbThesaurusRepository
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.ThesaurusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for thesaurus bindings.
 * Binds the ThesaurusRepository interface to its implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ThesaurusModule {

    @Binds
    @Singleton
    abstract fun bindThesaurusRepository(
        impl: EmbeddedDbThesaurusRepository
    ): ThesaurusRepository
}
