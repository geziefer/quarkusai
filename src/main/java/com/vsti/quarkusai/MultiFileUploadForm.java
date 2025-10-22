package com.vsti.quarkusai;

import jakarta.ws.rs.FormParam;

import java.io.InputStream;
import java.util.List;

public class MultiFileUploadForm {

    @FormParam("files")
    public List<InputStream> files;

    @FormParam("fileNames")
    public List<String> fileNames;

    @FormParam("contentTypes")
    public List<String> contentTypes;
}
