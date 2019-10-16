Change Log
==========

1.30.1  *(2019-10-16)*
--------------------
Update environment
* compile and target api level 29
* gradle: 5.5.1 -> 5.6.2
* android gradle plugin: 3.4.2 -> 3.5.1
* jacoco: 0.8.4 - 0.8.5
* ben-manes: 0.22.0 -> 0.27.0
* robolectric: 4.3 -> 4.3.1
* kotlin: 1.3.41 -> 1.3.50
* kotlin coroutines: 1.2.2 -> 1.3.2
* androidx:
  - appcompat: 1.0.2 -> 1.1.0
  - lifecycle: 2.0.0 -> 2.1.0
  - preference: 1.0.0 -> 1.1.0
  - room: 2.1.0 -> 2.2.0

1.30.0  *(2019-08-07)*
--------------------
* Update build environment:
  - gradle: 5.4 -> 5.5.1
  - agp: 3.4.0 -> 3.4.2
  - ben-manes: 0.21.0 -> 0.22.0
  - kotlin: 1.3.30 -> 1.3.41
  - kotlin coroutines: 1.2.0 +> 1.2.2
  - room: 2.0.0 -> 2.1.0
  - dqgger: 2.22.1 -> 2.24
  - jacoco: 0.8.3 -> 0.8.4
  - roboctric: 4.2.1 -> 4.3
  - androidx test runner/rules: 1.1.1 -> 1.2.0
  - androidx test core: 1.1.0 -> 1.2.0
  - androidx junit: 1.1.0 -> 1.1.1
  - espresso: 3.1.1 -> 3.2.0

1.29.2  *(2019-04-22)*
--------------------
* Catch `Throwable`s thrown by a background task.

1.29.1  *(2019-04-21)*
--------------------
* Update build environment:
  - agp: 3.3.0 -> 3.4.0
  - kotlin: 1.3.20 -> 1.3.30
  - kotlin coroutines: 1.1.1 -> 1.2.0
  - dagger: 2.21 -> 2.22.1
  - robolectric: 4.1 -> 4.2.1
  - ben manes: 0.20.0 -> 0.21.0
  - dexcount: 0.8.5 -> 0.8.6
* Fix issue #164: make autobackup work for settings and the user db

1.29.0  *(2019-02-03)*
--------------------
* Update build environment:
  - agp: 3.2.1 -> 3.3.0
  - jacoco: 0.8.2 -> 0.8.3
  - dexcount: 0.8.4 -> 0.8.5
  - kotlin: 1.3.11 -> 1.3.20
  - kotlin coroutines: 1.1.0 -> 1.1.1
  - dagger: 2.20 -> 2.21
  - leak-canary: 1.6.2 -> 1.6.3
* Improve audio export: show the share intent directly (bypassing the notification) if the app is visible
* Fix Android Studio code inspection issues

1.28.0  *(2018-12-23)*
--------------------
* Update to androidx
* Remove deprecated code and resources which were relevant only for KitKat and older
* Rename the "Reader" tab to "Composer" and change the icon to a pencil icon
* Fix issues with the dark theme on newer devices (Oreo+)

1.27.1  *(2018-11-11)*
--------------------
* Correct margins in the settings screen.

1.27.0  *(2018-11-10)*
--------------------
* Drop support for KitKat and JellyBean. The minSdkVersion is now 21 (Lollipop).
* Added icons to the settings screen.

1.26.8  *(2018-11-10)*
--------------------
* Identical to 1.26.6. This is a rollback a bad 1.26.7 build which was pushed to beta.

1.26.7  *(2018-11-10)*
--------------------
* Update build environment.

1.26.6  *(2018-07-04)*
--------------------
* Update build environment.
* Issue #148: Add adaptive launcher icon.

1.26.5  *(2018-05-01)*
--------------------
* Update build environment.
  - agp: 3.1.0 -> 3.1.2
  - kotlin: 1.2.31 -> 1.2.40
  - constraint layout: 1.0.2 -> 1.1.0
  - test runner: 1.0.2-alpha1 -> 1.0.2
  - espresso: 3.0.1 -> 3.0.2
* Use ListAdapter instead of RecyclerView.Adapter

1.26.4  *(2018-04-07)*
--------------------
* Update build environment.
  - jacoco: 0.8.0 -> 0.8.1
  - robolectric: 3.6.1 -> 3.8
  - arch components: 1.1.0 -> 1.1.1
  - dagger: 2.14.1 -> 2.15
  - agp: 3.0.1 -> 3.1.0
  - kotlin: 1.2.21 -> 1.2.31
  - coroutines: 0.22.1 -> 0.22.5
  - support lib: 27.0.2 -> 27.1.1
* Fix at least one case of issue 140, where the player only read empty text.

