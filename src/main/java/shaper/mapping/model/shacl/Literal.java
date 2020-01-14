package shaper.mapping.model.shacl;

import java.net.URI;
import java.util.Optional;

public class Literal extends Node {
    private String value;
    private Optional<URI> datatype;
    private Optional<String> langtag;

    Literal(String value) {
        this.value = value;
        nodeKind = NodeKinds.LITERAL;
    }

    Literal(String value, URI datatype) {
        this(value);
        this.datatype = Optional.of(datatype);
    }

    Literal(String value, String langtag) {
        this(value);
        this.langtag = Optional.of(langtag);
    }

    @Override
    public String getValue() {
        return value;
    }
}
