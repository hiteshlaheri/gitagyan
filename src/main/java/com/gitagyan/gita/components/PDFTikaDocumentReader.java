package com.gitagyan.gita.components;

import org.apache.pdfbox.pdmodel.font.encoding.StandardEncoding;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Objects;

@Component
public class PDFTikaDocumentReader {

        private final Resource resource;

    PDFTikaDocumentReader(@Value("classpath:/gitaenglish.pdf")
                             Resource resource) {
            this.resource = resource;
        }

       public List<Document> loadText()  {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
            TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
            var documentlsit = tikaDocumentReader.get();
            return splitter.apply(documentlsit);
        }


}
