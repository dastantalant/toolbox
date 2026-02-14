package com.toolbox.excel.fast.parser;

import com.toolbox.excel.fast.opc.OpcPackage;

import java.util.ArrayList;
import java.util.List;


import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public interface SharedStringsTable {
    String get(int index);

    // --- Реализация ---
    class InMemory implements SharedStringsTable {
        private final List<String> strings = new ArrayList<>();

        public InMemory(OpcPackage opc) {
            // Файла может не быть (если в Excel только числа), это нормально
            try (InputStream stream = opc.getEntryStream("xl/sharedStrings.xml")) {
                if (stream == null) return;

                XMLStreamReader reader = opc.createReader(stream);
                StringBuilder buffer = new StringBuilder();
                boolean insideT = false;

                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if ("t".equals(reader.getLocalName())) {
                            insideT = true;
                            buffer.setLength(0);
                        }
                    } else if (event == XMLStreamConstants.CHARACTERS) {
                        if (insideT) buffer.append(reader.getText());
                    } else if (event == XMLStreamConstants.END_ELEMENT) {
                        String name = reader.getLocalName();
                        if ("t".equals(name)) {
                            insideT = false;
                        } else if ("si".equals(name)) {
                            // Конец элемента (Shared Item) -> сохраняем строку
                            strings.add(buffer.toString());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load Shared Strings", e);
            }
        }

        @Override
        public String get(int index) {
            if (index < 0 || index >= strings.size()) return null;
            return strings.get(index);
        }
    }
}