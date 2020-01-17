package shaper.mapping.model.shacl;

import janus.database.SQLSelectField;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.ObjectMap;
import shaper.mapping.model.r2rml.PredicateMap;
import shaper.mapping.model.r2rml.RefObjectMap;
import shaper.mapping.model.r2rml.Template;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Optional;

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

    private String buildSerializedPropertyShape(RefObjectMap mappedRefObjectMap) {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        // sh:node
        o = getShaclDocModel().getRelativeIRI(mappedRefObjectMap.getParentTriplesMap().toString());
        buffer.append(getPO("sh:node", o));
        buffer.append(getSNT());

        return buffer.toString();
    }

    private String buildSerializedPropertyShape(ObjectMap mappedObjectMap) {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        // cardinality
        o = "1";
        Optional<SQLSelectField> sqlSelectField = mappedObjectMap.getColumn();
        if (sqlSelectField.isPresent()) {
            switch (sqlSelectField.get().getNullable()) {
                case ResultSetMetaData.columnNoNulls:
                    // "exactly one"
                    buffer.append(getPO("sh:minCount", o));
                    buffer.append(getSNT());
                case ResultSetMetaData.columnNullable:
                case ResultSetMetaData.columnNullableUnknown:
                    // "zero or one"
                    buffer.append(getPO("sh:maxCount", o));
                    buffer.append(getSNT());
            }
        } else {
            Optional<Template> template = mappedObjectMap.getTemplate();
            if (template.isPresent()) {
                List<SQLSelectField> columnNames = template.get().getColumnNames();
                boolean isEveryColumnNoNulls = true;
                for (SQLSelectField columnName: columnNames) {
                    // "?" - zero or one
                    if (columnName.getNullable() != ResultSetMetaData.columnNoNulls) {
                        buffer.append(getPO("sh:maxCount", o));
                        buffer.append(getSNT());
                        isEveryColumnNoNulls = false;
                        break;
                    }
                }
                // "exactly one"
                if (isEveryColumnNoNulls) {
                    buffer.append(getPO("sh:minCount", o));
                    buffer.append(getSNT());
                    buffer.append(getPO("sh:maxCount", o));
                    buffer.append(getSNT());
                }
            }
        }

        return buffer.toString();
    }

    private String buildSerializedPropertyShape() {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        // rdf:type sh:PropertyShape
        String id = getShaclDocModel().getRelativeIRI(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        buffer.append(getPO(Symbols.A, "sh:PropertyShape"));
        buffer.append(getSNT());

        // sh:path
        o = getShaclDocModel().getRelativeIRI(mappedPredicateMap.getConstant().get());
        buffer.append(getPO("sh:path", o));
        buffer.append(getSNT());

        // if RefObjectMap
        if (mappingType.equals(MappingTypes.REF_OBJECT_MAP))
            buffer.append(buildSerializedPropertyShape(mappedRefObjectMap));

        // if ObjectMap
        if (mappingType.equals(MappingTypes.OBJECT_MAP))
            buffer.append(buildSerializedPropertyShape(mappedObjectMap));

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        return buffer.toString();
    }
}
