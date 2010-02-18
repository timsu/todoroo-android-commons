/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.android.data;

import java.util.WeakHashMap;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.todoroo.android.data.Property.PropertyVisitor;

/**
 * AstridCursor wraps a cursor and allows users to query for individual
 * {@link Property} types or read an entire {@link AbstractModel} from
 * a database row.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 * @param <TYPE> a model type that is returned by this cursor
 */
public class TodorooCursor<TYPE extends AbstractModel> extends CursorWrapper {

    /** Weakly cache field name to column id references for this cursor */
    private final WeakHashMap<String, Integer> columnIndexCache;

    /** Property reading visitor */
    private static final CursorReadingVisitor reader = new CursorReadingVisitor();

    /**
     * Create an <code>AstridCursor</code> from the supplied {@link Cursor}
     * object.
     *
     * @param cursor
     */
    public TodorooCursor(Cursor cursor) {
        super(cursor);

        columnIndexCache = new WeakHashMap<String, Integer>();
    }

    /**
     * Get the value for the given property on the underlying {@link Cursor}
     *
     * @param <PROPERTY_TYPE> type to return
     * @param property to retrieve
     * @return
     */
    public <PROPERTY_TYPE> PROPERTY_TYPE get(Property<PROPERTY_TYPE> property) {
        return (PROPERTY_TYPE)property.accept(reader, this);
    }


    /**
     * Use cache to get the column index for the given field name
     */
    public synchronized int getColumnIndexFromCache(String field) {
        Integer index = columnIndexCache.get(field);
        if(index == null) {
            index = getColumnIndexOrThrow(field);
            columnIndexCache.put(field, index);
        }

        return index;
    }

    /**
     * Visitor that reads the given property from a cursor
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class CursorReadingVisitor implements PropertyVisitor<Object, TodorooCursor<?>> {

        public Object visitDouble(Property<Double> property,
                TodorooCursor<?> cursor) {
            return cursor.getDouble(cursor.getColumnIndexFromCache(property.name));
        }

        public Object visitInteger(Property<Integer> property,
                TodorooCursor<?> cursor) {
            return cursor.getInt(cursor.getColumnIndexFromCache(property.name));
        }

        public Object visitLong(Property<Long> property, TodorooCursor<?> cursor) {
            return cursor.getLong(cursor.getColumnIndexFromCache(property.name));
        }

        public Object visitString(Property<String> property,
                TodorooCursor<?> cursor) {
            return cursor.getString(cursor.getColumnIndexFromCache(property.name));
        }

    }

}
