package shaper.mapping.metadata.rdf;

import shaper.Shaper;
import janus.database.DBField;
import shaper.mapping.RDFMapper;
import org.apache.jena.ext.com.google.common.net.UrlEscapers;

import java.net.URI;
import java.util.List;

public class RowNode {
	
	// generates a row node with ordered column names.
	static String getMappedRowNodeAfterBase(String table, List<DBField> pkFields) {
		StringBuffer rowNode = new StringBuffer(table);
		rowNode.append(RDFMapper.SLASH);
		
		for (DBField pkField: pkFields) {
			String column = pkField.getColumnName();
			String value = pkField.getValue();
			
			rowNode.append(column);
			rowNode.append(RDFMapper.EQUAL);
			rowNode.append(getEncodedColumnValue(value));
			rowNode.append(RDFMapper.SEMICOLON);
		}
		rowNode.deleteCharAt(rowNode.lastIndexOf(RDFMapper.SEMICOLON));
		
		return rowNode.toString();
	}
	
	static URI getRowNode(String rowNodeAfterBase) {
	    return URI.create(getRowNodeIncludingBase(rowNodeAfterBase));
	}
	
	private static String getRowNodeIncludingBase(String rowNodeAfterBase) {
		return Shaper.rdfMapper.rdfMappingMD.getBaseIRI() + rowNodeAfterBase;
	}

	private static String getEncodedColumnValue(String columnValue) {
        return UrlEscapers.urlPathSegmentEscaper().escape(columnValue);
	}
}