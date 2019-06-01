/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.conaug.conaugApp.analyzers.impl;

import com.conaug.conaugApp.analyzers.Modelable;
import opennlp.tools.namefind.*;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates annotations, writes annotations to file, and creates a model and writes to a file
 */
public class GenericModelableImpl implements Modelable {

    private Set<String> annotatedSentences = new HashSet<String>();
    BaseModelBuilderParams params;

    @Override
    public void setParameters(BaseModelBuilderParams params) {
        this.params = params;
    }

    @Override
    public String annotate(String sentence, String namedEntity, String entityType) {
        String annotation = sentence.replace(namedEntity, " <START:" + entityType + "> " + namedEntity + " <END> ");
        return annotation;
    }

    @Override
    public void writeAnnotatedSentences() {
        try {

            FileWriter writer = new FileWriter(params.getAnnotatedTrainingDataFile(), false);

            for (String s : annotatedSentences) {
                writer.write(s.replace("\n", " ").trim() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getAnnotatedSentences() {
        return annotatedSentences;
    }

    @Override
    public void setAnnotatedSentences(Set<String> annotatedSentences) {
        this.annotatedSentences = annotatedSentences;
    }

    @Override
    public void addAnnotatedSentence(String annotatedSentence) {
        annotatedSentences.add(annotatedSentence);
    }

    @Override
    public void buildModel(String entityType) {
        try {
            System.out.println("\tBuilding Model using " + annotatedSentences.size() + " annotations");
            System.out.println("\t\treading training data...");
            Charset charset = Charset.forName("UTF-8");
            ObjectStream<String> lineStream =
                    new PlainTextByLineStream(new MarkableFileInputStreamFactory(params.getAnnotatedTrainingDataFile()), charset);
            ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

            TokenNameFinderModel model;
            model = NameFinderME.train("en", entityType, sampleStream, TrainingParameters.defaultParams(), new TokenNameFinderFactory());
            sampleStream.close();
            OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(params.getModelFile()));
            model.serialize(modelOut);
            if (modelOut != null) {
                modelOut.close();
            }
            System.out.println("\tmodel generated");
        } catch (Exception e) {
        }
    }

    @Override
    public TokenNameFinderModel getModel() {


        TokenNameFinderModel nerModel = null;
        try {
            nerModel = new TokenNameFinderModel(new FileInputStream(params.getModelFile()));
        } catch (IOException ex) {
            Logger.getLogger(GenericModelableImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nerModel;
    }

    @Override
    public String[] tokenizeSentenceToWords(String sentence) {
        return sentence.split(" ");

    }
}
