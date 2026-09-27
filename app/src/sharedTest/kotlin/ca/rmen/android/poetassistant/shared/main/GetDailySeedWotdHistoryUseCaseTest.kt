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

package ca.rmen.android.poetassistant.shared.main

import android.app.Application
import android.content.Context
import android.text.format.DateUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.rules.ActivityTestRules
import ca.rmen.android.poetassistant.main.wotd.usecases.GetDailySeedWotdHistoryUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class GetDailySeedWotdHistoryUseCaseTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var context: Context

    private lateinit var useCase: GetDailySeedWotdHistoryUseCase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityTestRules.beforeActivityLaunched(context)
        hiltTestRule.inject()
        useCase = GetDailySeedWotdHistoryUseCase(
            dictionaryRepository = dictionaryRepository,
            application = context.applicationContext as Application,
        )
    }

    @After
    fun tearDown() {
        ActivityTestRules.afterActivityFinished(context)
    }

    /**
     * Given the dictionary,
     * When the history is generated for the days ending at 2018-11-24,
     * Then the words match the recorded words of the day.
     * The recorded words were verified in the iOS app, which took them from
     * the Android wotd feature in 2018. The dictionary content is unchanged
     * since then, so the words are still valid.
     * And the first date is the given day, displayed in the default timezone.
     * And the given calendar is not modified.
     */
    @Test
    fun testRecordedPicks() = runBlocking {
        val anchorDate = recordedHistoryAnchorDate()
        val anchorTimeInMillis = anchorDate.timeInMillis
        val entries = useCase(emptySet(), anchorDate)
        assertEquals(RECORDED_WORDS.size, entries.size)
        RECORDED_WORDS.forEachIndexed { index, expectedWord ->
            assertEquals("Word for day $index", expectedWord, entries[index].word)
        }
        val expectedFirstDate = DateUtils.formatDateTime(
            context,
            anchorTimeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL
        )
        assertEquals(expectedFirstDate, entries[0].date)
        // The given calendar is not modified.
        assertEquals(anchorTimeInMillis, anchorDate.timeInMillis)
        assertEquals("UTC", anchorDate.timeZone.id)
    }

    /**
     * Given a fixed date,
     * When the history is generated twice,
     * Then both calls return the same list.
     */
    @Test
    fun testDeterminism() = runBlocking {
        val first = useCase(setOf("vaccinate"), recordedHistoryAnchorDate())
        val second = useCase(setOf("vaccinate"), recordedHistoryAnchorDate())
        assertEquals(first, second)
    }

    /**
     * Given favorite words, one of which appears on two days of the history,
     * When the history is generated,
     * Then the entries with favorite words are marked as favorites,
     * And the entries with other words are not.
     */
    @Test
    fun testFavorites() = runBlocking {
        // "territorially" appears on two days of the recorded history:
        // favoriting the word must flag both entries.
        val favoriteWords = setOf("vaccinate", "territorially")
        val entries = useCase(favoriteWords, recordedHistoryAnchorDate())
        assertTrue(entries.all { entry -> entry.isFavorite == favoriteWords.contains(entry.word) })
        assertEquals(2, entries.count { it.word == "territorially" && it.isFavorite })
    }

    private fun recordedHistoryAnchorDate(): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2018, Calendar.NOVEMBER, 24, 0, 0, 0)
        calendar[Calendar.MILLISECOND] = 0
        return calendar
    }

    companion object {

        /**
         * The recorded words of the day for the days ending at 2018-11-24,
         * oldest last. Each word was recorded from the Android app in 2018
         * and verified in the iOS app.
         */
        private val RECORDED_WORDS = listOf(
            "vaccinate", "devaluation", "copulation", "fuselage", "lyricist",
            "emphysema", "auscultation", "hypotonia", "brigadier", "gallstone",
            "unspoilt", "sprayer", "tufa", "braised", "unselfishly",
            "economise", "navel", "rhombus", "swinish", "offshoot",
            "personation", "sorrowing", "leasehold", "blowout", "dropout",
            "interrogatively", "indelible", "sprit", "semiotic", "normalisation",
            "overladen", "deist", "silviculture", "heathenism", "divisive",
            "unripe", "soviet", "napped", "minder", "heighten",
            "karat", "phosphorous", "firearm", "hypotenuse", "boldface",
            "recapitulate", "alchemist", "abrogate", "binnacle", "unimpressive",
            "insanitary", "friday", "ancestress", "badminton", "bioremediation",
            "servo", "mobilisation", "taproot", "relinquishing", "creosote",
            "autograph", "catechetical", "jib", "protraction", "ambit",
            "panchayat", "deb", "territorially", "hart", "downtrodden",
            "prolapse", "metaphysically", "substratum", "adroitly", "isi",
            "yardarm", "pullout", "computationally", "schoolyard", "advisedly",
            "maxillofacial", "belike", "storyteller", "blip", "colorist",
            "scythe", "timbered", "overspread", "succinct", "masochistic",
            "maltreatment", "dampen", "bichromate", "subluxation", "prodigiously",
            "superficiality", "territorially", "unconstrained", "literati", "dispossessed",
        )
    }
}