1.26.3  *(2018-01-31)*
--------------------
* Maintenance release: update build environment:
  - android-arch: 1.0.0 -> 1.1.0
  - kotlin: 1.2.10 -> 1.2.21
  - kotlin coroutines: 0.21 -> 0.22.1

1.26.2  *(2018-01-06)*
--------------------
* Issue #134: Fix crash when copying word to clipboard
* Issue #136: Fix crash when searching then rotating.

1.26.1  *(2017-12-29)*
--------------------
* Issue #133: Stop support for Ice Cream Sandwich (4.0.x).

1.26.0  *(2017-12-29)*
--------------------
* Maintenance release:
  - Complete rewrite of the app in Kotlin.
  - Replace RxJava schedulers with Kotlin coroutines.

1.25.1  *(2017-11-13)*
--------------------
* Attempt to fix a crash related to the search view hint.

1.25.0  *(2017-11-11)*
--------------------
* Open the app in the last tab the user had selected.
* Update to support library 27.0.1

1.24.0  *(2017-11-06)*
--------------------
* Add word and character count.
* Migrate data classes to Kotlin.
* Fix access to system TTS settings.

1.23.0  *(2017-11-03)*
--------------------
* Added setting to include reverse lookups in thesaurus searches.

1.22.3  *(2017-11-02)*
--------------------
* Maintenance release: update environment:
  - target api level 27
  - RxJava 2.1.6
  - Jacoco 0.7.9
  - Robolectric 3.5.1

1.22.2  *(2017-10-22)*
--------------------
* Library updates: dagger 2.12, rxjava 2.1.5, robolectric 3.5
* Use LiveData instead of EventBus and loaders.

1.22.1  *(2017-10-19)*
--------------------
* Fix crash when sharing poem text or search results, or when doing a web search.

1.22.0  *(2017-10-15)*
--------------------
* Add setting to toggle lookups from text selection.
* Maintenance: use the android viewmodel and room architecture components.

1.21.0  *(2017-10-01)*
--------------------
* Add setting for the priority of the word of the day notification.
* Add favorites import/export function in the settings screen.

1.20.0  *(2017-08-27)*
--------------------
* Add settings to consider some different vowel sounds as identical for rhyming.
* Make the app compatible with Android O.

1.19.0  *(2017-07-08)*
--------------------
* Added an option to print the poem text.

1.18.5  *(2017-06-28)*
--------------------
* Added possibility to pause playback by typing "..." in the poem text. Currently only works for playback, not when saving the poem to an audio file.

1.18.4  *(2017-06-10)*
--------------------
* Attempt to fix ClassCastException crashes.

1.18.3  *(2017-05-30)*
--------------------
* Another fix for #81: Crash looking up a word in Poet Assistant, from another app.

1.18.2  *(2017-05-26)*
--------------------
* Technical:
  - Library updates: retrolambda 3.6.1, RxJava 2.1.0, gradle 2.3.2, dagger 2.11
  - Added Leak Canary.
  - Use two-way databinding
  - Refactoring: introduce "ViewModels"
* Crash fixes:
  - #78, #80: Crashes related to text selection in the reader
  - #79: Crash starting the app or starting the word of the day notification service.
  - #81: Crash looking up a word in Poet Assistant, from another app.
* Features:
  - Added link to the privacy policy in the about screen.

1.18.1  *(2017-04-23)*
--------------------
* Technical:
  - Introduced RxJava
  - Put back retrolambda
  - Updated to support library 25.3.1
  - Updated to dagger 2.10
  - Added espresso tests
* Bug fixes:
  - https://github.com/caarmen/poet-assistant/issues/72: Clearing the poem hides the app bar layout (on some devices)
  - On some devices, sometimes dismissing the filter dialog would leave the app bar layout hidden

1.18.0  *(2017-03-12)*
--------------------
* Added function in the setting screen to preview the voice.

1.17.1  *(2017-03-11)*
--------------------
* Maintenance release:
  - Update build environment
  - Dagger refactoring
  - Removed use of hidden MenuPopupHelper class

1.17.0  *(2017-02-25)*
--------------------
* Added option to export the poem as an audio file.
* Simplify the menu items:
  - Moved "clear search history" to the settings screen.
  - Moved "New", "Open", "Save", "Save as" to a "File" submenu.

1.16.1  *(2017-02-11)*
--------------------
* Fix issue #41: crash when opening the word of the day notification just after changing the theme.
* Limit rhyme results: With version 1.2.0 of the rhymer lib, all rhyme results are returned.  Now we filter the results to 500.  This means that now, when searching for "happy", the user will see up to 500 "last syllable" matches.  Previously, the user would see none.

1.16.0  *(2017-02-04)*
--------------------
* Made the toolbar collapsible when scrolling down.

1.15.0  *(2017-01-28)*
--------------------
* Added possibility to lookup words in Poet Assistant from other compatible applications.

