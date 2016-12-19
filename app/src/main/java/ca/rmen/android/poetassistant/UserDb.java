package ca.rmen.android.poetassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import java.util.Set;

import ca.rmen.android.poetassistant.main.dictionaries.DaoMaster;
import ca.rmen.android.poetassistant.main.dictionaries.FavoriteDao;

class UserDb extends DaoMaster.OpenHelper {
    private static final String DB_NAME = "userdata";
    private final Context mContext;

    UserDb(Context context) {
        super(context, DB_NAME);
        mContext = context;
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> favorites = prefs.getStringSet("PREF_FAVORITE_WORDS", null);
        if (favorites != null) {
            for (String favorite : favorites) {
                DatabaseStatement stmt = db.compileStatement("INSERT INTO " + FavoriteDao.TABLENAME + "(" + FavoriteDao.Properties.Word.columnName + ") VALUES (?)");
                stmt.bindString(1, favorite);
                stmt.executeInsert();
            }
        }
        prefs.edit().remove("PREF_FAVORITE_WORDS").apply();
    }
}
