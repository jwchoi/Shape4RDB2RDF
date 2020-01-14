package shaper.mapping.model.shacl;

import shaper.mapping.PrefixMap;
import shaper.mapping.Symbols;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class ShaclDocModel {
    private URI baseIRI;
    private String prefix;

    private Set<Directive> directives;

    private Set<Shape> shapes;

    ShaclDocModel(URI baseIRI, String prefix) {
        this.baseIRI = baseIRI;
        this.prefix = prefix;

        directives = new TreeSet<>();
        directives.add(new BaseDecl(baseIRI));
        directives.add(new PrefixDecl(URI.create(baseIRI + Symbols.HASH), prefix));
        directives.add(new PrefixDecl(PrefixMap.getURI("sh"), "sh"));

        shapes = new TreeSet<>();
    }

    public NodeShape getMappedNodeShape(URI triplesMap) {
        for (Shape shape: shapes) {
            if (shape instanceof NodeShape) {
                NodeShape nodeShape = (NodeShape) shape;
                Optional<URI> mappedTriplesMap = nodeShape.getMappedTriplesMap();
                if (mappedTriplesMap.isPresent()) {
                    if (mappedTriplesMap.get().equals(triplesMap))
                        return nodeShape;
                }
            }
        }

        return null;
    }

    String getPrefixOf(URI uri) {
        for (Directive directive: directives) {
            if (directive instanceof PrefixDecl) {
                if (uri.toString().startsWith(directive.getIRI().toString()))
                    return ((PrefixDecl) directive).getPrefix();
            }
        }

        return null;
    }

    void addShape(Shape shape) { shapes.add(shape); }

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
