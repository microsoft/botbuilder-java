package com.microsoft.bot.dialogs;

public class Foo {
    private String name;
    private int age;
    private boolean cool;
    private Bar subname;

    public Foo() {

    }

    public Foo(String name, int age, boolean cool, Bar subname) {
        this.name = name;
        this.age = age;
        this.cool = cool;
        this.subname = subname;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public boolean getCool() {
        return this.cool;
    }

    public Bar getSubname() {
        return this.subname;
    }

}
