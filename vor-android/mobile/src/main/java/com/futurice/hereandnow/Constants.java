package com.futurice.hereandnow;

/**
 * Created by pper on 16/04/15.
 */
public class Constants {
    // Had to change these from an enum to int to work with PersistentValue storage mode limitations. Can change back later
    public static final int CARDS_HAPPENING_NOW = 0;
    public static final int CARDS_TRENDING = 1;
    public static final int CARDS_MY_CARDS = 2;
    public static final int CARDS_NUMBER_OF_TABS = 3;

    public static final int MAP_7TH_FLOOR = 0;
    public static final int MAP_8TH_FLOOR = 1;
    public static final int MAPS_NUMBER_OF_TABS = 2;

//    public static final String SERVER_URL = "http://rubix.futurice.com/";
    public static final String SERVER_URL = "http://192.168.0.107:8080/";

    public static final String TYPE_KEY = "type";
    public static final String ID_KEY = "id";

    public static final String LOCATION_KEY = "location";
    public static final String FLOOR_KEY = "floor";
    public static final String MESSAGE_KEY = "message";

    public static final String EVENT_INIT = "init";

    public static final String TOILET_KEY = "toilet";
    public static final String SAUNA_KEY = "sauna";
    public static final String FOOD_KEY = "food";
    public static final String IMAGE_KEY = "image";
    public static final String POOL_KEY = "pool";
    public static final String TRACK_ITEM_KEY = "track";
    public static final String RESERVED_KEY = "reserved";
    public static final String METHANE_KEY = "methane";

    public static final String EMAIL = "email";
    public static final String DUMMY_EMAIL = "first.last@futurice.com";

    // Location service
    public static final String NETWORK_SSID = "\"Futurice-Helsinki\"";

    // To be removed
    public static final String TEST_KEY = "test";
}
