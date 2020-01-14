package shaper.mapping.model.shacl;

import java.net.URI;

public class PrefixDecl extends Directive {
    private String prefix;

    PrefixDecl(URI iri, String prefix) {
        super(iri);
        this.prefix = prefix;
    }

    String getPrefix() { return prefix; }
}
