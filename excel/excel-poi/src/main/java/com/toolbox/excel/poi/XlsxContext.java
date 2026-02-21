package com.toolbox.excel.poi;

import org.apache.poi.openxml4j.opc.OPCPackage;

import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;

public class XlsxContext {
    private OPCPackage opcPackage;
    private InputStream sheetStream;
    private XMLStreamReader reader;
    private StaxRowParser parser;

    public XlsxContext() {
    }

    public XlsxContext(OPCPackage opcPackage, InputStream sheetStream, XMLStreamReader reader, StaxRowParser parser) {
        this.opcPackage = opcPackage;
        this.sheetStream = sheetStream;
        this.reader = reader;
        this.parser = parser;
    }

    public OPCPackage getOpcPackage() {
        return opcPackage;
    }

    public OPCPackage opcPackage() {
        return opcPackage;
    }

    public XlsxContext setOpcPackage(OPCPackage opcPackage) {
        this.opcPackage = opcPackage;
        return this;
    }

    public XlsxContext opcPackage(OPCPackage opcPackage) {
        this.opcPackage = opcPackage;
        return this;
    }

    public InputStream getSheetStream() {
        return sheetStream;
    }

    public InputStream sheetStream() {
        return sheetStream;
    }

    public XlsxContext setSheetStream(InputStream sheetStream) {
        this.sheetStream = sheetStream;
        return this;
    }

    public XlsxContext sheetStream(InputStream sheetStream) {
        this.sheetStream = sheetStream;
        return this;
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    public XMLStreamReader reader() {
        return reader;
    }


    public XlsxContext setReader(XMLStreamReader reader) {
        this.reader = reader;
        return this;
    }

    public XlsxContext reader(XMLStreamReader reader) {
        this.reader = reader;
        return this;
    }

    public StaxRowParser getParser() {
        return parser;
    }

    public StaxRowParser parser() {
        return parser;
    }

    public XlsxContext setParser(StaxRowParser parser) {
        this.parser = parser;
        return this;
    }

    public XlsxContext parser(StaxRowParser parser) {
        this.parser = parser;
        return this;
    }
}
