package shaper.mapping;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// nm: Based on: https://www.w3.org/TR/2012/REC-r2rml-20120927/#natural-mapping
// ws: Based on: https://www.ibm.com/support/knowledgecenter/SSRTLW_8.5.1/com.ibm.datatools.dsws.tooling.ui.rad.doc/topics/rdswssupdattyp.html
// mapping: Based on: https://www.w3.org/2001/sw/rdb2rdf/wiki/Mapping_SQL_datatypes_to_XML_Schema_datatypes
public final class DatatypeMap {
	public static final String XSD_ANY_TYPE = "xsd:anyType";
	public static final String XSD_ANY_URI = "xsd:anyURI";
	public static final String XSD_BASE_64_BINARY = "xsd:base64Binary";
	public static final String XSD_BOOLEAN = "xsd:boolean";
	public static final String XSD_DATE = "xsd:date";
	public static final String XSD_DATE_TIME = "xsd:dateTime";
    public static final String XSD_DECIMAL = "xsd:decimal";
    public static final String XSD_DOUBLE = "xsd:double";
	public static final String XSD_HEX_BINARY = "xsd:hexBinary";
    public static final String XSD_INTEGER = "xsd:integer";
	public static final String XSD_STRING = "xsd:string";
	public static final String XSD_TIME = "xsd:time";

	private static Map<Integer, String> map = new Hashtable<>(36);
	
	static {
		map.put(Integer.valueOf(java.sql.Types.ARRAY), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.BIGINT), XSD_INTEGER); // nm
		map.put(Integer.valueOf(java.sql.Types.BINARY), XSD_HEX_BINARY); // nm
		map.put(Integer.valueOf(java.sql.Types.BIT), XSD_HEX_BINARY); // nm
		map.put(Integer.valueOf(java.sql.Types.BLOB), XSD_HEX_BINARY); // nm
		map.put(Integer.valueOf(java.sql.Types.BOOLEAN), XSD_BOOLEAN); // nm
		map.put(Integer.valueOf(java.sql.Types.CHAR), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.CLOB), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.DATALINK), XSD_ANY_URI); // ws
		map.put(Integer.valueOf(java.sql.Types.DATE), XSD_DATE); // nm
		map.put(Integer.valueOf(java.sql.Types.DECIMAL), XSD_DECIMAL); // nm
		map.put(Integer.valueOf(java.sql.Types.DISTINCT), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.DOUBLE), XSD_DOUBLE); // nm
		map.put(Integer.valueOf(java.sql.Types.FLOAT), XSD_DOUBLE); // nm
		map.put(Integer.valueOf(java.sql.Types.INTEGER), XSD_INTEGER); // nm
		map.put(Integer.valueOf(java.sql.Types.JAVA_OBJECT), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.LONGNVARCHAR), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.LONGVARBINARY), XSD_HEX_BINARY); // nm
		map.put(Integer.valueOf(java.sql.Types.LONGVARCHAR), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.NCHAR), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.NCLOB), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.NULL), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.NUMERIC), XSD_DECIMAL); // nm
		map.put(Integer.valueOf(java.sql.Types.NVARCHAR), XSD_STRING); // nm
		map.put(Integer.valueOf(java.sql.Types.OTHER), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.REAL), XSD_DOUBLE); // nm
		map.put(Integer.valueOf(java.sql.Types.REF), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.REF_CURSOR), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.ROWID), XSD_BASE_64_BINARY); // ws
		map.put(Integer.valueOf(java.sql.Types.SMALLINT), XSD_INTEGER); // nm
		map.put(Integer.valueOf(java.sql.Types.SQLXML), XSD_ANY_TYPE); // ws
		map.put(Integer.valueOf(java.sql.Types.STRUCT), XSD_STRING); // ws
		map.put(Integer.valueOf(java.sql.Types.TIME), XSD_TIME); // nm
		map.put(Integer.valueOf(java.sql.Types.TIME_WITH_TIMEZONE), XSD_TIME); // nm
		map.put(Integer.valueOf(java.sql.Types.TIMESTAMP), XSD_DATE_TIME); // nm
		map.put(Integer.valueOf(java.sql.Types.TIMESTAMP_WITH_TIMEZONE), XSD_DATE_TIME); // nm
		map.put(Integer.valueOf(java.sql.Types.TINYINT), XSD_INTEGER); // nm
		map.put(Integer.valueOf(java.sql.Types.VARBINARY), XSD_HEX_BINARY); // nm
		map.put(Integer.valueOf(java.sql.Types.VARCHAR), XSD_STRING); // nm
	}
	
	public static String getMappedXSD(int key) {
		return map.get(Integer.valueOf(key));
	}
	
	public static Set<Integer> getMappedJDBCTypes(String anXSD) {
		Set<Integer> mappedSQLTypes = new CopyOnWriteArraySet<>();
		
		Set<Integer> keySet = map.keySet();
		
		for (Integer key: keySet)
			if (map.get(key).equals(anXSD))
				mappedSQLTypes.add(key);
		
		return mappedSQLTypes;
	}
}
