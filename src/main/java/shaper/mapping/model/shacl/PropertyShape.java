package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.ObjectMap;
import shaper.mapping.model.r2rml.PredicateMap;
import shaper.mapping.model.r2rml.RefObjectMap;

public class PropertyShape extends Shape {
    private enum MappingTypes { OBJECT_MAP, REF_OBJECT_MAP }

    private PredicateMap mappedPredicateMap;
    private ObjectMap mappedObjectMap;
    private RefObjectMap mappedRefObjectMap;

    private MappingTypes mappingType;

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, ObjectMap mappedObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedObjectMap = mappedObjectMap;

        mappingType = MappingTypes.OBJECT_MAP;
    }

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, RefObjectMap mappedRefObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedRefObjectMap = mappedRefObjectMap;

        mappingType = MappingTypes.REF_OBJECT_MAP;
    }

    @Override
    public String toString() {
        String serializedPropertyShape = getSerializedShape();
        if (serializedPropertyShape == null) {
            serializedPropertyShape = buildSerializedPropertyShape();
            setSerializedShape(serializedPropertyShape);
        }

        return serializedPropertyShape;
    }

    private String buildSerializedPropertyShape() {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        String id = getShaclDocModel().getRelativeIRI(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        buffer.append(getPO(Symbols.A, "sh:PropertyShape"));
        buffer.append(getSNT());

        o = getShaclDocModel().getRelativeIRI(mappedPredicateMap.getConstant().get());
        buffer.append(getPO("sh:path", o));
        buffer.append(getSNT());

        if (mappingType.equals(MappingTypes.REF_OBJECT_MAP)) {
            o = getShaclDocModel().getRelativeIRI(mappedRefObjectMap.getParentTriplesMap().toString());
            buffer.append(getPO("sh:node", o));
            buffer.append(getSNT());
        }

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        return buffer.toString();
    }
}
