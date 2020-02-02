package shaper.mapping.model.shacl;

public abstract class Node {
    public enum NodeKinds {
        BlankNode, IRI, Literal, BlankNodeOrIRI, BlankNodeOrLiteral, IRIOrLiteral
    }

    NodeKinds nodeKind;

    public NodeKinds getNodeKind() { return nodeKind; }

    public abstract Object getValue();
}
