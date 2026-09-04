package com.montagegold.stock.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfTextExtractor {

    /**
     * Extracts raw text from a PDF file. Works out-of-the-box for digital
     * (text-based) PDFs. Scanned/image-only PDFs would additionally require an
     * OCR engine (e.g. Tesseract) which is intentionally NOT included so that
     * this first test version stays lightweight and easy to revert.
     */
    public String extract(InputStream inputStream) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        Parser parser = new PDFParser();
        parser.parse(inputStream, handler, metadata, new ParseContext());
        return handler.toString();
    }
}
