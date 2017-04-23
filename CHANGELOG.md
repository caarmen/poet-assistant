Change Log
==========

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
