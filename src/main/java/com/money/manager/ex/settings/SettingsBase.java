/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.money.manager.ex.core.ExceptionHandler;

/**
 * Base class for settings sections.
 */
public abstract class SettingsBase {

    public SettingsBase(Context context) {
        this.mContext = context.getApplicationContext() != null
            ? context.getApplicationContext()
            : context;
    }

    // Context for settings is the Application Context.
    protected final Context mContext;
    private SharedPreferences.Editor mEditor;

    // common

    protected SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    protected String getSettingsKey(Integer settingKeyConstant) {
        try {
            return mContext.getString(settingKeyConstant, "");
        } catch (Exception e) {
//            throw new RuntimeException("error getting string for resource " +
//                    Integer.toString(settingKeyConstant), e);
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "error getting string for resource " +
                Integer.toString(settingKeyConstant));
        }
        return "";
    }

    public SharedPreferences.Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }

    /**
     * Clear the preference value (remove preference).
     * @param key
     */
    public void clear(String key) {

    }

    // String

    protected String get(Integer settingKey, String defaultValue) {
        String key = getSettingsKey(settingKey);
        return get(key, defaultValue);
    }

    public String get(String key, String defaultValue) {
        try {
            return getSharedPreferences().getString(key, defaultValue);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "reading string preference: " + key);

            return defaultValue;
        }
    }

    /**
     * Save string value to settings.
     * @param key
     * @param value
     */
    protected boolean set(String key, String value) {
        getEditor().putString(key, value);
        boolean result = getEditor().commit();
        return result;
    }

    // Boolean

    public boolean get(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    protected boolean get(Integer settingKey, boolean defaultValue) {
        String key = getSettingsKey(settingKey);
        return getBooleanSetting(key, defaultValue);
    }

    protected boolean getBooleanSetting(String settingKey) {
        return getBooleanSetting(settingKey, false);
    }

    protected boolean getBooleanSetting(String settingKey, boolean defaultValue) {
        // This is the main method that actually fetches the value.
        return getSharedPreferences().getBoolean(settingKey, defaultValue);
    }

    public boolean set(String key, boolean value) {
        getEditor().putBoolean(key, value);
        boolean result = getEditor().commit();

        return result;
    }

    // Integer

    public int get(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    /**
     * Retrieve setting by passing the R.string.key
     * @param settingKey R.string.key_name
     * @param defaultValue The default value to use if setting not found.
     * @return The setting value or default.
     */
    protected int get(Integer settingKey, int defaultValue) {
        String key = getSettingsKey(settingKey);
        return getIntSetting(key, defaultValue);
    }

    protected int getIntSetting(String settingKey, int defaultValue) {
        // This is the main method that actually fetches the value.
        return getSharedPreferences().getInt(settingKey, defaultValue);
    }

    protected boolean set(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        boolean result = editor.commit();
        return result;
    }

    public boolean set(Integer key, int value) {
        String stringKey = getSettingsKey(key);
        return this.set(stringKey, value);
    }

    public boolean set(Integer key, boolean value) {
        String stringKey = getSettingsKey(key);
        return this.set(stringKey, value);
    }

}
