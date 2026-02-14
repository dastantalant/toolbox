package com.dastan.toolbox.excel.fast.opc;

import com.fasterxml.aalto.stax.InputFactoryImpl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OpcPackage implements Closeable {
    private final ZipFile zip;
    private final InputFactoryImpl xmlFactory;

    public OpcPackage(File file) throws IOException {
        this.zip = new ZipFile(file);
        this.xmlFactory = new InputFactoryImpl();
        this.xmlFactory.configureForSpeed();
    }

    public InputStream getEntryStream(String path) {
        // Excel пути иногда начинаются с /, иногда нет. Нормализуем.
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        ZipEntry entry = zip.getEntry(cleanPath);
        if (entry == null) return null;
        try {
            return zip.getInputStream(entry);
        } catch (IOException e) {
            throw new RuntimeException("Could not read zip entry: " + cleanPath, e);
        }
    }

    public XMLStreamReader createReader(InputStream stream) throws Exception {
        return xmlFactory.createXMLStreamReader(stream);
    }

    // Парсим workbook.xml и .rels, чтобы найти реальные файлы листов
    public List<SheetDescriptor> getSheets() {
        Map<String, String> idToPath = parseRelationships();
        List<SheetDescriptor> sheets = new ArrayList<>();

        try (InputStream is = getEntryStream("xl/workbook.xml")) {
            if (is == null) return Collections.emptyList();
            XMLStreamReader reader = createReader(is);

            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && "sheet".equals(reader.getLocalName())) {
                    String name = reader.getAttributeValue(null, "name");
                    String sheetId = reader.getAttributeValue(null, "sheetId");
                    String rId = reader.getAttributeValue("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");

                    String path = "xl/" + idToPath.get(rId);
                    sheets.add(new SheetDescriptor(name, Integer.parseInt(sheetId), path));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse workbook.xml", e);
        }
        return sheets;
    }

    private Map<String, String> parseRelationships() {
        Map<String, String> map = new HashMap<>();
        try (InputStream is = getEntryStream("xl/_rels/workbook.xml.rels")) {
            if (is == null) return Collections.emptyMap();
            XMLStreamReader reader = createReader(is);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && "Relationship".equals(reader.getLocalName())) {
                    String id = reader.getAttributeValue(null, "Id");
                    String target = reader.getAttributeValue(null, "Target");
                    map.put(id, target);
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}