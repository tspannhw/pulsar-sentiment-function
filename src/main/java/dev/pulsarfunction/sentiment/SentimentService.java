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

    public Classifications predict(String input)
            throws MalformedModelException, ModelNotFoundException, IOException,
            TranslateException {
        this.log.debug("input Sentence: {}", input);

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .setTypes(String.class, Classifications.class)
                        .optDevice(Device.cpu())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = criteria.loadModel();
             Predictor<String, Classifications> predictor = model.newPredictor()) {
            return predictor.predict(input);
        }
    }

    public Result getSentiment(String rawText) {
        Result result = new Result();
        Classifications classifications = null;
        try {
            classifications = predict(rawText);
        } catch (MalformedModelException e) {
            log.error(e.getLocalizedMessage());
        } catch (ModelNotFoundException e) {
            log.error(e.getLocalizedMessage());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        } catch (TranslateException e) {
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