package shaper.mapping.model.shacl;

import java.net.URI;

public class IRI extends Node implements Comparable<IRI> {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IRI)
            return value.equals(((IRI) obj).value);

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return value.toString();
    }


    @Override
    public int compareTo(IRI iri) {
        return value.compareTo(iri.value);
    }
}
