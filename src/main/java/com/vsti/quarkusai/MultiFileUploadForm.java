package com.vsti.quarkusai;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;
import java.util.List;

public class MultiFileUploadForm {

    @FormParam("files")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public List<InputStream> files;

    @FormParam("fileNames")
    @PartType(MediaType.TEXT_PLAIN)
    public List<String> fileNames;

    @FormParam("contentTypes")
    @PartType(MediaType.TEXT_PLAIN)
    public List<String> contentTypes;
}
