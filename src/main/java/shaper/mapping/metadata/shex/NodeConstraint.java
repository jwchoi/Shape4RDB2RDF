package shaper.mapping.metadata.shex;

import shaper.Shaper;
import janus.database.SQLSelectField;
import shaper.mapping.DatatypeMap;
import shaper.mapping.RDFMapper;
import shaper.mapping.metadata.r2rml.ObjectMap;
import shaper.mapping.metadata.r2rml.Template;
import shaper.mapping.metadata.r2rml.TermMap;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NodeConstraint implements Comparable<NodeConstraint> {

    private enum XSFacets {
        MAX_LENGTH("MAXLENGTH"),
        MIN_INCLUSIVE("MININCLUSIVE"), MAX_INCLUSIVE("MAXINCLUSIVE"),
        TOTAL_DIGITS("TOTALDIGITS"), FRACTION_DIGITS("FRACTIONDIGITS");

        private final String facet;

        XSFacets(String facet) {
            this.facet = facet;
        }

        @Override
        public String toString() { return facet; }
    }

    private String nodeConstraint;
    private String id;

    private String mappedTable;
    private String mappedColumn;

    NodeConstraint(String mappedTable, String mappedColumn) {
        this();
        this.mappedTable = mappedTable;
        this.mappedColumn = mappedColumn;
        this.id = buildNodeConstraintID(mappedTable, mappedColumn);
    }

    String getNodeConstraintID() { return id; }

    String getMappedTable() {
        return mappedTable;
    }

    String getMappedColumn() {
        return mappedColumn;
    }

    private String buildNodeConstraintID(String mappedTable, String mappedColumn) {
        return mappedTable + Character.toUpperCase(mappedColumn.charAt(0)) + mappedColumn.substring(1);
    }

    @Override
    public String toString() {
        if (nodeConstraint == null) {

            if (mappedTable != null && mappedColumn != null)
                buildNodeConstraint(mappedTable, mappedColumn);
            else
                buildNodeConstraint(mappedObjectMap);

            StringBuffer nodeConstraint = new StringBuffer();

            if (valueSet.isPresent())
                nodeConstraint.append(valueSet.get());
            else if (datatype.isPresent())
                nodeConstraint.append(datatype.get());
            else if (nodeKind.isPresent())
                nodeConstraint.append(nodeKind.get());

            if (xsFacet.isPresent())
                nodeConstraint.append(RDFMapper.SPACE + xsFacet.get());

            this.nodeConstraint = nodeConstraint.toString();
        }

        return this.nodeConstraint;
    }

    private void buildNodeConstraint(String table, String column) {
        valueSet = buildValueSet(table, column);
        if (valueSet.isPresent())
            return;

        datatype = Optional.of(DatatypeMap.getMappedXSD(Shaper.localDBSchema.getJDBCDataType(table, column)));

        xsFacet = buildFacet(table, column, datatype.get());
    }

    private Optional<String> buildValueSet(String table, String column) {
        Optional<Set<String>> valueSet = Shaper.localDBSchema.getValueSet(table, column);

        if (valueSet.isPresent()) {
            StringBuffer buffer = new StringBuffer(RDFMapper.OPEN_BRACKET + RDFMapper.SPACE);

            Set<String> set = valueSet.get();
            for (String value: set)
                buffer.append(value + RDFMapper.SPACE);

            buffer.append(RDFMapper.CLOSE_BRACKET);

            return Optional.of(buffer.toString());
        } else
            return Optional.empty();
    }

    private Optional<String> buildFacet(String table, String column, String xsd) {
        String facet = null;
        switch (xsd) {
            case DatatypeMap.XSD_BOOLEAN:
                facet = "/true|false/";
                break;
            case DatatypeMap.XSD_DATE:
                facet = Shaper.localDBSchema.getRegexForXSDDate();
                break;
            case DatatypeMap.XSD_DATE_TIME:
                facet = Shaper.localDBSchema.getRegexForXSDDateTime(table, column).get();
                break;
            case DatatypeMap.XSD_DECIMAL:
                Integer numericPrecision = Shaper.localDBSchema.getNumericPrecision(table, column).get();
                Integer numericScale = Shaper.localDBSchema.getNumericScale(table, column).get();
                facet = XSFacets.TOTAL_DIGITS + RDFMapper.SPACE + numericPrecision + RDFMapper.SPACE + XSFacets.FRACTION_DIGITS + RDFMapper.SPACE + numericScale;
                break;
            case DatatypeMap.XSD_DOUBLE:
                boolean isUnsigned = Shaper.localDBSchema.isUnsigned(table, column);
                facet = isUnsigned ? XSFacets.MIN_INCLUSIVE + RDFMapper.SPACE + RDFMapper.ZERO : null;
                break;
            case DatatypeMap.XSD_HEX_BINARY:
                Integer maximumOctetLength = Shaper.localDBSchema.getMaximumOctetLength(table, column).get();
                facet = XSFacets.MAX_LENGTH + RDFMapper.SPACE + (maximumOctetLength*2);
                break;
            case DatatypeMap.XSD_INTEGER:
                String minimumIntegerValue = Shaper.localDBSchema.getMinimumIntegerValue(table, column).get();
                String maximumIntegerValue = Shaper.localDBSchema.getMaximumIntegerValue(table, column).get();
                facet = XSFacets.MIN_INCLUSIVE + RDFMapper.SPACE + minimumIntegerValue + RDFMapper.SPACE + XSFacets.MAX_INCLUSIVE + RDFMapper.SPACE + maximumIntegerValue;
                break;
            case DatatypeMap.XSD_STRING:
                Integer characterMaximumLength = Shaper.localDBSchema.getCharacterMaximumLength(table, column).get();
                facet = XSFacets.MAX_LENGTH + RDFMapper.SPACE + characterMaximumLength;
                break;
            case DatatypeMap.XSD_TIME:
                facet = Shaper.localDBSchema.getRegexForXSDTime(table, column).get();
                break;
        }

        return Optional.ofNullable(facet);
    }

    @Override
    public int compareTo(NodeConstraint o) {
        return nodeConstraint.compareTo(o.toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Optional<NodeKinds> nodeKind;
    private Optional<String> valueSet;
    private Optional<String> datatype;
    private Optional<String> xsFacet;

    private ObjectMap mappedObjectMap;

    private NodeConstraint() {
        nodeKind = Optional.empty();
        valueSet = Optional.empty();
        datatype = Optional.empty();
        xsFacet = Optional.empty();
    }

    NodeConstraint(String id, ObjectMap mappedObjectMap) {
        this();
        this.id = id;
        this.mappedObjectMap = mappedObjectMap;
    }

    ObjectMap getMappedObjectMap() { return mappedObjectMap; }

    public static boolean isPossibleToHaveXSFacet(ObjectMap objectMap) {
        Optional<Template> template = objectMap.getTemplate();
        if (template.isPresent()) {
            if (template.get().getLengthExceptColumnName() > 0)
                return true;
        }

        return false;
    }

    private void buildNodeConstraint(ObjectMap objectMap) {
        // nodeKind
        Optional<TermMap.TermTypes> termType = objectMap.getTermType();
        if (termType.isPresent()) {
            if (termType.get().equals(TermMap.TermTypes.BLANKNODE))
                nodeKind = Optional.of(NodeKinds.BNODE);
            else if (termType.get().equals(TermMap.TermTypes.IRI))
                nodeKind = Optional.of(NodeKinds.IRI);
            else if (termType.get().equals(TermMap.TermTypes.LITERAL))
                nodeKind = Optional.of(NodeKinds.LITERAL);
        }

        // language
        Optional<String> language = objectMap.getLanguage();
        if (language.isPresent()) {
            String languageTag = RDFMapper.OPEN_BRACKET + RDFMapper.AT + language.get() + RDFMapper.CLOSE_BRACKET;
            valueSet = Optional.of(languageTag);
        }

        // datatype
        Optional<String> datatype = objectMap.getDatatype();
        if (datatype.isPresent()) { // from rr:column
            String dt = datatype.get();
            Optional<String> relativeDatatype = Shaper.rdfMapper.r2rmlModel.getRelativeIRI(URI.create(dt));
            if (relativeDatatype.isPresent())
                dt = relativeDatatype.get();

            this.datatype = Optional.of(dt);
        } else { // Natural Mapping of SQL Values
            Optional<SQLSelectField> sqlSelectField = objectMap.getColumn();
            if (sqlSelectField.isPresent())
                this.datatype = Optional.of(DatatypeMap.getMappedXSD(sqlSelectField.get().getSqlType()));
        }

        // xsFacet, only REGEXP
        if (isPossibleToHaveXSFacet(objectMap))
            xsFacet = Optional.of(buildRegex(objectMap.getTemplate().get()));
    }

    private String buildRegex(Template template) {
        String regex = template.getFormat();

        // replace meta-characters in XPath
        regex = regex.replace(RDFMapper.SLASH, RDFMapper.BACKSLASH + RDFMapper.SLASH);
        regex = regex.replace(RDFMapper.DOT, RDFMapper.BACKSLASH + RDFMapper.DOT);
        if (!template.isIRIPattern()) { // for LITERAL
            //regex = regex.replace(RDFMapper.BACKSLASH, RDFMapper.BACKSLASH + RDFMapper.BACKSLASH);
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

        return RDFMapper.SLASH + RDFMapper.CARET + regex + RDFMapper.DOLLAR + RDFMapper.SLASH;
    }
}
