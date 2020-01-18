package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.Template;

import java.util.List;
import java.util.Optional;

public abstract class Shape implements Comparable<Shape> {
    private IRI id;
    private ShaclDocModel shaclDocModel;

    private String serializedShape;

    private List<IRI> targetClass; // rdfs:Class
    private List<Node> targetNode; // any IRI or literal
    private IRI targetObjectsOf; // rdf:Property
    private IRI targetSubjectsOf; // rdf:Property

    Shape(IRI id, ShaclDocModel shaclDocModel) {
        this.id = id;
        this.shaclDocModel = shaclDocModel;
    }

    protected IRI getID() { return id; }
    protected ShaclDocModel getShaclDocModel() { return shaclDocModel; }

    protected String getSerializedShape() { return serializedShape; }
    protected void setSerializedShape(String serializedShape) { this.serializedShape = serializedShape; }

    @Override
    public int compareTo(Shape o) {
        return id.getValue().compareTo(o.id.getValue());
    }

    protected boolean isPossibleToHavePattern(Optional<Template> template) {
        if (template.isPresent()) {
            if (template.get().getLengthExceptColumnName() > 0)
                return true;
        }

        return false;
    }

    // \n\t
    protected String getNT() { return Symbols.NEWLINE + Symbols.TAB; }
    // ;\n\t
    protected String getSNT() { return Symbols.SPACE + Symbols.SEMICOLON + Symbols.NEWLINE + Symbols.TAB; }
    // .\n\t
    protected String getDNT() { return Symbols.SPACE + Symbols.DOT + Symbols.NEWLINE + Symbols.TAB; }

    protected String getPO(String p, String o) { return p + Symbols.SPACE + o; }
}
