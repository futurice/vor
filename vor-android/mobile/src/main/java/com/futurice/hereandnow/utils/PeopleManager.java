package com.futurice.hereandnow.utils;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

public class PeopleManager {
    private static final float GOLDEN_RATIO_CONJUGATE = 0.618033988749895f;
    private static final float COLOR_SATURATION = 0.5f;
    private static final float COLOR_VALUE = 0.95f;

    private float hue;

    ArrayList<Person> people;

    public PeopleManager() {
        this.people = new ArrayList<>();
        hue = -1f;
    }

    /**
     * Add a new person to the manager.
     * @param email email of the person.
     */
    public void addPerson(String email) {
        Person person = new Person(people.size(), email);
        person.setColor(getNewColor());
        this.people.add(person);
    }

    /**
     * Given an email checks if the person exists in the manager.
     * @param email email of the person.
     * @return True if it exists, false if it doesn't.
     */
    public Boolean exists(String email) {
        for (Person person : people) {
            if (email.equals(person.getEmail())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a person object with a given email address.
     * @param email email to look for.
     * @return Person object if found, null otherwise.
     */
    public Person getPerson(String email) {
        for (int i = 0; i < people.size(); i++) {
            if (people.get(i).getEmail().equals(email)) {
                return people.get(i);
            }
        }

        return null;
    }

    /**
     * Calculate a unique color for the marker.
     *
     * http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
     * @return unique color.
     */
    private int getNewColor() {
        if (hue == -1f) {
            // Initial starting value for the hue.
            hue = (float) Math.random();
        }
        hue += GOLDEN_RATIO_CONJUGATE;
        hue %= 1f;

        float[] hsv = new float[3];
        hsv[0] = hue * 360f; // Hue range in HSVToColor [0, 360]
        hsv[1] = COLOR_SATURATION;
        hsv[2] = COLOR_VALUE;

        return Color.HSVToColor(hsv);
    }

    /**
     * Get a list of every person on the application.
     * @return List of Person objects.
     */
    public ArrayList<Person> getPeople() {
        return this.people;
    }

    /**
     * Filter people based on an argument.
     * @param email What to search for.
     * @return New list of people matching the argument.
     */
    public ArrayList<Person> filterPeople(String email) {
        ArrayList<Person> filteredPeople = new ArrayList<>();

        for (Person person : people) {
            if (person.getEmail().contains(email.toLowerCase())) {
                filteredPeople.add(person);
            }
        }

        return filteredPeople;
    }

    /**
     * Class for keeping track of a person's location and color on the map.
     *
     */
    public class Person {
        private int id;
        private String email;
        private Integer color;
        private Paint paint;

        // Pixel coordinates in the map.
        float mapLocationX, mapLocationY;

        // Desired location on the screen.
        float locationOnScreenX, locationOnScreenY;

        // Current location on the screen (for animation)
        float currentLocationX, currentLocationY;

        public Person(int id, String email) {
            this.id = id;
            this.email = email;
            mapLocationX = mapLocationY = -1f;
            locationOnScreenX = locationOnScreenY = -1f;
            currentLocationX = currentLocationY = -1f;

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN); //default color.
        }

        public void setColor(Integer color) {
            this.color = color;
            paint.setColor(color);
        }

        public int getId() {
            return this.id;
        }

        public void setLocation(float newX, float newY) {
            this.mapLocationX = newX;
            this.mapLocationY = newY;
        }

        public float getMapLocationX() {
            return this.mapLocationX;
        }

        public float getMapLocationY() {
            return this.mapLocationY;
        }

        public float getLocationOnScreenX() {
            return this.locationOnScreenX;
        }

        public float getLocationOnScreenY() {
            return this.locationOnScreenY;
        }

        public float getCurrentLocationX() {
            return this.currentLocationX;
        }

        public float getCurrentLocationY() {
            return this.currentLocationY;
        }

        public String getEmail() {
            return this.email;
        }

        public Integer getColor() {
            return this.color;
        }

        public Paint getPaint() {
            return this.paint;
        }

        /**
         * Calculate new location on the display.
         * @param newX the new X coordinate.
         * @param newY the new Y coordinate.
         * @param moveEvent True if the method is called during a move event, false otherwise
         */
        public void setDisplayedLocation(float newX, float newY, Boolean moveEvent) {
            if (moveEvent) {
                float differenceX = Math.abs(newX - locationOnScreenX);
                float differenceY = Math.abs(newY - locationOnScreenY);

                if (newX > locationOnScreenX) {
                    currentLocationX += differenceX;
                } else if (newX < locationOnScreenX) {
                    currentLocationX -= differenceX;
                }

                if (newY > locationOnScreenY) {
                    currentLocationY += differenceY;
                } else if (newY < locationOnScreenY) {
                    currentLocationY -= differenceY;
                }
            }

            locationOnScreenX = newX;
            locationOnScreenY = newY;
        }

        /**
         * Incremeent the current location to be nearer to the desired location.
         * @param animationSpeed factor for how quick the animation is.
         * @param updateRadius area where the increment isn't necessary.
         */
        public void updateCurrentLocation(float animationSpeed, float updateRadius) {
            float distanceX = Math.abs(locationOnScreenX - currentLocationX);
            float distanceY = Math.abs(locationOnScreenY - currentLocationY);

            if (distanceX > updateRadius) {
                if (currentLocationX < locationOnScreenX) {
                    currentLocationX += (animationSpeed * distanceX);
                } else if (currentLocationX > locationOnScreenX) {
                    currentLocationX -= (animationSpeed * distanceX);
                }
            }

            if (distanceY > updateRadius) {
                if (currentLocationY < locationOnScreenY) {
                    currentLocationY += (animationSpeed * distanceY);
                } else if (currentLocationY > locationOnScreenY) {
                    currentLocationY -= (animationSpeed * distanceY);
                }
            }
        }
    }
}
