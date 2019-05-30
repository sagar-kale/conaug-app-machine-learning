package com.conaug.conaugApp.service;

import com.conaug.conaugApp.model.Feedback;
import com.conaug.conaugApp.model.Response;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.*;
import opennlp.tools.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ParseServiceImpl implements ParseService {
    private DoccatModel model;

    /*@Override
    public Response parseData(Feedback feedback) {
        Response response = new Response();

        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"
        );
        lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

        Tree parse = lp.parse(feedback.getData());
        double score = parse.score();
        List<String> taggedList = new ArrayList<>();
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
        for (TaggedWord taggedWord : taggedWords) {
            taggedList.add(taggedWord.word() + "/" + taggedWord.tag());
        }
        log.info("Tagged List {}", taggedList);

        //response.setResponse(taggedList);
        response.setScore(score);
        response.setStatus(HttpStatus.OK.toString());
        response.setStatusCode(HttpStatus.OK.value());
        log.info("tagged words {}", taggedWords);
        log.info("score:: {}", score);
        return response;
    }

    void parseSentiments(String text) {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        //   CoreDocument document = new CoreDocument(text);
        Annotation document = new Annotation(text);
        // annnotate the document
        pipeline.annotate(document);
        // examples

        // 10th token of the document
//        CoreLabel token = document.tokens().get(10);
        System.out.println("Example: token");
        //      System.out.println(token);
        System.out.println();

    }
*/
    @Override
    public void trainModel() {
        int minNgramSize = 2;
        int maxNgramSize = 3;
        DoccatFactory doccatFactory = null;
        try {
            doccatFactory = new DoccatFactory(
                    new FeatureGenerator[]{
                            new BagOfWordsFeatureGenerator(),
                            new NGramFeatureGenerator(minNgramSize, maxNgramSize)
                    }
            );
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        MarkableFileInputStreamFactory inputStreamFactory = null;
        try {
            inputStreamFactory = new MarkableFileInputStreamFactory(ResourceUtils.getFile("classpath:categorical-stanford-train.txt"));
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
            // Specifies the minimum number of times a feature must be seen
            int cutoff = 0;
            int trainingIterations = 100;
            TrainingParameters trainingParameters = new TrainingParameters();
            trainingParameters.put(TrainingParameters.CUTOFF_PARAM, cutoff);
            trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, trainingIterations);
            model = DocumentCategorizerME.train("en", sampleStream, trainingParameters, doccatFactory);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Response classifyFeedback(Feedback feedback) {
        Response response = new Response();
        DocumentCategorizer documentCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = documentCategorizer.categorize(feedback.getData().replaceAll("[^A-Za-z0-9 ]", " ").split(" "));
        log.info("outcomes::: {}", Arrays.asList(outcomes));
        String category = documentCategorizer.getBestCategory(outcomes);

        Map<String, Double> map = new HashMap<>();
        // print the probabilities of the categories
        log.info("\n---------------------------------\nCategory : Probability\n---------------------------------");
        for (int i = 0; i < documentCategorizer.getNumberOfCategories(); i++) {
            map.put(documentCategorizer.getCategory(i), outcomes[i]);
            log.info(documentCategorizer.getCategory(i) + " : " + outcomes[i]);
        }
        log.info("---------------------------------");
        log.info("\n" + documentCategorizer.getBestCategory(outcomes) + " : is the predicted category for the given sentence.");


        response.setStatus(HttpStatus.OK.toString());
        response.setStatusCode(HttpStatus.OK.value());

        if (category.equalsIgnoreCase("love")) {
            response.setRating("BEST");
            response.setScore(5);
        } else if (category.equalsIgnoreCase("like")) {
            response.setRating("GOOD");
            response.setScore(4);
        } else if (category.equalsIgnoreCase("sad")) {
            response.setRating("Bad");
            response.setScore(2);
        } else if (category.equalsIgnoreCase("neutral")) {
            response.setRating("Average ");
            response.setScore(3);
        } else if (category.equalsIgnoreCase("angry")) {
            response.setRating("Really Bad");
            response.setScore(1);
        }
        response.setRating_probability(map);
        return response;
    }
}
