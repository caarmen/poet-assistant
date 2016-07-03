Change Log
==========

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
