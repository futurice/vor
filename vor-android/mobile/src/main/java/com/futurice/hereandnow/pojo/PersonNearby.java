package com.futurice.hereandnow.pojo;

import java.util.Comparator;

/**
 * Created by Luís Ramalho on 21/12/15.
 * <luis.ramalho@futurice.com>
 */
public class PersonNearby {
    private String mName;
    private double mDistance;

    public PersonNearby(String name, double distance) {
        mName = name;
        mDistance = distance;
    }

    public String getName() {
        return mName;
    }

    public Double getDistance() {
        return mDistance;
    }

    public static class PersonComparator implements Comparator<PersonNearby> {
        @Override
        public int compare(PersonNearby left, PersonNearby right) {
            return left.getDistance().compareTo(right.getDistance());
        }
    }
}
