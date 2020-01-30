package shaper.mapping.model.shacl;

import janus.database.SQLSelectField;
import shaper.mapping.DatatypeMap;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;

import java.net.URI;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Optional;

public class PropertyShape extends Shape {
    private enum MappingTypes { OBJECT_MAP, REF_OBJECT_MAP }

    private PredicateMap mappedPredicateMap;
    private Optional<ObjectMap> mappedObjectMap = Optional.empty();
    private Optional<RefObjectMap> mappedRefObjectMap = Optional.empty();

    private MappingTypes mappingType;

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, ObjectMap mappedObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedObjectMap = Optional.of(mappedObjectMap);

        mappingType = MappingTypes.OBJECT_MAP;
    }

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, RefObjectMap mappedRefObjectMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedRefObjectMap = Optional.of(mappedRefObjectMap);

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
        o = getShaclDocModel().getRelativeIRIOr(mappedRefObjectMap.getParentTriplesMap().toString());
        buffer.append(getPO("sh:node", o));
        buffer.append(getSNT());

        return buffer.toString();
    }

    private String buildSerializedPropertyShape(ObjectMap mappedObjectMap) {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        // sh:nodeKind
        Optional<TermMap.TermTypes> termType = mappedObjectMap.getTermType();
        if (termType.isPresent()) {

            switch (termType.get()) {
                case BLANKNODE: o = "sh:BlankNode"; break;
                case IRI: o = "sh:IRI"; break;
                case LITERAL: o = "sh:Literal"; break;
                default: o = null;
            }

            if (o != null) {
                buffer.append(getPO("sh:nodeKind", o));
                buffer.append(getSNT());
            }
        }

        // sh:languageIn
        Optional<String> language = mappedObjectMap.getLanguage();
        if (language.isPresent()) {
            o = Symbols.OPEN_PARENTHESIS + Symbols.SPACE + Symbols.DOUBLE_QUOTATION_MARK + language.get() + Symbols.DOUBLE_QUOTATION_MARK + Symbols.SPACE + Symbols.CLOSE_PARENTHESIS;
            buffer.append(getPO("sh:languageIn", o));
            buffer.append(getSNT());
        }

        // sh:datatype
        o = null;

        Optional<URI> datatype = mappedObjectMap.getDatatype();
        if (datatype.isPresent()) {
            // from rr:column
            o = getShaclDocModel().getRelativeIRIOr(datatype.get());
        } else {
            // Natural Mapping of SQL Values
            Optional<SQLSelectField> sqlSelectField = mappedObjectMap.getColumn();
            if (sqlSelectField.isPresent())
                o = DatatypeMap.getMappedXSD(sqlSelectField.get().getSqlType());
        }

        if (o != null) {
            buffer.append(getPO("sh:datatype", o));
            buffer.append(getSNT());
        }

        // sh:pattern
        Optional<String> regex = getRegex(mappedObjectMap);
        if (regex.isPresent()) {
            o = regex.get();
            buffer.append(getPO("sh:pattern", o));
            buffer.append(getSNT());
        }

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
        String id = getShaclDocModel().getRelativeIRIOr(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        buffer.append(getPO(Symbols.A, "sh:PropertyShape"));
        buffer.append(getSNT());

        // sh:path
        o = getShaclDocModel().getRelativeIRIOr(mappedPredicateMap.getConstant().get());
        buffer.append(getPO("sh:path", o));
        buffer.append(getSNT());

        // if RefObjectMap
        if (mappingType.equals(MappingTypes.REF_OBJECT_MAP))
            buffer.append(buildSerializedPropertyShape(mappedRefObjectMap.get()));

        // if ObjectMap
        if (mappingType.equals(MappingTypes.OBJECT_MAP))
            buffer.append(buildSerializedPropertyShape(mappedObjectMap.get()));

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        return buffer.toString();
    }

    private Optional<String> getRegex(ObjectMap objectMap) {
        Optional<Template> template = objectMap.getTemplate();

        if (!isPossibleToHavePattern(template)) return Optional.empty();

        String regex = template.get().getFormat();

        // column names
        List<SQLSelectField> columnNames = template.get().getColumnNames();
        for (SQLSelectField columnName: columnNames)
            regex = regex.replace("{" + columnName.getColumnNameOrAlias() + "}", "(.*)");

        return Optional.of(Symbols.DOUBLE_QUOTATION_MARK + Symbols.CARET + regex + Symbols.DOLLAR + Symbols.DOUBLE_QUOTATION_MARK);
    }
}
