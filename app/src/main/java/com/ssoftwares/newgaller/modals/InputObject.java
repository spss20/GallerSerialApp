package com.ssoftwares.newgaller.modals;

public class InputObject {
    private int id;
    private boolean value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InputObject(int id, boolean value) {
        this.id = id;
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
