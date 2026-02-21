package com.toolbox.excel.processor;

import com.google.auto.service.AutoService;
import com.toolbox.excel.api.CellParser;
import com.toolbox.excel.api.RowData;
import com.toolbox.excel.api.RowMapper;
import com.toolbox.excel.api.annotation.ExcelColumn;
import com.toolbox.excel.api.annotation.ExcelSheet;
import com.toolbox.excel.api.config.Constants;
import com.toolbox.excel.api.context.DeserializationContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.toolbox.excel.api.annotation.ExcelSheet")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExcelAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ExcelSheet.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                try {
                    generateMapper((TypeElement) element);
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Failed to generate mapper: " + e.getMessage(), element);
                }
            }
        }
        return true;
    }

    private void generateMapper(TypeElement typeElement) throws IOException {
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String simpleClassName = typeElement.getSimpleName().toString();
        String mapperName = simpleClassName + Constants.MAPPER_POSTFIX;
        String qualifiedClassName = typeElement.getQualifiedName().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import ").append(RowMapper.class.getName()).append(";\n");
        sb.append("import ").append(RowData.class.getName()).append(";\n");
        sb.append("import ").append(DeserializationContext.class.getName()).append(";\n");
        sb.append("import ").append(CellParser.class.getName()).append(";\n");
        sb.append("import ").append(qualifiedClassName).append(";\n\n");
        sb.append("public class ").append(mapperName)
                .append(" implements RowMapper<").append(simpleClassName).append("> {\n\n");
        List<FieldInfo> fields = collectFields(typeElement);
        for (FieldInfo field : fields)
            if (field.customParserType != null)
                sb.append("    private final ").append(field.customParserType).append(" parser_")
                        .append(field.name).append(" = new ").append(field.customParserType).append("();\n");
        sb.append("\n");
        sb.append("    @Override\n");
        sb.append("    public ").append(simpleClassName).append(" map(RowData row, DeserializationContext context) {\n");
        sb.append("        ").append(simpleClassName).append(" instance = new ").append(simpleClassName).append("();\n");
        for (FieldInfo field : fields)
            generateFieldMapping(sb, field);
        sb.append("        return instance;\n");
        sb.append("    }\n");
        sb.append("}\n");
        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + mapperName);
        try (Writer writer = file.openWriter()) {
            writer.write(sb.toString());
        }
    }

    private void generateFieldMapping(StringBuilder sb, FieldInfo field) {
        sb.append("        // Map ").append(field.name).append("\n");
        String getKey = field.index != -1 ? String.valueOf(field.index) : "\"" + field.columnName + "\"";
        sb.append("        String val_").append(field.name).append(" = row.get(").append(getKey).append(");\n");
        sb.append("        if (val_").append(field.name).append(" != null) {\n");
        if (!field.format.isEmpty())
            sb.append("            context.setFormat(\"").append(field.format).append("\");\n");
        if (field.customParserType != null)
            sb.append("            Object parsed = parser_").append(field.name)
                    .append(".parse(val_").append(field.name).append(", context);\n");
        else sb.append("            CellParser<?> p = context.findParser(").append(field.type).append(".class);\n")
                .append("            Object parsed = (p != null) ? p.parse(val_").append(field.name).append(", context) : null;\n");
        sb.append("            if (parsed != null) {\n");
        sb.append("                instance.").append(getSetterName(field.name)).append("((").append(field.type).append(") parsed);\n");
        sb.append("            }\n");
        if (!field.format.isEmpty())
            sb.append("            context.setFormat(null);\n");
        sb.append("        }\n");
    }

    private List<FieldInfo> collectFields(TypeElement typeElement) {
        List<FieldInfo> list = new ArrayList<>();
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                ExcelColumn ann = enclosed.getAnnotation(ExcelColumn.class);
                if (ann != null) {
                    VariableElement var = (VariableElement) enclosed;
                    String customParser = getCustomParserType(ann);
                    list.add(new FieldInfo(
                            var.getSimpleName().toString(),
                            var.asType().toString(),
                            ann.value(),
                            ann.index(),
                            ann.format(),
                            customParser
                    ));
                }
            }
        }
        return list;
    }

    private String getCustomParserType(ExcelColumn ann) {
        try {
            Class<?> clazz = ann.parser();
            if (clazz.getCanonicalName().equals(CellParser.None.class.getCanonicalName())) return null;
            return clazz.getCanonicalName();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            String typeName = typeMirror.toString();
            if (typeName.equals(CellParser.None.class.getCanonicalName())) return null;
            return typeName;
        }
    }

    private String getSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static final class FieldInfo {
        String name;
        String type;
        String columnName;
        int index;
        String format;
        String customParserType;

        public FieldInfo(String name, String type, String columnName, int index, String format, String customParserType) {
            this.name = name;
            this.type = type;
            this.columnName = columnName;
            this.index = index;
            this.format = format;
            this.customParserType = customParserType;
        }

        public FieldInfo() {
        }
    }
}