1.14.0  *(2017-01-22*)
--------------------
* Removed the greendao library, for these issues:
  - https://gitlab.com/fdroid/fdroiddata/issues/585
  - https://github.com/greenrobot/greenDAO/issues/498
  - https://github.com/greenrobot/greenDAO/issues/412

1.13.2  *(2017-01-21*)
--------------------
* Attempt a fix for the project to compile for F-droid.

1.13.1  *(2017-01-15*)
--------------------
* Added the R, T, D icons to the popup menu.

1.13.0  *(2017-01-14*)
--------------------
* Added a layout setting with two options: "Clean" and "Efficient".
  -  The "Clean" layout has no RTD buttons: you tap on a word to see a popup with the RTD entries and other actions.
  -  The "Efficient" layout: you have the RTD buttons to more quickly look up a word, and tapping on a word shows additional actions.
  -  For selectable text (inside the dictionary + reader): the RTD actions are inserted into the system action popup.

1.12.0  *(2016-12-19)*
--------------------
* The word of the day will be the same for everybody on a given day.
* Added a list of the past 100 words of the day.
* Fixed some minor regressions in suggestions: (sorted, removed duplicates)

1.11.3  *(2016-12-19)*
--------------------
* Fix crash when searching by pattern

1.11.2  *(2016-12-16)*
--------------------
* Use the SeekBarPreference from the support library.
* When saving a poem to a new file, suggest a file name based on the poem content.
* Added contextual menu when tapping on a dictionary definition word, to look up the selected word in the rhymer, thesaurus or dictionary.

1.11.1  *(2016-12-10)*
--------------------
* Improved confirmation dialogs for "new poem" and deleting favorites.
* Allow undoing clearing search history

1.11.0  *(2016-12-03)*
--------------------
* Add a setting for the voice pitch
* Make the voice speed and pitch settings sliders instead of lists
* Display a snackbar when the user tries to play the poem without TTS initialized

1.10.11  *(2016-11-19)*
--------------------
* Use support library 25.0.1
* Fix corrupt db crashes
* Added dagger

1.10.10  *(2016-11-09)*
--------------------
* Prompt before clearing the poem text (from the "new" menu item)

1.10.9  *(2016-11-01)*
--------------------
* Improve display of tabs on small screens.

1.10.8  *(2016-10-30)*
--------------------
* Maintenance release: use a content provider for the suggested words.

1.10.7  *(2016-10-30)*
--------------------
* Support Android 4.0.3-4.0.4 (api level 15).

1.10.6  *(2016-10-29)*
--------------------
* Fix issue #36: save voice-searched words to suggested words list.
* Add more words to suggested list dropdown, based on similar sounding words.

1.10.5  *(2016-10-29)*
--------------------
* Fix issue #35: crash when searching for some words, with "show all rhymes" enabled.
* Use rhymer 1.1.2, which excludes "last syllable" rhymes if there there are too many and if there are "strict" rhymes.

1.10.4  *(2016-10-21)*
--------------------
* Try to improve random word selection by filtering out words which are too common or too rare.
* Added a setting to show/hide rhymes which have no definition

1.10.3  *(2016-10-21)*
--------------------
* Maintenance (updated Android SDK components)
* Simplified labels of different types of rhymes
* Use NotificationCompat for the word of the day notification.

1.10.2  *(2016-10-15)*
--------------------
* Fixed bug where changing the voice speed setting required an app restart.

1.10.1  *(2016-10-15)*
--------------------
* Minor UI improvements

1.10.0  *(2016-10-08)*
--------------------
* Possibility to star words.  
 - In the rhymer they will appear before the other rhymes.
 - In the thesaurus they appear starred but, for now, they remain at the default position
 - A new "favorites" tab shows all the starred words.
* Basic pattern search support.
 - Two wildcards are supported: `*` matches any number of letters, and `?` matches exactly one letter.
 - When searching for a word with one of these symbols, a new "Patterns" tab appears with the results, from
   which you may navigate to the rhymer, thesaurus, or dictionary tabs.
* Slight modification to the UI: since we can now have 5 or 6 tabs, it wasn't possible anymore to use
  a fixed tab layout with text labels.  Now the app has scrolling tabs, with icons instead of text labels
  on phones in portrait mode.

1.9.6  *(2016-10-06)*
--------------------
* Make the dictionary definitions selectable.

1.9.5  *(2016-10-02)*
--------------------
* Clarify rhyme types (ex: "One syllable matches" becomes "Last-syllable matches").

1.9.4  *(2016-09-18)*
--------------------
* Slight improvement of result list display on tablets.

1.9.3  *(2016-09-18)*
--------------------
* Attempt to fix Issue #21 about too frequent word of the day notifications.
* Fix for crash Issue #19, when opening the word of the day notification just after changing the theme in the settings.
* Use support library 24.2.1
* Use prefs library 1.1.0

