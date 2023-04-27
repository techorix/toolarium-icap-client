package de.techorix.icap;


import org.jboss.resteasy.annotations.jaxrs.FormParam;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MultipartBody {

    @FormParam("name")
    @XmlMimeType((MediaType.TEXT_PLAIN))
    String name;

    @FormParam("file")
    @XmlMimeType(MediaType.APPLICATION_OCTET_STREAM)
    InputStream file;
}
