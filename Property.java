/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.androidcommons.data;

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
    public final Table table;

    /** The database column name for this property */
    public final String name;

    /**
     * Create a property by table and column name
     */
    protected Property(Table table, String columnName) {
        this.table = table;
        this.name = columnName;
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
        return table.getName() + '.' + name;
    }

    /**
     * Return the qualified name of the property
     */
    public String asSqlSelector() {
        return qualifiedName() + " AS " + name; //$NON-NLS-1$
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

        public IntegerProperty(Table table, String name) {
            super(table, name);
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

        public StringProperty(Table table, String name) {
            super(table, name);
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

        public DoubleProperty(Table table, String name) {
            super(table, name);
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

        public LongProperty(Table table, String name) {
            super(table, name);
        }

        @Override
        public <RETURN, PARAMETER> RETURN accept(
                PropertyVisitor<RETURN, PARAMETER> visitor, PARAMETER data) {
            return visitor.visitLong(this, data);
        }
    }

}
