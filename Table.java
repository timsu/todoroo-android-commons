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
    private final Property<?>[] properties;

    public Table(String name, Class<? extends AbstractModel> modelClass,
            Property<?>[] properties) {
        this.name = name;
        this.modelClass = modelClass;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Property<?>[] getProperties() {
        return properties;
    }

    public Class<? extends AbstractModel> getModelClass() {
        return modelClass;
    }

    @Override
    public String toString() {
        return name;
    }
}