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

import com.conaug.conaugApp.analyzers.SentenceProvider;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides user sentences via a simple text file
 */
public class FileSentenceProvider implements SentenceProvider {

    BaseModelBuilderParams params;
    Set<String> sentences = new HashSet<String>();

    public Set<String> getSentences() {
        if (sentences.isEmpty()) {
            try {
                InputStream fis;
                BufferedReader br;
                String line;

                fis = new FileInputStream(params.getSentenceFile());
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                int i = 0;
                while ((line = br.readLine()) != null) {

                    sentences.add(line);
                }

                // Done with the file
                br.close();
                br = null;
                fis = null;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileKnownEntityProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FileKnownEntityProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sentences;
    }

    public void setParameters(BaseModelBuilderParams params) {
        this.params = params;
    }
}
