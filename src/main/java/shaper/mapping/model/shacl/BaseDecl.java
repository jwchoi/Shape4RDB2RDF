package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;

import java.net.URI;

public class BaseDecl extends Directive {
    private URI iri;

    BaseDecl(URI iri) {
        this.iri = iri;
    }

    @Override
    public String toString() {
        return ShaclDocModel.Keywords.KW_BASE + Symbols.SPACE + Symbols.LT + iri + Symbols.GT;
    }

    @Override
    public int compareTo(Directive o) {
        if (o instanceof ImportsDecl || o instanceof PrefixDecl) return -1;

        return this.toString().compareTo(o.toString());
    }
}
