package shaper.mapping.model.rdf;

import shaper.mapping.model.Utils;

import java.net.URI;

public class TableIRI implements Comparable<TableIRI> {
	private URI tableIRI;
	private String tableIRIFragment;

	private String mappedTable;
	
	TableIRI(URI baseIRI, String mappedTable) {
		this.mappedTable = mappedTable;

		tableIRIFragment = buildTableIRIFragment();
		tableIRI = buildTableIRI(baseIRI, tableIRIFragment);
	}

	public URI getTableIRI() { return tableIRI; }

	public String getTableIRIFragment() { return tableIRIFragment; }
	
	public String getMappedTableName() {
		return mappedTable;
	}

	@Override
	public int compareTo(TableIRI o) {
		return tableIRI.compareTo(o.getTableIRI());
	}

	private URI buildTableIRI(URI baseIRI, String tableIRIFragment) {
		return URI.create(baseIRI + tableIRIFragment);
	}

	private String buildTableIRIFragment() { return Utils.encode(mappedTable); }

	@Override
	public String toString() {
		return getTableIRI().toString();
	}
}
