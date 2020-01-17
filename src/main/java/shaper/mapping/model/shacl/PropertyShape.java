package shaper.mapping.model.shacl;

import janus.database.SQLSelectField;
import shaper.Shaper;
import shaper.mapping.DatatypeMap;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;
import shaper.mapping.model.shex.NodeKinds;

import java.net.URI;
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

        Optional<String> datatype = mappedObjectMap.getDatatype();
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
        if (isPossibleToHavePattern(mappedObjectMap)) {
            o = buildRegex(mappedObjectMap.getTemplate().get());
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
            buffer.append(buildSerializedPropertyShape(mappedRefObjectMap));

        // if ObjectMap
        if (mappingType.equals(MappingTypes.OBJECT_MAP))
            buffer.append(buildSerializedPropertyShape(mappedObjectMap));

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        return buffer.toString();
    }

    private String buildRegex(Template template) {
        String regex = template.getFormat();

        // replace meta-characters in XPath
        regex = regex.replace(Symbols.SLASH, Symbols.BACKSLASH + Symbols.SLASH);
        regex = regex.replace(Symbols.DOT, Symbols.BACKSLASH + Symbols.DOT);
        if (!template.isIRIPattern()) { // for LITERAL
//            regex = regex.replace(RDFMapper.BACKSLASH, RDFMapper.BACKSLASH + RDFMapper.BACKSLASH);
//            regex = regex.replace(RDFMapper.SLASH, RDFMapper.BACKSLASH + RDFMapper.SLASH);
//            regex = regex.replace(RDFMapper.DOT, RDFMapper.BACKSLASH + RDFMapper.DOT);
//            regex = regex.replace(RDFMapper.QUESTION_MARK, RDFMapper.BACKSLASH + RDFMapper.QUESTION_MARK);
//            regex = regex.replace(RDFMapper.PLUS, RDFMapper.BACKSLASH + RDFMapper.PLUS);
//            regex = regex.replace(RDFMapper.ASTERISK, RDFMapper.BACKSLASH + RDFMapper.ASTERISK);
//            regex = regex.replace(RDFMapper.OR, RDFMapper.BACKSLASH + RDFMapper.OR);
//            regex = regex.replace(RDFMapper.CARET, RDFMapper.BACKSLASH + RDFMapper.CARET);
//            regex = regex.replace(RDFMapper.DOLLAR, RDFMapper.BACKSLASH + RDFMapper.DOLLAR);
//            regex = regex.replace(RDFMapper.OPEN_PARENTHESIS, RDFMapper.BACKSLASH + RDFMapper.OPEN_PARENTHESIS);
//            regex = regex.replace(RDFMapper.CLOSE_PARENTHESIS, RDFMapper.BACKSLASH + RDFMapper.CLOSE_PARENTHESIS);
        }

        // column names
        List<SQLSelectField> columnNames = template.getColumnNames();
        for (SQLSelectField columnName: columnNames)
            regex = regex.replace("{" + columnName.getColumnNameOrAlias() + "}", "(.*)");

        return Symbols.DOUBLE_QUOTATION_MARK + Symbols.CARET + regex + Symbols.DOLLAR + Symbols.DOUBLE_QUOTATION_MARK;
    }

    private boolean isPossibleToHavePattern(ObjectMap objectMap) {
        Optional<Template> template = objectMap.getTemplate();
        if (template.isPresent()) {
            if (template.get().getLengthExceptColumnName() > 0)
                return true;
        }

        return false;
    }
}
