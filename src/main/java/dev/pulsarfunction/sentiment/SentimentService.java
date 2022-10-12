package dev.pulsarfunction.sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.ModelNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.StringJoiner;

/**
 *
 */
public class SentimentService {
    private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

//    public Classifications predict(String input)
//            throws MalformedModelException, ModelNotFoundException, IOException,
//            TranslateException {
//        this.log.info("input Sentence: {}", input);
//
//        Criteria<String, Classifications> criteria =
//                Criteria.builder()
//                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
//                        .setTypes(String.class, Classifications.class)
//                        // This model was traced on CPU and can only run on CPU
//                        .optDevice(Device.cpu())
//                        .optProgress(new ProgressBar())
//                        .build();
//
//        try (ZooModel<String, Classifications> model = criteria.loadModel();
//             Predictor<String, Classifications> predictor = model.newPredictor()) {
//            return predictor.predict(input);
//        }
//    }
//
//    public String getSentiment(String rawText) {
//        Classifications classifications = null;
//        try {
//            classifications = predict(rawText);
//        } catch (MalformedModelException e) {
//            e.printStackTrace();
//        } catch (ModelNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TranslateException e) {
//            e.printStackTrace();
//        }
//        if ( classifications == null) {
//            return "DJL.AI Sentiment Model has failed.";
//        }
//        this.log.info(classifications.toString());
//
//        Classifications.Classification classPositive = classifications.get("Positive");
//        Classifications.Classification classNegative = classifications.get("Negative");
//
//        this.log.info("Pos: {} = {}",classPositive.getClassName(),classPositive.getProbability());
//        this.log.info("Neg: {} = {}",classNegative.getClassName(),classNegative.getProbability());
//
//        StringJoiner outputString = new StringJoiner(", ",
//                "", "");
//
//        outputString.add(rawText);
//
//        if ( classPositive.getProbability() > classNegative.getProbability()) {
//            this.log.info("Sentiment is positive");
//            outputString.add("Positive");
//        }
//        else if (classNegative.getProbability() > classPositive.getProbability())
//        {
//            this.log.info("Sentiment is negative");
//            outputString.add("Negative");
//        }
//        else {
//            this.log.info("Sentiment is neutral");
//            outputString.add("Neutral");
//        }
//
//        return outputString.toString();
//    }
}