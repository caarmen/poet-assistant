package ca.rmen.android.poetassistant;

import com.squareup.leakcanary.RefWatcher;

/**
 * Created by calvarez on 26/10/2017.
 */

public class TestPoetAssistantApplication extends PoetAssistantApplication {
    @Override
    protected RefWatcher setupLeakCanary() {
        return RefWatcher.DISABLED;
    }
}
