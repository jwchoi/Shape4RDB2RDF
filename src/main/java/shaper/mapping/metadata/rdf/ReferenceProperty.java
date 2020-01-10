package shaper.mapping.metadata.rdf;

import shaper.Shaper;
import shaper.mapping.RDFMapper;

import java.util.List;

public class ReferenceProperty implements Comparable<ReferenceProperty> {

    private String propertyFragment;

    private String mappedTable;
    private String mappedRefConstraint;

    ReferenceProperty(String mappedTable, String mappedRefConstraint) {
        this.mappedTable = mappedTable;
        this.mappedRefConstraint = mappedRefConstraint;

        propertyFragment = buildReferencePropertyFragment(mappedTable, mappedRefConstraint);
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

    @Override
    public int compareTo(ReferenceProperty o) {
        return propertyFragment.compareTo(o.getPropertyFragment());
    }

    private String buildReferencePropertyFragment(String tableName, String refConstraintName) {
        StringBuffer referenceProperty = new StringBuffer(tableName);
        referenceProperty.append(RDFMapper.HASH);
        referenceProperty.append(RDFMapper.REF);
        referenceProperty.append(RDFMapper.DASH);

        List<String> referencingColumns = Shaper.localDBSchema.getReferencingColumnsByOrdinalPosition(tableName, refConstraintName);
        for (int i = 0; i < referencingColumns.size(); i++) {
            referenceProperty.append(referencingColumns.get(i));
            referenceProperty.append(RDFMapper.SEMICOLON);
        }
        referenceProperty.deleteCharAt(referenceProperty.lastIndexOf(RDFMapper.SEMICOLON));

        return referenceProperty.toString();
    }
}
