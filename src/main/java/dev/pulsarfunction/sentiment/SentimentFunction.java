package dev.pulsarfunction.sentiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import java.io.IOException;
import java.util.UUID;

/**
 *
 */
public class SentimentFunction implements Function<byte[], Void> {


    /**
     * parse Chat JSON Message into Class
     *
     * todo  Make a schema and find how to attach to websockets
     * @param message String of message
     * @return Chat Message
     */
    private Chat parseMessage(String message) {
        Chat chatMessage = new Chat();
        if ( message == null) {
            return chatMessage;
        }

        try {
            if ( message.trim().length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                chatMessage = mapper.readValue(message, Chat.class);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (chatMessage == null) {
            chatMessage = new Chat();
        }
        return chatMessage;
    }

    /** PROCESS */
    @Override
    public Void process(byte[] input, Context context) {
        if ( input == null) {
            return null;
        }
        // @TODO:  Fix.  maybe pass in
        String outputTopic = "persistent://public/default/sentimentresults";

        if (context != null && context.getLogger() != null && context.getLogger().isDebugEnabled()) {
            context.getLogger().debug("LOG:" + input.toString());

            System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");

            context.getLogger().debug("Available processors (cores): " +
                    Runtime.getRuntime().availableProcessors());

            /* Total amount of free memory available to the JVM */
            context.getLogger().debug("Free memory (bytes): " +
                    Runtime.getRuntime().freeMemory());

            /* This will return Long.MAX_VALUE if there is no preset limit */
            long maxMemory = Runtime.getRuntime().maxMemory();

            /* Maximum amount of memory the JVM will attempt to use */
            context.getLogger().debug("Maximum memory (bytes): " +
                    (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

            /* Total memory currently available to the JVM */
            context.getLogger().debug("Total memory available to JVM (bytes): " +
                    Runtime.getRuntime().totalMemory());
        }

        Result result = null;
        Chat chat = parseMessage(new String(input));

//        try {
//            result = predict(chat.getComment());
//        } catch (Throwable e) {
//            e.printStackTrace();
//            if ( context != null && context.getLogger() != null) {
//                context.getLogger().error("ERROR:" + e.getLocalizedMessage());
//            }
//        }

        if ( result != null && result.getRawClassification() != null) {
            String sentiment = "Neutral";
            if (result.getProbability() > result.getProbabilityNegative()) {
                sentiment = "Positive";
            }
            else if (result.getProbability() < result.getProbabilityNegative()) {
                sentiment = "Negative";
            }

            if ( context != null && context.getLogger() != null) {
                context.getLogger().info("sentiment-" + sentiment);
            }

            chat.setPrediction( String.format( "%s", sentiment));
        }
        else {
            chat.setPrediction( "Neutral");
        }

//        if (record.getEventTime().isPresent()) {
//            context.recordMetric("MessageEventTime", record.getEventTime().get().doubleValue());
//        }

        try {
            if ( context != null && context.getTenant() != null ) {
                context.newOutputMessage(outputTopic, JSONSchema.of(Chat.class))
                        .key(UUID.randomUUID().toString())
                        .property("language", "Java")
                        .property("processor", "sentiment")
                        .value(chat)
                        .send();
            }
            else {
                System.out.println("Null context, assuming local test run. " + chat.toString());
            }
        } catch (Throwable e) {
            if (context != null  && context.getLogger() != null) {
                context.getLogger().error("ERROR:" + e.getLocalizedMessage());
            }
            else {
                e.printStackTrace();
            }
        }

        return null;
    }
} 