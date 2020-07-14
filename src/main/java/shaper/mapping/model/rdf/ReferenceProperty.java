package shaper.mapping.model.rdf;

import shaper.Shaper;
import shaper.mapping.Symbols;
import shaper.mapping.model.Utils;

import java.net.URI;
import java.util.List;

public class ReferenceProperty implements Comparable<ReferenceProperty> {
    private URI referencePropertyIRI;
    private String propertyFragment;

    private String mappedTable;
    private String mappedRefConstraint;

    ReferenceProperty(URI baseIRI, String mappedTable, String mappedRefConstraint) {
        this.mappedTable = mappedTable;
        this.mappedRefConstraint = mappedRefConstraint;

        propertyFragment = buildReferencePropertyFragment(mappedTable, mappedRefConstraint);
        referencePropertyIRI = buildReferencePropertyIRI(baseIRI, propertyFragment);
    }

    String getMappedTable() {
        return mappedTable;
    }

    String getMappedRefConstraintName() {
        return mappedRefConstraint;
    }

    String getPropertyFragment() {
        return propertyFragment;
    }

    public URI getReferencePropertyIRI() { return referencePropertyIRI; }

    @Override
    public int compareTo(ReferenceProperty o) {
        return referencePropertyIRI.compareTo(o.getReferencePropertyIRI());
    }

    private URI buildReferencePropertyIRI(URI baseIRI, String propertyFragment) {
        return URI.create(baseIRI + propertyFragment);
    }

    private String buildReferencePropertyFragment(String tableName, String refConstraintName) {
        StringBuffer referenceProperty = new StringBuffer(Utils.encode(tableName));
        referenceProperty.append(Symbols.HASH);
        referenceProperty.append(Symbols.REF);
        referenceProperty.append(Symbols.DASH);

        List<String> referencingColumns = Shaper.dbSchema.getReferencingColumnsByOrdinalPosition(tableName, refConstraintName);
        for (int i = 0; i < referencingColumns.size(); i++) {
            referenceProperty.append(Utils.encode(referencingColumns.get(i)));
            referenceProperty.append(Symbols.SEMICOLON);
        }
        referenceProperty.deleteCharAt(referenceProperty.lastIndexOf(Symbols.SEMICOLON));

        return referenceProperty.toString();
    }

    @Override
    public String toString() {
        return referencePropertyIRI.toString();
    }
}
