package com.toolbox.excel.poi;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class XlsxSourceFactory {

    public XlsxIterator createIterator(File file) {
        return createIterator(file, null);
    }

    public XlsxIterator createIterator(File file, String sheetName) {
        OPCPackage pkg = null;
        InputStream sheetStream = null;
        XMLStreamReader reader = null;

        try {
            pkg = OPCPackage.open(file);
            XSSFReader xssfReader = new XSSFReader(pkg);

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            StylesTable styles = xssfReader.getStylesTable();

            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            sheetStream = findSheet(iter, sheetName);

            if (sheetStream == null) {
                pkg.revert();
                return XlsxIterator.empty();
            }

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);

            reader = factory.createXMLStreamReader(sheetStream);

            CellValueResolver resolver = new CellValueResolver(strings, styles);
            HashMap<String, Integer> headerMap = new HashMap<>();

            StaxRowParser parser = new StaxRowParser(reader, resolver, headerMap);

            XlsxContext context = new XlsxContext(pkg, sheetStream, reader, parser);
            return new XlsxIterator(context);

        } catch (Exception e) {
            try {
                if (reader != null) reader.close();
                if (sheetStream != null) sheetStream.close();
                if (pkg != null) pkg.revert();
            } catch (Exception ignored) {
            }
            throw new ExcelPoiException("Failed to open Excel file: " + file.getAbsolutePath(), e);
        }
    }

    private InputStream findSheet(XSSFReader.SheetIterator iter, String targetName) {
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            if (targetName == null || targetName.isEmpty()) {
                return stream;
            }
            if (targetName.equals(iter.getSheetName())) {
                return stream;
            }
            try {
                stream.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}