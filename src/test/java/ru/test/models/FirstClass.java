package ru.test.models;


import ru.test.annotation.Id;

public class FirstClass {
    @Id
    private final String key;

    public FirstClass(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}