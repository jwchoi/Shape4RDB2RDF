package shaper.mapping.metadata.rdf;

import shaper.Shaper;
import shaper.mapping.DatatypeMap;

class Literal {

	// referenced: https://www.w3.org/TR/2012/REC-r2rml-20120927/#natural-mapping
    static String getMappedLiteral(String table, String column, String value) {
        String literal;

	    int sqlDataType = Shaper.localDBSchema.getJDBCDataType(table, column);
        String xmlSchemaDataType = DatatypeMap.getMappedXSD(sqlDataType);

        switch (xmlSchemaDataType) {
            case DatatypeMap.XSD_STRING:
                literal = getStringLiteral(value);
                break;
            case DatatypeMap.XSD_DATE_TIME:
                value = value.replace(' ', 'T');
                literal = buildTypedLiteral(value, xmlSchemaDataType);
                break;
            case DatatypeMap.XSD_BOOLEAN:
                value = value.toLowerCase();
                if (value.equals("0")) value = "false";
                if (value.equals("1")) value = "true";
                literal = buildTypedLiteral(value, xmlSchemaDataType);
                break;
            default:
                literal = buildTypedLiteral(value, xmlSchemaDataType);
        }

        return literal;
    }

    private static String buildTypedLiteral(String lexicalValue, String anXsd) {
        return "\"" + lexicalValue + "\""  + "^^" + anXsd;
    }

    // https://www.w3.org/TR/turtle/#literals
    private static String getStringLiteral(String value) {
	    String qMark = "\"";
	    if (value.contains("\""))
	        qMark = "'";

	    if (value.contains("\n"))
	        return qMark + qMark + qMark + value + qMark + qMark + qMark;
	    else
	        return qMark + value + qMark;
    }
}
