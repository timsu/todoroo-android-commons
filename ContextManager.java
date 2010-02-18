/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.androidcommons.service;

import android.content.Context;

/**
 * Singleton class to manage current application context
 * b
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class ContextManager {

    /**
     * Global application context
     */
    private static Context context = null;
    
    /**
     * Sets the global context
     * @param context
     */
    public static void setContext(Context context) {
        ContextManager.context = context;
    }

    /**
     * Gets the global context
     */
    public static Context getContext() {
        return context;
    }
}
