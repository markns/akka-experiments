package com.mns.streams.model;

import com.google.common.base.MoreObjects;

public class Yield {
    private final int id;
    private final double value;

    public Yield(int id, double value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("value", value)
                .toString();
    }
}
