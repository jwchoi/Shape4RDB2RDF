package shaper.mapping.model.shacl;

public abstract class Node {
    public enum NodeKinds {
        IRI, BLANKNODE, LITERAL
    }

    NodeKinds nodeKind;

    public NodeKinds getNodeKind() { return nodeKind; }

    public abstract Object getValue();
}
