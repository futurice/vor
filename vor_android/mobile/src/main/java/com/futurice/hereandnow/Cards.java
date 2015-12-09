package com.futurice.hereandnow;

import android.content.Context;
import android.graphics.Color;

import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.ImageCard;
import com.futurice.hereandnow.card.Topic;

import java.util.Date;
import java.util.Random;

public class Cards {

    /**
     * Sauna card
     * @param status status of the sauna, ON or OFF
     * @return the topic
     */
    public static ITopic sauna(String status, Context context) {
        final Topic topic = new Topic("Sauna", 1250, context);
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
        final Topic topic = new Topic("Sauna", 1350, context);
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
     * Test card, to be removed
     * Example JSON: { type: "test", message: "Hello World!" }
     *
     * @return the test topic
     */
    public static ITopic test(String message, Context context) {
        final Topic topic = new Topic("Test", 1450, context);
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
