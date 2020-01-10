package shaper.mapping.metadata.rdf;

import janus.database.DBField;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RDFMappingMetadata {
	private URI baseIRI;

	private Set<TableIRI> tableIRIs;
	private Set<LiteralProperty> literalProperties;
	private Set<ReferenceProperty> referenceProperties;
	
	public RDFMappingMetadata(URI baseIRI) {
		this.baseIRI = baseIRI;
		
		tableIRIs = new CopyOnWriteArraySet<>();
		literalProperties = new CopyOnWriteArraySet<>();
        referenceProperties = new CopyOnWriteArraySet<>();
	}
	
	void addTableIRIMetaData(TableIRI classMetaData) {
		tableIRIs.add(classMetaData);
	}
	
	void addLiteralPropertyMetaData(LiteralProperty propertyMetaData) {
		literalProperties.add(propertyMetaData);
	}

    void addReferencePropertyMetaData(ReferenceProperty referenceProperty) {
        referenceProperties.add(referenceProperty);
    }

	public String getMappedTableIRI(String table) {
		for (TableIRI tableIRI: tableIRIs)
			if (tableIRI.getMappedTableName().equals(table))
				return tableIRI.getTableIRIFragment();

		return null;
	}

    public String getMappedRowNode(String table, List<DBField> pkFields) {
        return RowNode.getMappedRowNodeAfterBase(table, pkFields);
    }

    public String getMappedBlankNode(String table, List<DBField> dbFields) {
	    return BlankNode.getMappedBlankNodeFragment(table, dbFields);
    }

    public String getMappedLiteralProperty(String table, String column) {
        for (LiteralProperty property : literalProperties)
            if (property.getMappedTable().equals(table)
                    && property.getMappedColumn().equals(column))
                return property.getPropertyFragment();

        return null;
    }

	public String getMappedLiteral(String table, String column, String value) {
		return Literal.getMappedLiteral(table, column, value);
	}

	public String getMappedReferenceProperty(String table, String refConstraint) {
		for (ReferenceProperty property : referenceProperties)
			if (property.getMappedTable().equals(table)
					&& property.getMappedRefConstraintName().equals(refConstraint))
				return property.getPropertyFragment();

		return null;
	}
	
	URI getBaseIRI() {
		return baseIRI;
	}
}
