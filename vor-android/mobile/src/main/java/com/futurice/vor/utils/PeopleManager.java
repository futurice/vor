package com.futurice.vor.utils;

import android.graphics.Paint;

import java.util.ArrayList;

public class PeopleManager {
    private static final long ONE_MINUTE = 60 * 1000;

    ArrayList<Person> people;

    public PeopleManager() {
        this.people = new ArrayList<>();
    }

    /**
     * Add a new person to the manager.
     * @param email email of the person.
     */
    public void addPerson(String email) {
        Person person = new Person(people.size(), email);
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
     * Get a list of every person on the application.
     * @return List of Person objects.
     */
    public ArrayList<Person> getPeople() {
        return this.people;
    }

    /**
     * Filter people based on an argument.
     * @param filter What to search for.
     * @return New list of people matching the argument.
     */
    public ArrayList<Person> filterPeople(String filter) {
        ArrayList<Person> filteredPeople = new ArrayList<>();
        filter = filter.toLowerCase();

        for (Person person : people) {
            if (VorUtils.getName(person.getEmail()).toLowerCase().contains(filter)) {
                filteredPeople.add(person);
            }
        }

        return filteredPeople;
    }

    /**
     * Get people that are located in a given floor.
     * @param floor floor number.
     * @return A new list of people located in the floor.
     */
    public ArrayList<Person> getPeopleWithFloor(int floor) {
        ArrayList<Person> filteredPeople = new ArrayList<>();

        for (Person person: people) {
            if (person.getFloor() == floor) {
                filteredPeople.add(person);
            }
        }

        return filteredPeople;
    }

    /**
     * Filter people based on an argument
     * @param filter What to search floor.
     * @param floor In which floor.
     * @return New list of people matching the arguments.
     */
    public ArrayList<Person> filterPeopleWithFloor(String filter, int floor) {
        ArrayList<Person> filteredPeople = new ArrayList<>();
        filter = filter.toLowerCase();

        for (Person person : people) {
            if (VorUtils.getName(person.getEmail()).toLowerCase().contains(filter)
                    && person.getFloor() == floor) {
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
        private static final float OLD_LOCATION_AVERAGE_FACTOR = 0.7f;
        private static final float NEW_LOCATION_AVERAGE_FACTOR = 0.3f;
        private static final float MAXIMUM_WALKING_DISTANCE = 200f;

        private int id;
        private String email;
        private Integer color;
        private Paint paint;
        boolean clicked;
        boolean freshLocationValue;

        // Pixel coordinates in the map.
        float mapLocationX, mapLocationY;

        // Desired location on the screen.
        float locationOnScreenX, locationOnScreenY;

        // Current location on the screen (for animation)
        float currentLocationX, currentLocationY;

        // The real location in meters for calculating distances to other people.
        float meterLocationX, meterLocationY;

        // Current floor for the person.
        int floor;

        // Last location update for filtering the data.
        float previousLocationOnScreenX, previousLocationOnScreenY;

        // Time since last updated.
        long lastUpdated;

        public Person(int id, String email) {
            this.id = id;
            this.email = email;
            freshLocationValue = true;
            mapLocationX = mapLocationY = -1f;
            locationOnScreenX = locationOnScreenY = -1f;
            currentLocationX = currentLocationY = -1f;

            paint = new Paint();
            paint.setAntiAlias(true);
            color = null;
            clicked = false;
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

        public void setLocationInMeters(float newX, float newY) {
            this.meterLocationX = newX;
            this.meterLocationY = newY;
        }

        public void setClicked(boolean clicked) {
            this.clicked = clicked;
        }

        public void setFloor(int floor) {
            this.floor = floor;
        }

        public void setLastUpdated(long update) { this.lastUpdated = update; }

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

        public float getMeterLocationX() { return this.meterLocationX; }

        public float getMeterLocationY() { return this.meterLocationY; }

        public int getFloor() { return this.floor; }

        public String getEmail() {
            return this.email;
        }

        public Integer getColor() {
            return this.color;
        }

        public Paint getPaint() {
            return this.paint;
        }

        public boolean isClicked() {
            return this.clicked;
        }

        public boolean isFreshLocationValue() {
            return this.freshLocationValue;
        }

        /**
         * Set new location for the map. Apply basic filtering for the received location.
         * @param newX X coordinate of the new location.
         * @param newY Y coordinate of the new location.
         * @param moveEvent True if the event is called with a new location, otherwise (e.g. zooming)
         *                  false.
         */
        public void setLocation(float newX, float newY, boolean moveEvent) {
            if ((previousLocationOnScreenX <= 0 && previousLocationOnScreenY <= 0) || !moveEvent) {
                mapLocationX = newX;
                mapLocationY = newY;
                return;
            }

            previousLocationOnScreenX = mapLocationX;
            previousLocationOnScreenY = mapLocationY;

            final float[] averagedValues = calculateAverageLocation(newX, newY);

            final float differenceX = Math.abs(averagedValues[0] - previousLocationOnScreenX);
            final float differenceY = Math.abs(averagedValues[1] - previousLocationOnScreenY);

            if (differenceX > MAXIMUM_WALKING_DISTANCE) {
                if (averagedValues[0] > previousLocationOnScreenX) {
                    mapLocationX += MAXIMUM_WALKING_DISTANCE;
                } else if (averagedValues[0] < previousLocationOnScreenX) {
                    mapLocationX -= MAXIMUM_WALKING_DISTANCE;
                }

            } else {
                mapLocationX = averagedValues[0];
            }

            if (differenceY > MAXIMUM_WALKING_DISTANCE) {
                if (averagedValues[1] > previousLocationOnScreenY) {
                    mapLocationY += MAXIMUM_WALKING_DISTANCE;
                } else if (averagedValues[1] < previousLocationOnScreenY) {
                    mapLocationY -= MAXIMUM_WALKING_DISTANCE;
                }

            } else {
                mapLocationY = averagedValues[1];
            }
        }

        /**
         * Calculate new location on the display.
         * @param newX the new X coordinate.
         * @param newY the new Y coordinate.
         * @param moveEvent True if the method is called during a move event, false otherwise
         */
        public void setDisplayedLocation(float newX, float newY, Boolean moveEvent) {
            if (moveEvent) {
                final float differenceX = Math.abs(newX - locationOnScreenX);
                final float differenceY = Math.abs(newY - locationOnScreenY);

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

            if (updateValueIsOld()) {
                freshLocationValue = false;
            } else {
                freshLocationValue = true;
            }
        }

        /**
         * Set current location on screen directly to a specific value.
         * @param x New X coordinate.
         * @param y New Y coordinate.
         */
        public void setCurrentLocation(float x, float y) {
            currentLocationX = x;
            currentLocationY = y;
        }

        /**
         * Calculate a weighted average of the location.
         * @param newX X coordinate of the new location.
         * @param newY Y coordinate of the new location.
         */
        private float[] calculateAverageLocation(float newX, float newY) {
            // No previous location set, so return just the new one.
            if (previousLocationOnScreenX <= 0 || previousLocationOnScreenY <= 0) {
                return new float[] { newX, newY };
            }

            final float averagedX = OLD_LOCATION_AVERAGE_FACTOR * previousLocationOnScreenX
                    + NEW_LOCATION_AVERAGE_FACTOR * newX;
            final float averagedY = OLD_LOCATION_AVERAGE_FACTOR * previousLocationOnScreenY
                    + NEW_LOCATION_AVERAGE_FACTOR * newY;

            return new float[] { averagedX, averagedY };
        }

        private boolean updateValueIsOld() {
            long difference = System.currentTimeMillis() - lastUpdated;
            return difference > ONE_MINUTE;
        }
    }
}
