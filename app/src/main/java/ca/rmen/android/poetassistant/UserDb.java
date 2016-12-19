package ca.rmen.android.poetassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import java.util.Set;

import ca.rmen.android.poetassistant.main.dictionaries.DaoMaster;
import ca.rmen.android.poetassistant.main.dictionaries.FavoriteDao;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionDao;

class UserDb extends DaoMaster.OpenHelper {
    private static final String DB_NAME = "userdata.db";
    private final Context mContext;

    UserDb(Context context) {
        super(context, DB_NAME);
        mContext = context;
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        // Migrate our data from shared prefs to the db.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> favorites = prefs.getStringSet("PREF_FAVORITE_WORDS", null);
        if (favorites != null) {
            for (String favorite : favorites) {
                DatabaseStatement stmt = db.compileStatement("INSERT INTO " + FavoriteDao.TABLENAME + "(" + FavoriteDao.Properties.Word.columnName + ") VALUES (?)");
                stmt.bindString(1, favorite);
                stmt.executeInsert();
            }
        }

        Set<String> suggestions = prefs.getStringSet("pref_suggestions", null);
        if (suggestions != null) {
            for (String suggestion : suggestions) {
                DatabaseStatement stmt = db.compileStatement("INSERT INTO " + SuggestionDao.TABLENAME + "(" + SuggestionDao.Properties.Word.columnName + ") VALUES (?)");
                stmt.bindString(1, suggestion);
                stmt.executeInsert();
            }
        }
        prefs.edit().remove("pref_suggestions").apply();
    }
}
