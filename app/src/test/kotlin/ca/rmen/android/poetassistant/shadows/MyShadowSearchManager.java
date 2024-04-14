package ca.rmen.android.poetassistant.shadows;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

import ca.rmen.android.poetassistant.R;

@Implements(SearchManager.class)
public class MyShadowSearchManager {

    public MyShadowSearchManager() {
    }

    @Implementation
    public SearchableInfo getSearchableInfo(ComponentName componentName) {
        SearchableInfo searchableInfo = ReflectionHelpers.callConstructor(SearchableInfo.class);
        ReflectionHelpers.setField(searchableInfo, "mHintId", R.string.search_hint);
        ReflectionHelpers.setField(searchableInfo, "mLabelId", R.string.search_hint);
        ReflectionHelpers.setField(searchableInfo, "mSearchActivity", new ComponentName(
                "ca.rmen.android.poetassistant.test", "ca.rmen.android.poetassistant.main.MainActivity"
        ));
        ReflectionHelpers.setField(searchableInfo, "mSuggestAuthority","ca.rmen.android.poetassistant.test.SuggestionsProvider");
        ReflectionHelpers.setField(searchableInfo, "mSuggestIntentAction", "android.intent.action.SEARCH");
        ReflectionHelpers.setField(searchableInfo, "mSuggestProviderPackage", "ca.rmen.android.poetassistant.test");
        ReflectionHelpers.setField(searchableInfo, "mSearchImeOptions", 2);
        ReflectionHelpers.setField(searchableInfo, "mSearchInputType", 1);
        ReflectionHelpers.setField(searchableInfo, "mSearchMode", 32);
        ReflectionHelpers.setField(searchableInfo, "mVoiceSearchMode", 5);
        return searchableInfo;

    }
}
