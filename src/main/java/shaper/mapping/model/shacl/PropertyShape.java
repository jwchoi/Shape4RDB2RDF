package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.ObjectMap;
import shaper.mapping.model.r2rml.PredicateMap;
import shaper.mapping.model.r2rml.RefObjectMap;

public class PropertyShape extends Shape {
    private PredicateMap mappedPredicateMap;
    private ObjectMap mappedObjectMap;
    private RefObjectMap mappedRefObjectMap;

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, ObjectMap mappedObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedObjectMap = mappedObjectMap;
    }

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, RefObjectMap mappedRefObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedRefObjectMap = mappedRefObjectMap;
    }

    @Override
    public String toString() {
        String serializedPropertyShape = getSerializedShape();
        if (serializedPropertyShape != null) return serializedPropertyShape;

        StringBuffer buffer = new StringBuffer();

        String id = getShaclDocModel().getRelativeIRI(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        buffer.append(getPO(Symbols.A, "sh:PropertyShape"));
        buffer.append(getSNT());

        String p = getShaclDocModel().getRelativeIRI(mappedPredicateMap.getConstant().get());
        buffer.append(getPO("sh:path", p));
        buffer.append(getSNT());

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        serializedPropertyShape = buffer.toString();
        setSerializedShape(serializedPropertyShape);
        return serializedPropertyShape;
    }
}
