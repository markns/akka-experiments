package com.mns.streams.model;

import com.google.common.base.MoreObjects;

import java.time.LocalDate;

public class Product {
    private final int id;
    private final LocalDate date;

    public Product(int id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("date", date)
                .toString();
    }
}
