package com.microsoft.bot.dialogs;

public class Bar {
    private String name;
    private int age;
    private boolean cool;

    public Bar() {

    }

    public Bar(String name, int age, boolean cool) {
        this.name = name;
        this.age = age;
        this.cool = cool;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public boolean getCool() {
        return cool;
    }
}
