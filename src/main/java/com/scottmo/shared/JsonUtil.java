package com.scottmo.shared;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtil() {}

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static void save(File file, Object object) throws IOException {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        DefaultIndenter indenter = new DefaultIndenter("    ", System.lineSeparator());
        printer.indentArraysWith(indenter);
        printer.indentObjectsWith(indenter);
        
        MAPPER.writer(printer).writeValue(file, object);
    }
    
    public static String saveToString(Object object) throws IOException {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        DefaultIndenter indenter = new DefaultIndenter("    ", System.lineSeparator());
        printer.indentArraysWith(indenter);
        printer.indentObjectsWith(indenter);
        
        return MAPPER.writer(printer).writeValueAsString(object);
    }
}
