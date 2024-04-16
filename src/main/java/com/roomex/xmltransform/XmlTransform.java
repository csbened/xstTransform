package com.roomex.xmltransform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
public class XmlTransform {
    @Value("${spring.transform.xslt}")
    String xslt;
    public void transform(String inputFileName, String outputFileName) {
        Source xmlSource = new StreamSource(inputFileName);
        InputStream xlstInput = null;
        try {
            xlstInput = (new ClassPathResource(xslt)).getInputStream();
        } catch (IOException e) {
            throw new TransformException("Unable to read resource "+xslt,e);
        }
        Source xsltSource = new StreamSource(xlstInput);
        StreamResult result = new StreamResult(outputFileName);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException e) {
            throw new TransformException("Unable to create transformer ",e);
        } catch (TransformerException e) {
            throw new TransformException("unable to transform", e);
        }
        log.info("XSLT transformation completed successfully.");
    }


}
