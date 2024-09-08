package com.scottmo.core.google.api;

import java.io.IOException;
import java.util.List;

import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.Request;
import com.scottmo.core.Service;

public interface GoogleCloudService extends Service {

    String copyPresentation(String title, String folderId, String templatePresentationId);

    List<Page> getSlides(String presentationId) throws IOException;

    Presentation getPresentation(String presentationId) throws IOException;

    boolean updateSlides(String presentationId, List<Request> updateRequests) throws IOException;

    void setDefaultTitleText(String presentationId, SlideConfig slideConfig) throws IOException;

    void setBaseFont(String presentationId, SlideConfig slideConfig) throws IOException;

}
