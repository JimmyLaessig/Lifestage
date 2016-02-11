package at.ac.tuwien.ims.lifestage.vibrotouch.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Saves and overwrites curent user.
 * <p>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class UserPreferences {
    private final static String setting="settings";
    private final static String userid="userid";
    private final static String testcaseid="testcaseid";
    private final static String testcaseid_user="testcaseid_user";
    private final static String testcase_finished="testcase_finished";;
    private final static String builderInfo="builderInfo_";
    private final static String seed_str="seed";

    public static void setUserID(Context context, String text) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(userid, text);
        editor.commit();
        Log.d("Prefs", "Saved new User ID: " + text);
    }

    public static String getCurrentUserID(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        return settings.getString(userid, "0");
    }

    public static void setJustFinishedTestcase(Context context, boolean finished) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean(testcase_finished, finished);
        editor.commit();
        Log.d("Prefs", "Saved just finished a Testcase: " + finished);
    }

    public static boolean getJustFinishedTestcase(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        return settings.getBoolean(testcase_finished, false);
    }

    public static void setCurrentTestcasePositionInList(Context context, int id) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        String user=getCurrentUserID(context);
        editor.putInt(testcaseid, id);
        editor.putString(testcaseid_user, user);
        editor.commit();
        Log.d("Prefs", "Saved new Testcase ID: " + id + " (with User: " + user + ")");
    }

    public static int getCurrentTestcasePositionInList(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        String res=settings.getString(testcaseid_user, null);
        if (res==null)
            return -2;
        if (!res.equals(getCurrentUserID(context)))
            return -3;
        return settings.getInt(testcaseid, -1);
    }

    public static void setShowTestcaseInfo(Context context, int scenario, boolean value) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean(builderInfo + scenario, value);
        editor.commit();
    }

    public static boolean getShowTestcaseInfo(Context context, int scenario) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        return settings.getBoolean(builderInfo + scenario, true);
    }

    public static void clearAll(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.clear();
        editor.commit();
    }
}
