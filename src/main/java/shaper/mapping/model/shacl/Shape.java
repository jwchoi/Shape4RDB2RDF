package shaper.mapping.model.shacl;

import java.util.List;

public abstract class Shape implements Comparable<Shape> {
    private IRI id;

    private List<IRI> targetClass; // rdfs:Class
    private List<Node> targetNode; // any IRI or literal
    private IRI targetObjectsOf; // rdf:Property
    private IRI targetSubjectsOf; // rdf:Property

    Shape(IRI id) {
        this.id = id;
    }

    IRI getID() { return id; }

    @Override
    public int compareTo(Shape o) {
        return id.getValue().compareTo(o.id.getValue());
    }
}
