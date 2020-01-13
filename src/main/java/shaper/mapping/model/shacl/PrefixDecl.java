package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;

import java.net.URI;

public class PrefixDecl extends Directive {
    private URI iri;
    private String prefix;

    public PrefixDecl(URI iri, String prefix) {
        this.iri = iri;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return ShaclDocModel.Keywords.KW_PREFIX + Symbols.SPACE + prefix + Symbols.COLON + Symbols.SPACE + Symbols.LT + iri + Symbols.GT;
    }

    @Override
    public int compareTo(Directive o) {
        if (o instanceof BaseDecl || o instanceof ImportsDecl) return 1;

        return this.toString().compareTo(o.toString());
    }
}
