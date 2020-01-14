package shaper.mapping.model.shacl;

import java.net.URI;

public abstract class Directive implements Comparable<Directive> {
    private URI iri;

    Directive(URI iri) {
        this.iri = iri;
    }

    URI getIRI() { return iri; }

    @Override
    public int compareTo(Directive o) {
        return iri.compareTo(o.iri);
    }
}
