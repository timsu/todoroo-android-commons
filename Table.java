package com.todoroo.androidcommons.data;

/**
 * Pair class for tables
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class Table {
    private final String name;
    private final Class<? extends AbstractModel> modelClass;
    private Property<?>[] properties;

    public Table(String name, Class<? extends AbstractModel> modelClass) {
        this.name = name;
        this.modelClass = modelClass;
    }

    public String getName() {
        return name;
    }

    public Property<?>[] getProperties() {
        return properties;
    }

    public void setProperties(Property<?>[] properties) {
        this.properties = properties;
    }

    public Class<? extends AbstractModel> getModelClass() {
        return modelClass;
    }

    @Override
    public String toString() {
        return name;
    }
}