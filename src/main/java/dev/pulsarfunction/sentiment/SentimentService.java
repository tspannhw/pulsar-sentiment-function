package dev.pulsarfunction.sentiment;

import ai.djl.repository.zoo.ModelZoo;
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

    private static Predictor<String, Classifications> predictor;

    private Predictor<String, Classifications> getOrCreatePredictor()
            throws ModelException, IOException {
        if (predictor == null) {
            Criteria<String, Classifications> criteria =
                    Criteria.builder()
                            .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                            .setTypes(String.class, Classifications.class)
                            .optProgress(new ProgressBar())
                            .build();
            ZooModel<String, Classifications> model = criteria.loadModel();
            predictor = model.newPredictor();
        //.optDevice(Device.cpu())
        }
        return predictor;
    }
    public Result getSentiment(String rawText) {
        Result result = new Result();
        Classifications classifications = null;
        try {

            Predictor<String, Classifications> predictor = null;
            try {
                predictor = getOrCreatePredictor();
            } catch (ModelException e) {
                throw new RuntimeException(e);
            }
            classifications = predictor.predict(rawText);
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage());
        }
        if ( classifications == null) {
            result.setRawClassification("DJL.AI Sentiment Model has failed.");
            return result;
        }
        this.log.debug(classifications.toString());
        result.setRawClassification(classifications.toString());
        Classifications.Classification classPositive = classifications.get("Positive");
        Classifications.Classification classNegative = classifications.get("Negative");

        this.log.debug("Pos: {} = {}",classPositive.getClassName(),classPositive.getProbability());
        this.log.debug("Neg: {} = {}",classNegative.getClassName(),classNegative.getProbability());

        result.setProbabilityNegative(classNegative.getProbability());
        result.setProbabilityNegativePercentage( classNegative.getProbability() * 100 );
        result.setProbability(classPositive.getProbability());
        result.setProbabilityPercentage(classPositive.getProbability() * 100);

        String sentimentValue = "";

        if ( classPositive.getProbability() > classNegative.getProbability()) {
            sentimentValue = "Positive";
        }
        else if (classNegative.getProbability() > classPositive.getProbability())
        {
            sentimentValue = "Negative";
        }
        else {
            sentimentValue = "Neutral";
        }

        result.setSentimentValue(sentimentValue);

        return result;
    }
}