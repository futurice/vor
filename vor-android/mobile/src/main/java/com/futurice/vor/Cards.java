package com.futurice.vor;

import android.content.Context;
import android.graphics.Color;

import com.futurice.vor.card.ITopic;
import com.futurice.vor.card.ImageCard;
import com.futurice.vor.card.Topic;
import com.futurice.vor.utils.VorUtils;

import java.util.Date;
import java.util.Random;

import static com.futurice.vor.Constants.*;

public class Cards {

    /**
     * Pool card
     * @param file the file in Base64
     * @return the pool topic
     */
    public static ITopic pool(String file, Context context) {
        final Topic topic = new Topic("Pool", 140, context, POOL_KEY);
        topic.setText("Are you up for a game?");
        topic.setIsPrebuiltTopic(true);
        topic.setCardType(POOL_KEY);
        topic.setImageUri(VorUtils.getResourceUri(R.raw.card_pool));

        ImageCard card = new ImageCard("__", 440, context);
        card.setCardType(POOL_KEY);
        card.setText("Come and play!");
        card.setAuthor("Vör", "V001");
        card.setDate(new Date());
        card.setImageBase64(file);

        topic.addCard(card);
        return topic;
    }

    /**
     * Food card
     * @param file the file in Base64
     * @return the food topic
     */
    public static ITopic food(String file, Context context) {
        final Topic topic = new Topic("Food", 240, context, FOOD_KEY);
        topic.setText("Check what's on FutuCafé table");
        topic.setIsPrebuiltTopic(true);
        topic.setImageUri(VorUtils.getResourceUri(R.raw.card_food));

        ImageCard card = new ImageCard("__", 540, context);
        card.setCardType(FOOD_KEY);
        card.setText("Bon Appétit!");
        card.setAuthor("Vör", "V001");
        card.setDate(new Date());
        card.setImageBase64(file);

        topic.addCard(card);
        return topic;
    }

    /**
     * Sauna card
     * @param status status of the sauna, ON or OFF
     * @return the topic
     */
    public static ITopic sauna(String status, Context context) {
        final Topic topic = new Topic("Sauna", 1250, context, SAUNA_KEY);
        topic.setText("Sauna is " + status);
        topic.setColor(R.color.blueDark);
        topic.setIsPrebuiltTopic(true);

        ImageCard card = new ImageCard("__", 1550, context);
        card.setText("Just to inform that the sauna is " + status);
        card.setAuthor("Vör", "V001");
        card.setDate(new Date());

        topic.addCard(card);
        return topic;
    }

    /**
     * Track your item card
     * @param item the item being tracked
     * @return the topic
     */
    public static ITopic trackItem(String item, Context context) {
        final Topic topic = new Topic("TrackItem", 1350, context, TRACK_ITEM_KEY);
        topic.setText("Track the item " + item);
        topic.setColor(R.color.green);
        topic.setIsPrebuiltTopic(true);

        ImageCard card = new ImageCard("__", 1650, context);
        card.setText("You're tracking the item " + item);
        card.setAuthor("Vör", "V001");
        card.setDate(new Date());

        topic.addCard(card);
        return topic;
    }


    /**
     * Workspace card
     * @param message the message (?)
     * @return the workspace topic
     */
    public static ITopic workspace(String message, Context context) {
        final Topic topic = new Topic("Workspace", 280, context, WORKSPACE_KEY);
        topic.setText("One of your workspaces is now free");
        topic.setIsPrebuiltTopic(true);

        ImageCard card = new ImageCard("__", 580, context);
        card.setText("One of your workspaces is now free");
        card.setAuthor("Futu2", "Futu2");
        card.setDate(new Date());
        card.setImageUri(VorUtils.getResourceUri(R.raw.card_workspace));

        topic.addCard(card);
        return topic;
    }

    /**
     * Test card, to be removed
     * Example JSON: { type: "test", message: "Hello World!" }
     *
     * @return the test topic
     */
    public static ITopic test(String message, Context context) {
        final Topic topic = new Topic("Test", 1450, context, TEST_KEY);
        topic.setText(message);
        topic.setColor(getRandomColor());
        topic.setIsPrebuiltTopic(true);

        ImageCard card = new ImageCard("__", 1750, context);
        card.setText(message);
        card.setAuthor("Vör", "V001");
        card.setDate(new Date());

        topic.addCard(card);
        return topic;
    }

    private static int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
