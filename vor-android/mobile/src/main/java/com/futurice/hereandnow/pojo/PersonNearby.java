package com.futurice.hereandnow.pojo;

/**
 * Created by Luís Ramalho on 21/12/15.
 * <luis.ramalho@futurice.com>
 */
public class PersonNearby {
    private String mName;
    private int mDistance;

    public PersonNearby(String name, int distance) {
        mName = name;
        mDistance = distance;
    }

    public String getName() {
        return mName;
    }

    public Integer getDistance() {
        return mDistance;
    }
}
