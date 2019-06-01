package com.conaug.conaugApp.service;

import com.conaug.conaugApp.model.Feedback;
import com.conaug.conaugApp.model.Response;

public interface ParseService {
    Response parseData(Feedback data);

    void trainModel();
    Response identifyOrg(Feedback feedback);

    Response classifyFeedback(Feedback feedback);
}
