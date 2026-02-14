package com.dastan.toolbox.excel.poi;

import com.dastan.toolbox.excel.api.ArrayRowData;
import com.dastan.toolbox.excel.api.RowData;
import org.apache.poi.ss.util.CellReference;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaxRowParser {

    private final XMLStreamReader reader;
    private final CellValueResolver resolver;
    private final Map<String, Integer> headerMap;

    private final StringBuilder textBuffer;
    private final List<String> cellValues;

    private int currentRowIndex;
    private int currentAutoColIndex;

    private String cellRef;
    private String cellType;
    private String cellStyle;
    private boolean insideValue;

    public StaxRowParser(XMLStreamReader reader, CellValueResolver resolver, Map<String, Integer> headerMap) {
        this.reader = reader;
        this.resolver = resolver;
        this.headerMap = headerMap;

        textBuffer = new StringBuilder();
        cellValues = new ArrayList<>();
        currentRowIndex = -1;
        currentAutoColIndex = 0;
    }

    public RowData parseNextRow() throws XMLStreamException {
        if (!reader.hasNext()) return null;
        resetRowState();

        while (reader.hasNext()) {
            int event = reader.next();
            if (XMLStreamConstants.START_ELEMENT == event) {
                handleStartElement();
            } else if (XMLStreamConstants.CHARACTERS == event && insideValue) {
                textBuffer.append(reader.getText());
            } else if (XMLStreamConstants.END_ELEMENT == event && handleEndElement()) {
                return buildRowData();
            }
        }
        return null;
    }

    private void resetRowState() {
        cellValues.clear();
        textBuffer.setLength(0);
        currentAutoColIndex = 0;
        insideValue = false;
    }

    private void handleStartElement() {
        String tagName = reader.getLocalName();

        switch (tagName) {
            case "row" -> {
                String value = reader.getAttributeValue(null, "value");
                if (value != null) {
                    currentRowIndex = Integer.parseInt(value) - 1;
                }
            }
            case "c" -> {
                cellRef = reader.getAttributeValue(null, "r");
                cellType = reader.getAttributeValue(null, "t");
                cellStyle = reader.getAttributeValue(null, "s");
                textBuffer.setLength(0);
            }
            case "v", "t" -> insideValue = true;
        }
    }

    private boolean handleEndElement() {
        switch (reader.getLocalName()) {
            case "v", "t" -> insideValue = false;
            case "c" -> processFinishedCell();
            case "row" -> {
                return true;
            }
        }
        return false;
    }

    private void processFinishedCell() {
        int index;
        if (cellRef != null) {
            index = getColumnIndex(cellRef);
            currentAutoColIndex = index + 1;
        } else {
            index = currentAutoColIndex++;
        }

        while (cellValues.size() <= index) {
            cellValues.add(null);
        }

        cellValues.set(index, resolver.resolve(textBuffer.toString(), cellType, cellStyle));
    }

    private RowData buildRowData() {
        String[] cellsArray = cellValues.toArray(new String[0]);
        updateHeaderMapIfNeeded(cellsArray);
        return new ArrayRowData(currentRowIndex, cellsArray, headerMap);
    }

    private void updateHeaderMapIfNeeded(String[] cellsArray) {
        if (currentRowIndex == 0 || (currentRowIndex == -1 && headerMap.isEmpty())) {
            for (int i = 0; i < cellsArray.length; i++) {
                if (cellsArray[i] != null) {
                    headerMap.putIfAbsent(cellsArray[i], i);
                }
            }
        }
    }

    private int getColumnIndex(String ref) {
        String colRef = ref.replaceAll("[0-9]", "");
        return CellReference.convertColStringToIndex(colRef);
    }
}