package com.conaug.conaugApp.service;

import com.conaug.conaugApp.model.Feedback;
import com.conaug.conaugApp.model.Response;

public interface ParseService {
    //Response parseData(Feedback data);

    void trainModel();

    Response classifyFeedback(Feedback feedback);
}
