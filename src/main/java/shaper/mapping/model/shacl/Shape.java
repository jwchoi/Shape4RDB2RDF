package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.Template;

import java.util.List;
import java.util.Optional;

public abstract class Shape implements Comparable<Shape> {
    private IRI id;
    private ShaclDocModel shaclDocModel;

    private String serializedShape;

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

    // Unlabeled Blank Node
    protected String getUBN(String p, String o) { return Symbols.OPEN_BRACKET + Symbols.SPACE + p + Symbols.SPACE + o + Symbols.SPACE + Symbols.CLOSE_BRACKET; }

    // Multiple Lines Unlabeled Blank Node
    protected String getMultipleLineUBN(String p, String o, int indentSize) {
        StringBuffer buffer = new StringBuffer();

        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < indentSize; i++)
            indent.append(Symbols.SPACE);

        buffer.append(indent + Symbols.OPEN_BRACKET + Symbols.NEWLINE);
        buffer.append(indent.toString() + indent + p + Symbols.SPACE + o + Symbols.NEWLINE);
        buffer.append(indent + Symbols.CLOSE_BRACKET + Symbols.NEWLINE);

        return buffer.toString();
    }
}
