package com.conaug.conaugApp.util;

import com.conaug.conaugApp.analyzers.DefaultModelBuilderUtil;

import java.io.File;

public class ModelBuilderUtil {
    public void buildModel() {
        File fileOfSentences = new File("path to your sentence file");
        File fileOfNames = new File("path to your file of person names");
        File blackListFile = new File("path to your blacklist file");
        File modelOutFile = new File("path to you where the model will be saved");
        File annotatedSentencesOutFile = new File("path to your sentence file");

        DefaultModelBuilderUtil.generateModel(fileOfSentences, fileOfNames, blackListFile, modelOutFile, annotatedSentencesOutFile, "person", 3);

    }
}
