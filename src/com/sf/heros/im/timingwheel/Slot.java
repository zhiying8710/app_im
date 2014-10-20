package com.sf.heros.im.timingwheel;

import java.util.Set;

public abstract class Slot {
    private int id;
    private String key;

    public Slot(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void add(String e);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public abstract boolean remove(String e);

    public abstract Set<String> elements();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Slot other = (Slot) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
