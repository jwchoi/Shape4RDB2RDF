package shaper.mapping;

import shaper.Shaper;
import shaper.mapping.metadata.r2rml.R2RMLModel;
import shaper.mapping.metadata.rdf.RDFMappingMetadata;
import shaper.mapping.metadata.shex.ShExSchema;

import java.io.File;
import java.io.PrintWriter;

public abstract class RDFMapper {

    public static final String A = "a";
    public static final String AMPERSAND = "&";
    public static final String ASTERISK = "*";
    public static final String AT = "@";
    public static final String BACKSLASH = "\\";
    public static final String BASE = "BASE";
    public static final String BNODE = "BNODE";
    public static final String CARET = "^";
    public static final String CLOSE_BRACE = "}";
    public static final String CLOSE_BRACKET = "]";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String COLON = ":";
    public static final String DOLLAR = "$";
    public static final String DASH = "-";
    public static final String DOT = ".";
    public static final String EQUAL = "=";
    public static final String GT = ">";
    public static final String HASH = "#";
    public static final String IRI = "IRI";
    public static final String OPEN_BRACE = "{";
    public static final String OPEN_BRACKET = "[";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String OR = "|";
    public static final String LT = "<";
    public static final String NEWLINE = "\n";
    public static final String PLUS = "+";
    public static final String PREFIX = "PREFIX";
    public static final String QUESTION_MARK = "?";
    public static final String REF = "ref";
    public static final String SEMICOLON = ";";
    public static final String SLASH = "/";
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String UNDERSCORE = "_";
    public static final String ZERO = "0";
    public static final String base = "base";
    public static final String prefix = "prefix";

    public enum MapperTypes {
        DIRECT_MAPPING("dm"), R2RML("r2rml");

        private String string;
        private MapperTypes(String string) { this.string = string; }

        @Override
        public String toString() {
            return string;
        }
    }

    enum Extension {
        ShEx("shex"), Turtle("ttl");

        private final String extension;

        Extension(String extension) {
            this.extension = extension;
        }

        @Override
        public String toString() { return extension; }
    }

    public RDFMappingMetadata rdfMappingMD;
    public ShExSchema shExSchema;
    public R2RMLModel r2rmlModel;

    File output;
    PrintWriter writer;

    public abstract File generateRDFFile();
    public abstract File generateShExFile();

    void preProcess(Extension extension) {
        String catalog = Shaper.localDBSchema.getCatalog();

        switch (extension) {
            case Turtle:
                output = new File(Shaper.DEFAULT_DIR_FOR_RDF_FILE + catalog + "." + extension);
                break;
            case ShEx:
                output = new File(Shaper.DEFAULT_DIR_FOR_SHEX_FILE + catalog + "." + extension);
                break;
        }
        try {
            writer = new PrintWriter(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
