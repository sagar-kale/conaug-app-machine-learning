package com.conaug.conaugApp.service;

import com.conaug.conaugApp.model.Feedback;
import com.conaug.conaugApp.model.Response;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.doccat.*;
import opennlp.tools.namefind.*;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class ParseServiceImpl implements ParseService {

    //    @Value("gs://spring-boot-feedback-mgmt/categorical-stanford-train.txt")
    @Value("classpath:categorical-stanford-train.txt")
    private Resource resource;
    @Value("classpath:en-ner-organization.bin")
    private Resource org_model;
    @Value("classpath:en-token.bin")
    private Resource token_model;
    private DoccatModel model;
    private TokenNameFinderModel tokenNameFinderModel;

    @Override
    public Response parseData(Feedback feedback) {

        Response response = new Response();
        identifyOrg(feedback);
        /*Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,depparse,openie");
        // example customizations (these are commented out but you can uncomment them to see the results

        // disable fine grained ner
        props.setProperty("ner.applyFineGrained", "false");

        // customize fine grained ner
        props.setProperty("ner.fine.regexner.mapping", "example.rules");
        props.setProperty("ner.fine.regexner.ignorecase", "true");

        // add additional rules
        //props.setProperty("ner.additional.regexner.mapping", "example.rules");
        //props.setProperty("ner.additional.regexner.ignorecase", "true");

        // add 2 additional rules files ; set the first one to be case-insensitive
        //props.setProperty("ner.additional.regexner.mapping", "ignorecase=true,example_one.rules;example_two.rules");

        // set up pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // make an example document
        CoreDocument doc = new CoreDocument("Joe Smith is from Seattle.");
        // annotate the document
        pipeline.annotate(doc);
        // view results
        System.out.println("---");
        System.out.println("entities found");
        for (CoreEntityMention em : doc.entityMentions())
            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
        System.out.println("---");
        System.out.println("tokens and ner tags");
        String tokensAndNERTags = doc.tokens().stream().map(token -> "(" + token.word() + "," + token.ner() + ")").collect(
                Collectors.joining(" "));
        System.out.println(tokensAndNERTags);
*/
/*
        InputStream mpis = this.getClass().getClassLoader().getResourceAsStream("edu/stanford/nlp/models/ner/english.all.3class.distsim.prop");
        Properties mp = new Properties();
            try {
            mp.load(mpis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CRFClassifier<CoreMap> namedEntityRecognizer =
                null;
        try {
            namedEntityRecognizer = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz", mp);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String text = "I am sagar and i  born in Springfield and grew up in Boston. TCS is my first company";
        List<List<CoreMap>> classify = namedEntityRecognizer.classify(text);


        System.out.println("SSSSSSSSSSSSSS: " + classify);*/

        return response;
    }

    void parseSentiments(String text) {
        Properties props = new Properties();
        props.setProperty("parts-of-speech", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);        // create a document object
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
        File file = new File("temp.txt");

        try (OutputStream outputStream = new FileOutputStream(file)) {
            StreamUtils.copy(resource.getInputStream(), outputStream);


            inputStreamFactory = new MarkableFileInputStreamFactory(file);
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

    @Override
    public Response identifyOrg(Feedback feedback) {
        Response response = null;
        TokenizerModel tokenModel = null;
        try {
            tokenModel = new TokenizerModel(token_model.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Instantiating the TokenizerME class
        TokenizerME tokenizer = new TokenizerME(tokenModel);

        //Tokenizing the sentence in to a string array
        String tokens[] = tokenizer.tokenize(feedback.getData());

        //Loading the NER-person model
        TokenNameFinderModel tModel = null;
        try {
            tModel = new TokenNameFinderModel(org_model.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Instantiating the NameFinderME class
        NameFinderME nameFinder = new NameFinderME(tModel);

        //Finding the names in the sentence
        Span nameSpans[] = nameFinder.find(tokens);

        //Printing the names and their spans in a sentence
        System.out.println("printing org namesss.....");
        Map<String, String> map = new HashMap<>();
        boolean flag = false;
        for (Span s : nameSpans) {
            flag = true;
            log.info("............ ::: {}", s.toString());
            response = classifyFeedback(feedback);
            map.put(tokens[s.getStart()], s.toString());
            response.setOrganization(tokens[s.getStart()]);


        }
        if (!flag) {
            response = new Response();
            response.setStatus("Organization not found...");
        }

        return response;
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

    public void trainNameFinderModel() {
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<String> lineStream =
                null;
        try {
            lineStream = new PlainTextByLineStream((InputStreamFactory) org_model.getInputStream(), charset);
        } catch (FileNotFoundException e) {
            log.error("error occurred file not found", e);
        } catch (IOException e) {
            log.error("error occurred ", e);
        }
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

        try {
            tokenNameFinderModel = NameFinderME.train("en", "organization", sampleStream, TrainingParameters.defaultParams(),
                    new TokenNameFinderFactory());
        } catch (IOException e) {
            log.error("error occurred ", e);
        } finally {
            try {
                sampleStream.close();
            } catch (IOException e) {
                log.error("error occurred while closing resources", e);
            }
        }

    }
}
