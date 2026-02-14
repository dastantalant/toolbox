package com.dastan.toolbox.excel.poi;

import org.apache.poi.openxml4j.opc.OPCPackage;

import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;

public record XlsxContext(
        OPCPackage opcPackage,
        InputStream sheetStream,
        XMLStreamReader reader,
        StaxRowParser parser
) {
}