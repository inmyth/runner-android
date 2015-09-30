package jp.hanatoya.training.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceUtils {
	
	public static final String KEY_RUNNER_NAME = "KEY_RUNNER_NAME";
	public static final String KEY_LAST_DISTANCE = "KEY_LAST_DISTANCE";
	public static final String KEY_RUNNER_DATA_RUNNER = "KEY_RUNNER_DATA_RUNNER";
	public static final String KEY_CACHE_PITCH = "KEY_CACHE_PITCH";

	public static void setRunnerName (Context ctx, String name){
	  Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
	  editor.putString(KEY_RUNNER_NAME, name);
	  editor.commit();	
	}
	
	public static String getPlayerName(Context ctx){
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
	    return sharedPreferences.getString(KEY_RUNNER_NAME, null);				
	}
	
	public static int getLastDistance(Context ctx){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPreferences.getInt(KEY_LAST_DISTANCE, 0);
	}
	
	
	public static void setLastDistance(Context ctx, int lastDistance){
		  Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		  editor.putInt(KEY_LAST_DISTANCE, lastDistance);
		  editor.commit();	
	}

	
}
