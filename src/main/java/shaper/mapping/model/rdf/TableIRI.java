package shaper.mapping.model.rdf;

class TableIRI implements Comparable<TableIRI> {
	private String tableIRIFragment;

	private String mappedTable;
	
	TableIRI(String mappedTable) {
		this.mappedTable = mappedTable;
		tableIRIFragment = buildTableIRIFragment();
	}
	
	String getTableIRIFragment() { return tableIRIFragment; }
	
	String getMappedTableName() {
		return mappedTable;
	}

	@Override
	public int compareTo(TableIRI o) {
		return tableIRIFragment.compareTo(o.getTableIRIFragment());
	}

	private String buildTableIRIFragment() {
	    return mappedTable;
    }
}
