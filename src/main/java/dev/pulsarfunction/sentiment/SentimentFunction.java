package dev.pulsarfunction.sentiment;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import java.io.IOException;
import java.util.UUID;

/**
 text function for chat

 * DistilBERT model trained by HuggingFace using PyTorch
 *
 https://github.com/awslabs/djl/blob/master/examples/src/main/java/ai/djl/examples/inference/SentimentAnalysis.java
 https://github.com/awslabs/djl/blob/master/examples/docs/sentiment_analysis.md
**/
public class SentimentFunction implements Function<byte[], Void> {

    /**
     *
     * @param message
     * @return
     * @throws IOException
     * @throws TranslateException
     * @throws ModelException
     */
    private Result predict(String message) throws IOException, TranslateException, ModelException {
        Result result = new Result();

        if ( message == null || message.trim().length() <=0 ) {
            return result;
        }

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .optEngine( "PyTorch" )
                        .setTypes(String.class, Classifications.class)
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<String, Classifications> predictor = model.newPredictor()) {
                Classifications classifications = predictor.predict(message);
                if ( classifications == null) {
                    return result;
                }
                else {
                    if ( classifications.items() != null && classifications.items().size() > 0) {
                        if (  classifications.topK(5) != null ) {
                            result.setRawClassification( classifications.topK( 5 ).toString() );
                        }
                        for (Classifications.Classification classification : classifications.items()) {
                            try {
                                if (classification != null) {
                                    if ( classification.getClassName().equalsIgnoreCase( "positive" )) {
                                        result.setProbability( classification.getProbability() );
                                        result.setProbabilityPercentage( (classification.getProbability()*100) );
                                    }
                                    else if ( classification.getClassName().equalsIgnoreCase( "negative" )) {
                                        result.setProbabilityNegative( classification.getProbability() );
                                        result.setProbabilityNegativePercentage( (classification.getProbability()*100) );
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
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

        try {
            result = predict(chat.getComment());
        } catch (Throwable e) {
            e.printStackTrace();
            if ( context != null && context.getLogger() != null) {
                context.getLogger().error("ERROR:" + e.getLocalizedMessage());
            }
        }

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

        try {
            if ( context != null && context.getTenant() != null ) {
                context.newOutputMessage(outputTopic, JSONSchema.of(Chat.class))
                        .key(UUID.randomUUID().toString())
                        .property("language", "Java")
                        .property("engine", "PYTorch")
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
