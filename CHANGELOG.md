Change Log
==========

1.10.3  *(2016-10-15)*
--------------------
* Try to improve random word selection by filtering out words which are too common or too rare.

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
