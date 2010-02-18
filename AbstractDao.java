/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.android.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;



/**
 * Abstract data access object
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
abstract public class AbstractDao<TYPE extends AbstractModel> {

    private Class<TYPE> modelClass;

    // --- public methods

    public AbstractDao(Class<TYPE> modelClass) {
        this.modelClass = modelClass;
    }
    
    /**
     * Allows users to use SQLiteQueryBuilder to construct a query
     * @param database
     * @param properties
     * @param builder
     * @param where
     * @param groupBy
     * @param sortOrder
     * @return
     */
    public TodorooCursor<TYPE> query(AbstractDatabase database,
            Property<?>[] properties, SQLiteQueryBuilder builder,
            String where, String groupBy, String sortOrder) {
        Cursor cursor = builder.query(database.getDatabase(), 
                propertiesToFields(properties), where, null, groupBy, null, 
                sortOrder);
        return new TodorooCursor<TYPE>(cursor);
    }

    /**
     * Return cursor to all items matching criteria
     *
     * @param database
     * @param properties
     *            properties to read
     * @param where
     *            SQL where clause. <code>null</code> omits clause.
     * @return
     */
    public TodorooCursor<TYPE> fetch(AbstractDatabase database,
            Property<?>[] properties, String where) {
        return fetchItems(database, database.getTableName(modelClass),
                properties, where, null, null, null, null);
    }
    
    /**
     * Return cursor to all items matching criteria
     *
     * @param database
     * @param properties
     *            properties to read
     * @param where
     *            SQL where clause. <code>null</code> omits clause.
     * @param orderby
     *            SQL order by clause. <code>null</code> omits clause.
     * @return
     */
    public TodorooCursor<TYPE> fetch(AbstractDatabase database,
            Property<?>[] properties, String where, String orderby) {
        return fetchItems(database, database.getTableName(modelClass),
                properties, where, null, null, orderby, null);
    }

    /**
     * Return cursor to all items matching criteria
     *
     * @param database
     * @param properties
     *            properties to read
     * @param where
     *            SQL where clause. <code>null</code> omits clause.
     * @param orderby
     *            SQL order by clause. <code>null</code> omits clause.
     * @param limit
     *            SQL limit by clause. <code>null</code> omits clause.
     * @return
     */
    public TodorooCursor<TYPE> fetch(AbstractDatabase database,
            Property<?>[] properties, String where, String orderby, String limit) {
        return fetchItems(database, database.getTableName(modelClass),
                properties, where, null, null, orderby, limit);
    }

    /**
     * Return cursor to all items matching criteria
     *
     * @param database
     * @param properties
     *            properties to read
     * @param where
     *            SQL where clause. <code>null</code> omits clause.
     * @param groupby
     *            SQL group by clause. <code>null</code> omits clause.
     * @param having
     *            SQL having clause. <code>null</code> omits clause.
     * @param orderby
     *            SQL order by clause. <code>null</code> omits clause.
     * @param limit
     *            SQL limit by clause. <code>null</code> omits clause.
     * @return
     */
    public TodorooCursor<TYPE> fetch(AbstractDatabase database,
            Property<?>[] properties, String where, String groupby, 
            String having, String orderby, String limit) {
        return fetchItems(database, database.getTableName(modelClass),
                properties, where, groupby, having, orderby, limit);
    }

    /**
     * Returns object corresponding to the given identifier
     *
     * @param database
     * @param table
     *            name of table
     * @param properties
     *            properties to read
     * @param id
     *            id of item
     * @return
     */
    public TYPE fetch(AbstractDatabase database, Property<?>[] properties,
            long id) {
        TodorooCursor<TYPE> cursor = fetchItem(database,
                database.getTableName(modelClass), properties, id);
        try {
            if (cursor.getCount() == 0)
                return null;
            Constructor<TYPE> constructor = modelClass.getConstructor(TodorooCursor.class,
                    Property[].class);
            return constructor.newInstance(cursor, properties);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            cursor.close();
        }
    }

    // --- single task operations

    /**
     * Delete the given id
     *
     * @param database
     * @param id
     * @return true if delete was successful
     */
    public boolean delete(AbstractDatabase database, long id) {
        return deleteItem(database, database.getTableName(modelClass), id);
    }

    /**
     * Save the given object to the database.
     *
     * @return true on success.
     */
    public boolean save(AbstractDatabase database, AbstractModel item) {
        if (item.getId() == AbstractModel.NO_ID) {
            return createItem(database, database.getTableName(modelClass),
                    item);
        } else {
            ContentValues values = item.getSetValues();

            if (values.size() == 0) // nothing changed
                return true;

            return saveItem(database, database.getTableName(modelClass),
                    item);
        }
    }

    // --- internal helpers

    /**
     * Convert the given list of properties to a list of fields for sqlite
     *
     * @param properties
     * @return
     */
    protected static String[] propertiesToFields(Property<?>[] properties) {
        String[] fields = new String[properties.length];
        for (int i = 0; i < properties.length; i++)
            fields[i] = properties[i].asSqlSelector();
        return fields;
    }

    /**
     * Convert the given list of properties to a string suitable for sql query
     *
     * @param properties
     * @param qualified
     *            whether field should be qualified (i.e. multiple tables are
     *            referenced)
     * @return
     */
    protected static String propertiesForSelect(Property<?>[] properties,
            boolean qualified) {
        StringBuilder sql = new StringBuilder();
        for(int i = 0; i < properties.length; i++) {
        	if(qualified)
        		sql.append(properties[i].asSqlSelector());
        	else
        	    sql.append(properties[i].name);
            if(i != properties.length - 1)
            	sql.append(',');
        }
        return sql.toString();
    }

    /**
     * Sanitize the given input for SQL
     * @param input
     * @return
     */
    @SuppressWarnings("nls")
    public static String sanitize(String input) {
        return input.replace("\\", "\\\\").replace("'", "\\'");
    }

    // --- general database operations

    /**
     * Return cursor to all items matching criteria
     *
     * @param database
     * @param table
     *            name of table
     * @param properties
     *            properties to read
     * @param where
     * @param groupby
     * @param having
     * @param orderby
     * @param limit
     * @return
     */
    protected static <TYPE extends AbstractModel> TodorooCursor<TYPE> fetchItems(
            AbstractDatabase database, String table, Property<?>[] properties, 
            String where, String groupby, String having, String orderby, 
            String limit) {
        Cursor cursor = database.getDatabase().query(
                table,
                propertiesToFields(properties), where, null, groupby, having,
                orderby, limit);
        return new TodorooCursor<TYPE>(cursor);
    }

    /**
     * Returns cursor to object corresponding to the given identifier
     *
     * @param database
     * @param table
     *            name of table
     * @param properties
     *            properties to read
     * @param id
     *            id of item
     * @return
     */
    protected static <TYPE extends AbstractModel> TodorooCursor<TYPE> fetchItem(
            AbstractDatabase database, String table, Property<?>[] properties,
            long id) {
        Cursor cursor = database.getDatabase().query(true, table,
                propertiesToFields(properties),
                AbstractModel.ID_PROPERTY + "=" + id, null, null, null, null, null); //$NON-NLS-1$
        cursor.moveToFirst();
        return new TodorooCursor<TYPE>(cursor);
    }

    /**
     * Delete the given item
     *
     * @param database
     * @param id
     *            id of item
     * @return true if delete was successful
     */
    protected static boolean deleteItem(AbstractDatabase database, String table,
            long id) {
        return database.getDatabase().delete(table,
                AbstractModel.ID_PROPERTY + "=" + id, null) > 0; //$NON-NLS-1$
    }

    /**
     * Creates the given item.
     *
     * @param database
     * @param table
     *            table name
     * @param item
     *            item model
     * @return returns true on success.
     */
    protected static boolean createItem(AbstractDatabase database, String table,
            AbstractModel item) {
        long newRow = database.getDatabase().insert(table,
                AbstractModel.ID_PROPERTY, item.getMergedValues());
        item.setId(newRow);

        return newRow >= 0;
    }

    /**
     * Saves the given item.
     *
     * @param database
     * @param table
     *            table name
     * @param item
     *            item model
     * @return returns true on success.
     */
    protected static boolean saveItem(AbstractDatabase database,
            String table, AbstractModel item) {
        ContentValues values = item.getSetValues();

        if(values.size() == 0) // nothing changed
            return true;

        return database.getDatabase().update(table, values,
                AbstractModel.ID_PROPERTY + "=" + item.getId(), null) > 0; //$NON-NLS-1$
    }
}
