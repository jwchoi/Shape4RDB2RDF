package shaper.mapping.model.rdf;

import shaper.mapping.Symbols;
import shaper.mapping.model.Utils;

class LiteralProperty implements Comparable<LiteralProperty> {
	
	private String propertyFragment;
	
	private String mappedTable;
	private String mappedColumn;

	LiteralProperty(String mappedTable, String mappedColumn) {
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;

        this.propertyFragment = buildLiteralPropertyFragment(mappedTable, mappedColumn);
	}
	
	String getMappedTable() {
		return mappedTable;
	}
	
	String getMappedColumn() {
		return mappedColumn;
	}
	
	String getPropertyFragment() {
		return propertyFragment;
	}

	private String buildLiteralPropertyFragment(String table, String column) {
		return Utils.encode(table) + Symbols.HASH + Utils.encode(column);
	}

	@Override
	public int compareTo(LiteralProperty o) {
		return propertyFragment.compareTo(o.getPropertyFragment());
	}
}
