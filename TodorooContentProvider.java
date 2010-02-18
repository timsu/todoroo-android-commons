/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.android.data;

import java.util.Map.Entry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.todoroo.android.service.Autowired;
import com.todoroo.android.service.DependencyInjectionService;
import com.todoroo.android.service.ExceptionService;

/**
 * General-purpose Content Provider for objects backed by an
 * {@link AbstractDatabase}. The default implementation exposes all columns of
 * the database.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
@SuppressWarnings("nls")
abstract public class TodorooContentProvider extends ContentProvider {

    /**
     * @return table name of database
     */
    abstract protected String getTableName();

    /**
     * @return name of provider, i.e. com.company.product.provider
     */
    abstract protected String getProviderName();

    /**
     * @return default values for inserting new tasks. Can be null.
     */
    abstract protected ContentValues getDefaultValues();

    // --- internal constants

    protected static final int DIR = 1;
    protected static final int ITEM = 2;
    protected static final int GROUP = 3;

    private final UriMatcher uriMatcher;

    // --- instance variables

    @Autowired
    protected AbstractDatabase database;

    @Autowired
    protected ExceptionService exceptionService;

    public TodorooContentProvider() {
        DependencyInjectionService.getInstance().inject(this);

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(getProviderName(), "items", DIR);
        uriMatcher.addURI(getProviderName(), "#", ITEM);
        uriMatcher.addURI(getProviderName(), "groupby/*", GROUP);
    }

    /**
     * You must have opened your database before calling this method
     */
    @Override
    public boolean onCreate() {
        return database.getDatabase() != null;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case DIR:
            return "vnd.android.cursor.dir/vnd.todoroo";
        case ITEM:
            return "vnd.android.cursor.item/vnd.todoroo";
        case GROUP:
            return "vnd.android.cursor.dir/vnd.todoroo.group";
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    protected Uri makeContentUri() {
        return Uri.parse("content://" + getProviderName());
    }

    /*
     * ======================================================================
     * ========================================================== data access
     * ======================================================================
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
        case DIR:
            count = database.getDatabase().delete(getTableName(), selection,
                    selectionArgs);
            break;
        case ITEM:
            String id = uri.getPathSegments().get(0);
            count = database.getDatabase().delete(
                    getTableName(),
                    (AbstractModel.ID_PROPERTY + " = " + id)
                            + (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;
        case GROUP:
            throw new IllegalArgumentException("Invalid URI for delete: " + uri);

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // merge values with defaults
        long rowID = insertHelper(database, getTableName(),
                values, getDefaultValues());

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(makeContentUri(), rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * Helper for inserting from ContentValues into the given database.
     *
     * @param database
     * @param tableName
     * @param values
     * @param defaultValues
     * @return
     */
    public static long insertHelper(AbstractDatabase database, String tableName,
            ContentValues values, ContentValues defaultValues) {
        ContentValues valuesWithDefaults = defaultValues;
        if(valuesWithDefaults == null)
            valuesWithDefaults = values;
        else {
            valuesWithDefaults = new ContentValues(valuesWithDefaults);
            rewriteKeysFor(values, valuesWithDefaults);
        }

        long rowID = database.getDatabase().insertOrThrow(tableName,
                AbstractModel.ID_PROPERTY, valuesWithDefaults);
        return rowID;
    }

    /**
     * Transfer data from source to dest, stripping out table names from keys
     * @param source
     * @param dest
     */
    private static void rewriteKeysFor(ContentValues source, ContentValues dest) {
        for(Entry<String, Object> entry : source.valueSet()) {
            String key = stripTable(entry.getKey());
            if(entry.getValue() instanceof String)
                dest.put(key, (String)entry.getValue());
            else if(entry.getValue() instanceof Long)
                dest.put(key, (Long)entry.getValue());
            else if(entry.getValue() instanceof Integer)
                dest.put(key, (Integer)entry.getValue());
            else if(entry.getValue() instanceof Double)
                dest.put(key, (Double)entry.getValue());
            else {
                // if you encounter this error message, you can add your
                // case in here manually
                throw new UnsupportedOperationException("Unsupported object type " +
                        entry.getValue().getClass());
            }
        }
    }

    /**
     * Strips a column of its table identifier. i.e. 'tasks.id' becomes 'id'
     */
    private static String stripTable(String key) {
        int dot = key.indexOf('.');
        if(dot == -1)
            return key;
        else
            return key.substring(dot + 1);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(getTableName());

        String groupBy = null;
        switch (uriMatcher.match(uri)) {
        case DIR:
            break;
        case ITEM:
            sqlBuilder.appendWhere(AbstractModel.ID_PROPERTY + "="
                    + uri.getPathSegments().get(0));
            break;
        case GROUP:
            groupBy = uri.getPathSegments().get(1);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = sqlBuilder.query(database.getDatabase(), projection,
                selection, selectionArgs, groupBy, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // merge values with defaults
        ContentValues valuesWithDefaults = getDefaultValues();
        if(valuesWithDefaults == null)
            valuesWithDefaults = values;
        else {
            valuesWithDefaults = new ContentValues(valuesWithDefaults);
            rewriteKeysFor(values, valuesWithDefaults);
        }

        int count = 0;
        switch (uriMatcher.match(uri)) {
        case DIR:
            count = database.getDatabase().update(getTableName(), valuesWithDefaults,
                    selection, selectionArgs);
            break;
        case ITEM:
            String id = uri.getPathSegments().get(0);
            ContentValues newValues = new ContentValues();
            rewriteKeysFor(values, newValues);
            count = database.getDatabase().update(
                    getTableName(),
                    newValues,
                    (AbstractModel.ID_PROPERTY + "=" + id)
                            + (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;
        case GROUP:
            throw new IllegalArgumentException("Invalid URI for update: " + uri);

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
