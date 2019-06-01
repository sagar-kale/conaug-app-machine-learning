package com.conaug.conaugApp.analyzers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentimentAnalyzer {

    public TweetWithSentiment findSentiment(String line) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        int mainSentiment = 0;
        if (line != null && line.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(line);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }

        if (mainSentiment == 2) {
            System.out.println("Average");
        } else if (mainSentiment > 2) {
            System.out.println("Positive");
        } else if (mainSentiment < 2) {
            System.out.println("Negative ");
        }

        if (mainSentiment == 2 || mainSentiment > 4 || mainSentiment < 0) {
            //return null;
        }

        System.out.println(line);
        TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(line, toCss(mainSentiment));
        return tweetWithSentiment;

    }

    private String toCss(int sentiment) {
        switch (sentiment) {
            case 0:
                return "alert alert-danger";
            case 1:
                return "alert alert-danger";
            case 2:
                return "alert alert-warning";
            case 3:
                return "alert alert-success";
            case 4:
                return "alert alert-success";
            default:
                return "";


        }
    }

    public static void main(String[] args) {
        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
        TweetWithSentiment tweetWithSentiment = sentimentAnalyzer
                .findSentiment("The Philips slow juicer meets all expectations! The juice is easily extracted from all possible fruits. Even \"firmer\" fruits such as apples release all the juice and there is little waste.\n" +
                        "It is very handy that all parts can be put in the dishwasher. \n" +
                        "The anti-leak valve does not always work as well, but it is handy that it is there. \n" +
                        "The enclosed recipe book is inspiring and we have often used it to try out new juices. \n" +
                        "The device makes quite a bit of noise, but this is not a barrier in use. The design of the device is very modern, especially the compact size is very handy, the device can be placed on the kitchen countertop without any problems. We use the slow juicer several times a day for the whole family!");
        System.out.println(tweetWithSentiment);
    }
}