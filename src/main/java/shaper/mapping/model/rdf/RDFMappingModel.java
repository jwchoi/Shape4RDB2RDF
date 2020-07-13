package shaper.mapping.model.rdf;

import janus.database.DBField;
import shaper.Shaper;
import shaper.mapping.Symbols;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RDFMappingModel {
	private URI baseIRI;
	private String prefix;

	private Set<TableIRI> tableIRIs;
	private Set<LiteralProperty> literalProperties;
	private Set<ReferenceProperty> referenceProperties;
	
	RDFMappingModel(URI baseIRI, String prefix) {
		this.baseIRI = baseIRI;
		this.prefix = prefix;
		
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

    public Set<TableIRI> getTableIRIs() { return tableIRIs; }

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
	
	public URI getBaseIRI() { return baseIRI; }
	public String getPrefix() { return prefix; }

	public Set<LiteralProperty> getLiteralProperties(TableIRI tableIRI) {
		Set<LiteralProperty> set = new HashSet<>();
		String mappedTable = tableIRI.getMappedTableName();

		for (LiteralProperty lp: literalProperties) {
			if (lp.getMappedTable().equals(mappedTable))
				set.add(lp);
		}

		return set;
	}

	public Set<ReferenceProperty> getReferenceProperties(TableIRI tableIRI, boolean isInverse) {
		Set<ReferenceProperty> set = new HashSet<>();
		String mappedTable = tableIRI.getMappedTableName();

		if (isInverse) {
			for (ReferenceProperty rp : referenceProperties) {
				String referencedTable = Shaper.dbSchema.getReferencedTableBy(rp.getMappedTable(), rp.getMappedRefConstraintName());
				if (referencedTable.equals(mappedTable))
					set.add(rp);
			}
		} else {
			for (ReferenceProperty rp : referenceProperties) {
				if (rp.getMappedTable().equals(mappedTable))
					set.add(rp);
			}
		}

		return set;
	}
}