1.9.2  *(2016-09-11)*
--------------------
* Added new setting to open the system text-to-speech settings.
* Fixed [Issue #18] (https://github.com/caarmen/poet-assistant/issues/18): Crash when saving a poem to Google Drive without write permission to the document.

1.9.1  *(2016-09-11)*
--------------------
* Added new theme option "Auto" to switch automatically between the light and dark themes.

1.9.0  *(2016-09-04)*
--------------------
* Make words in the result list selectable, allowing access to the copy/share/translate functions.

1.8.3  *(2016-09-03)*
--------------------
* Update gradle, gradle plugin, and support library

1.8.2  *(2016-08-14)*
--------------------
* Minor UX improvements regarding searches with no results.

1.8.1  *(2016-07-19)*
--------------------
* Corrected a string in the voice speed setting.

1.8.0  *(2016-07-19)*
--------------------
* Added setting for playback speed.

1.7.3  *(2016-07-03)*
--------------------
* Migrated the ListView to RecyclerView
* Use a Material theme for the settings screen, on v21+

1.7.2  *(2016-06-26)*
--------------------
* When enabling the Word of the Day, create a Wotd notification immediately.
* Added a feature to look up a random word in the dictionary.

1.7.1  *(2016-06-26)*
--------------------
* Maintenance release. Updated dependencies: 
 - support library: 24.0.0
 - streams support: 1.5
 - targetSdkVersion, compileVersion, buildToolsVersion: 24

1.7.0  *(2016-06-11)*
--------------------

* Added a setting to select the text-to-speech voice.

1.6.5  *(2016-05-28)*
--------------------

* Prevent a crash if the user does not have enough disk space.

1.6.4  *(2016-05-18)*
--------------------

* Updated to version 23.4.0 of the android support library.
* Fixed a crash when tapping on the dictionary "D" icon.  I could not reproduce this issue, but saw it appear in the Play Store console.  For some reason the text was a SpannableString.

1.6.3  *(2016-05-09)*
--------------------

* Use the data binding feature in the support library.
* Fixed colors of alert dialogs.

1.6.2  *(2016-04-09)*
--------------------

* Upgraded to version 23.3.0 of the android support library.

1.6.1  *(2016-04-04)*
--------------------

* Fixed crash when sharing.

1.6.0  *(2016-04-02)*
--------------------

* If a thesaurus or dictionary lookup fails, retry a lookup on the word stem.

1.5.2  *(2016-03-25)*
--------------------

* Fixed a big with the "Share" action from the "Word of the day" notification, where the share text was not updated

1.5.1  *(2016-03-24)*
--------------------

* Added a "Share" action from the "Word of the day" notification

1.5.0  *(2016-03-19)*
--------------------

* Added a "Share" feature from the rhymer, thesaurus, and dictionary tabs, to share the results as text.
* Added a boot receiver, so that if you enable the "Word of the day" feature, and reboot your phone, the word of the day will still be scheduled.

1.4.0  *(2016-03-11)*
--------------------

* Added "Word of the day" feature.

1.3.1  *(2016-03-11)*
--------------------

* Support library fixes:
   - Fixed crash when leaving the app and coming back
   - Dark theme available on Marshmallow now

1.3.0  *(2016-02-28)*
--------------------

* Added dark theme setting (not yet available on Marshmallow)
* Fixed crash when opening the about screen

1.2.1 *(2016-02-28)*
--------------------

* Small refactoring
* Use the android gradle plugin version 2.0.0-beta6

1.2.0 *(2016-02-26)*
--------------------

* Show strict rhymes before other rhymes.
* Bug fixes.

1.1.0 *(2016-02-25)*
--------------------

* New features:
  - Filter the rhyming results based on synonyms of a word
  - Filter the Thesaurus results based on rhymes of a word
  - Long press on a word in the Reader to be able to look it up in one of the three dictionaries

1.0.7 *(2016-02-22)*
--------------------

* Update the suggestions list as the user types in a search query.

1.0.6 *(2016-02-21)*
--------------------

* New app icon.
* Show a different text for the two empty list scenarios: 1) no search query entered yet, 2) no results for the given query.

1.0.5 *(2016-02-21)*
--------------------

* Fix to enable the "save" menu item after the user does "new" followed by "save as".

1.0.4 *(2016-02-20)*
--------------------

* Fix various crashes related to the "reader" tab.

1.0.3 *(2016-02-20)*
--------------------

* Fix crash when opening a non-existent file.
* Hide the list header view until results are loaded.

1.0.2 *(2016-02-20)*
--------------------

* Fix so the volume keys control the music stream.

1.0.1 *(2016-02-20)*
--------------------

* Save the poem text in onPause.
* If the cursor is at the end, read from the beginning.

1.0.0 *(2016-02-20)*
--------------------
* Initial version.
