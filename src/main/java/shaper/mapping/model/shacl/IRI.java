package shaper.mapping.model.shacl;

import java.net.URI;

public class IRI extends Node {
    private URI value;

    IRI(URI value) {
        this.value = value;
        nodeKind = NodeKinds.IRI;
    }

    static IRI create(URI uri) { return new IRI(uri); }
    static IRI create(String uri) { return create(URI.create(uri)); }

    @Override
    public URI getValue() {
        return value;
    }
}
