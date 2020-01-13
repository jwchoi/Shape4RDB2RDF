package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

public class ShaclDocModel {
    private URI baseIRI;
    private String prefix;

    private Set<Directive> directives;

    ShaclDocModel(URI baseIRI, String prefix) {
        this.baseIRI = baseIRI;
        this.prefix = prefix;

        directives = new TreeSet<>();
        directives.add(new BaseDecl(baseIRI));
        directives.add(new PrefixDecl(URI.create(baseIRI + Symbols.HASH), prefix));
    }

    void addPrefixDecl(String prefix, String IRIString) {
        directives.add(new PrefixDecl(URI.create(IRIString), prefix));
    }

    public Set<Directive> getDirectives() { return directives; }

    public URI getBaseIRI() {
        return baseIRI;
    }
    public String getPrefix() { return prefix; }

    public static class Keywords {
        public static final String KW_BASE = Symbols.BASE;
        public static final String KW_IMPORTS = Symbols.IMPORTS;
        public static final String KW_PREFIX = Symbols.PREFIX;

        public static final String KW_SHAPE_CLASS = "shapeClass";
        public static final String KW_SHAPE = "shape";

        public static final String KW_TRUE = "true";
        public static final String KW_FALSE = "false";
    }
}
