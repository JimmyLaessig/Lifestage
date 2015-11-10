package at.ac.tuwien.ims.lifestage.vibrotouch.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves and overwrites curent user.
 * <p>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class UserPreferences {
    private final static String setting="settings";
    private final static String userid="userid";

    public static void saveUserID(Context context, String text) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(userid, text);
        editor.commit();
    }

    public static String getCurrentUserID(Context context) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        text = settings.getString(userid, null);
        return text;
    }
}
