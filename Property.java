/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.android.data;

/**
 * Property represents a typed column in a database.
 *
 * Within a given database row, the parameter may not exist, in which case the
 * value is null, it may be of an incorrect type, in which case an exception is
 * thrown, or the correct type, in which case the value is returned.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 * @param <TYPE>
 *            a database supported type, such as String or Integer
 */
public abstract class Property<TYPE> implements Cloneable {

    // --- pseudo-properties

    /** counting in aggregated tables */
    public static IntegerProperty countProperty() {
        return new IntegerProperty(null, "count") { //$NON-NLS-1$
            @Override
            public String asSqlSelector() {
                return "COUNT(1) AS " + name; //$NON-NLS-1$
            };
        };
    }

    // --- other goodness

    /** The database table name this property */
    public String tableName;

    /** The database column name for this property */
    public String name;

    /**
     * Create a property by table and column name
     */
    protected Property(String tableName, String columnName) {
        this.tableName = tableName;
        this.name = columnName;
    }

    /**
     * Create a property by combined name
     */
    protected Property(String combinedName) {
        int period = combinedName.indexOf('.');
        this.tableName = combinedName.substring(0, period);
        this.name = combinedName.substring(period + 1);
    }

    /**
     * Accept a visitor
     */
    abstract public <RETURN, PARAMETER> RETURN accept(
            PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data);

    /**
     * Return the name of the property
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Return the qualified name of this property
     */
    public String qualifiedName() {
        return tableName + '.' + name;
    }

    /**
     * Return the qualified name of the property
     */
    public String asSqlSelector() {
        return qualifiedName() + " AS " + name; //$NON-NLS-1$
    }

    /**
     * Clone this property with new parameters for name and tableName
     */
    public Property<TYPE> withNewValues(String newTableName, String newName) {
        try {
            Property<TYPE> clone = (Property<TYPE>) super.clone();
            clone.tableName = newTableName;
            clone.name = newName;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Visitor interface for property classes
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public interface PropertyVisitor<RETURN, PARAMETER> {
        public RETURN visitInteger(Property<Integer> property, PARAMETER data);

        public RETURN visitLong(Property<Long> property, PARAMETER data);

        public RETURN visitDouble(Property<Double> property, PARAMETER data);

        public RETURN visitString(Property<String> property, PARAMETER data);
    }

    /**
     * Integer property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class IntegerProperty extends Property<Integer> {

        public IntegerProperty(String tableName, String name) {
            super(tableName, name);
        }

        public IntegerProperty(String combinedName) {
            super(combinedName);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitInteger(this, data);
        }
    }

    /**
     * String property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class StringProperty extends Property<String> {

        public StringProperty(String tableName, String name) {
            super(tableName, name);
        }

        public StringProperty(String combinedName) {
            super(combinedName);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitString(this, data);
        }
    }

    /**
     * Double property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class DoubleProperty extends Property<Double> {

        public DoubleProperty(String tableName, String name) {
            super(tableName, name);
        }

        public DoubleProperty(String combinedName) {
            super(combinedName);

        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitDouble(this, data);
        }
    }

    /**
     * Long property type. See {@link Property}
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class LongProperty extends Property<Long> {

        public LongProperty(String tableName, String name) {
            super(tableName, name);
        }

        public LongProperty(String combinedName) {
            super(combinedName);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitLong(this, data);
        }
    }

}
