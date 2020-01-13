package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;

import java.net.URI;

public class ImportsDecl extends Directive {
    private URI iri;

    public ImportsDecl(URI iri) {
        this.iri = iri;
    }

    @Override
    public String toString() {
        return ShaclDocModel.Keywords.KW_IMPORTS + Symbols.SPACE + Symbols.LT + iri + Symbols.GT;
    }

    @Override
    public int compareTo(Directive o) {
        if (o instanceof BaseDecl) return 1;
        if (o instanceof PrefixDecl) return -1;

        return this.toString().compareTo(o.toString());
    }
}
