package com.roomex.xmltransform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;

import javax.xml.transform.Source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XmlTransformTest {
    XmlTransform xmlTransform = new XmlTransform();
    String working_folder="./demo/";
    String input ="input.xml";
    String outputCompare="output.xml";
    String outputGenerated = "output_generated.xml";
    @BeforeEach
    void init() {
        xmlTransform.xslt="partner_a_transform.xslt";
    }
    @Test
    void testInput() throws Exception {
        xmlTransform.transform(working_folder+input, working_folder+outputGenerated);
        compareXmlFiles(working_folder+outputGenerated, working_folder+outputCompare);
    }

    private void compareXmlFiles(String expectedFileName, String actualFileName) throws Exception {
        String expectedXML = Files.readString(Paths.get(expectedFileName), StandardCharsets.ISO_8859_1);
        String actualXML = Files.readString(Paths.get(actualFileName), StandardCharsets.ISO_8859_1);
        XmlDiff xmlDiff=new XmlDiff();
        xmlDiff.ignoreNode("soapenv:Envelope");
        xmlDiff.ignoreValueForNode("add:MessageID");
        xmlDiff.ignoreValueForNode("oas:Nonce");
        xmlDiff.ignoreValueForNode("oas:Password");
        xmlDiff.ignoreValueForNode("oas1:Created");
        List<String> diffList=new ArrayList<>();
        xmlDiff.diff(expectedXML, actualXML, diffList);
        assertEquals(0, diffList.size(), "Errors "+ diffList.stream().collect(Collectors.joining("\n")));

    }
}
