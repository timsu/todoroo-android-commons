/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.androidcommons.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.todoroo.androidcommons.data.Property.PropertyVisitor;

/**
 * AbstractDatabase is a database abstraction which wraps a SQLite database.
 * <p>
 * Users of this class are in charge of the database's lifecycle - ensuring that
 * the database is open when needed and closed when usage is finished. Within an
 * activity, this is typically accomplished through the onResume and onPause
 * methods, though if the database is not needed for the activity's entire
 * lifecycle, it can be closed earlier.
 * <p>
 * Direct querying is not recommended for type safety reasons. Instead, use one
 * of the service classes to issue the request and return a {@link TodorooCursor}.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
abstract public class AbstractDatabase {

	// --- abstract methods

    /**
     * Get SQLiteOpenHelper underlying this wrapper
     */
	protected abstract SQLiteOpenHelper getHelper();

	/**
	 * Get database underlying this wrapper
	 */
	public abstract SQLiteDatabase getDatabase();

	/**
     * Return the name of the table containing these models
     * @param modelType
     * @return
     */
    public abstract Table getTable(Class<? extends AbstractModel> modelType);

	// --- implementation

    /**
     * Internal pointer to open database. Hides the fact that there is a
     * database and a wrapper by making a single monolithic interface
     */
	protected SQLiteDatabase database = null;

    /**
     * Open the database for writing. Must be closed afterwards. If user is
     * out of disk space, database may be opened for reading instead
     */
    protected synchronized void openForWriting() {
        if(getHelper() == null)
            throw new IllegalAccessError("Database used without being opened!"); //$NON-NLS-1$

        try {
            database = getHelper().getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e("abstract-database", "error opening db", e); //$NON-NLS-1$ //$NON-NLS-2$
            // provide read-only database
            openForReading();
        }
    }

    /**
     * Open the database for reading. Must be closed afterwards
     */
    protected synchronized void openForReading() {
        database = getHelper().getReadableDatabase();
    }

    /**
     * Close the database if it has been opened previously
     */
    public synchronized void close() {
        if(database != null)
            database.close();
    }

    /**
     * Visitor that returns SQL constructor for this property
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    @SuppressWarnings("nls")
    public static class SqlConstructorVisitor implements PropertyVisitor<String, Void> {

        public String visitDouble(Property<Double> property, Void data) {
            return String.format("%s REAL", property.name);
        }

        public String visitInteger(Property<Integer> property, Void data) {
            return String.format("%s INTEGER", property.name);
        }

        public String visitLong(Property<Long> property, Void data) {
            return String.format("%s INTEGER", property.name);
        }

        public String visitString(Property<String> property, Void data) {
            return String.format("%s TEXT", property.name);
        }
    }
}

