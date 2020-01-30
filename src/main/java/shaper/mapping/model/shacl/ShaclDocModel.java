package shaper.mapping.model.shacl;

import shaper.mapping.PrefixMap;
import shaper.mapping.Symbols;

import java.net.URI;
import java.util.*;

public class ShaclDocModel {
    private URI baseIRI;
    private String prefix;

    private Map<URI, String> prefixMap;

    private Set<Shape> shapes;

    ShaclDocModel(URI baseIRI, String prefix) {
        this.baseIRI = baseIRI;
        this.prefix = prefix;

        prefixMap = new TreeMap<>();
        prefixMap.put(URI.create(baseIRI + Symbols.HASH), prefix); // prefix newly created by base
        prefixMap.put(PrefixMap.getURI("sh"), "sh"); // prefix for sh:

        shapes = new TreeSet<>();
    }

    public String getRelativeIRIOr(final URI uri) {
        return getRelativeIRIOr(uri.toString());
    }

    public String getRelativeIRIOr(final String absoluteIRIString) {
        String relativeIRI = null;

        Set<Map.Entry<URI, String>> entrySet = prefixMap.entrySet();

        for (Map.Entry<URI, String> entry: entrySet) {
            String uri = entry.getKey().toString();
            if (absoluteIRIString.startsWith(uri)) {
                relativeIRI = absoluteIRIString.replace(uri, entry.getValue() + Symbols.COLON);

                if (isRelativeIRI(relativeIRI)) return relativeIRI;
            }
        }

        if (absoluteIRIString.startsWith(baseIRI.toString()))
            return Symbols.LT + absoluteIRIString.substring(absoluteIRIString.length()) + Symbols.GT;

        return Symbols.LT + absoluteIRIString + Symbols.GT;
    }

    private boolean isRelativeIRI(String relativeIRI) {
        if (relativeIRI.contains(Symbols.SLASH) || relativeIRI.contains(Symbols.HASH))
            return false;

        return true;
    }

    public String getSerializedPropertyShape(IRI propertyShapeID) {
        for (Shape shape : shapes)
            if (shape instanceof PropertyShape) {
                PropertyShape propertyShape = (PropertyShape) shape;
                if (propertyShape.getID().equals(propertyShapeID))
                    return propertyShape.toString();
            }

        return null;
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
        Set<Map.Entry<URI, String>> entrySet = prefixMap.entrySet();

        for (Map.Entry<URI, String> entry: entrySet) {
            String key = entry.getKey().toString();
            if (uri.toString().startsWith(key))
                return entry.getValue();
        }

        return null;
    }

    void addShape(Shape shape) { shapes.add(shape); }

    void addPrefixDecl(String prefix, String IRIString) {
        prefixMap.put(URI.create(IRIString), prefix);
    }

    public Map<URI, String> getPrefixMap() { return prefixMap; }

    public URI getBaseIRI() {
        return baseIRI;
    }
    public String getPrefix() { return prefix; }
}
