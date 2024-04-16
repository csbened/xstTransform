package com.roomex.xmltransform;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class CommandLine implements CommandLineRunner {
    final XmlTransform xmlTransform;

    @Override
    public void run(String... args) throws Exception {
        checkArgs(args);
    }
    private void checkArgs(String ... args) {
        if (args.length!=2) {
            throw new TransformException("Needs two parameters");
        }
        File source = new File(args[0]);
        if (!source.exists() || !source.isFile()) {
            throw new TransformException("First parameter should be a file "+source.getAbsolutePath());
        }
        xmlTransform.transform(args[0], args[1]);
    }
}
