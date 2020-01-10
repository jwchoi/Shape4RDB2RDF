package shaper.mapping.metadata.shex;

import shaper.Shaper;
import janus.database.DBColumn;
import janus.database.DBRefConstraint;
import janus.database.SQLSelectField;
import shaper.mapping.RDFMapper;
import shaper.mapping.metadata.r2rml.*;

import java.net.URI;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class TripleConstraint implements Comparable<TripleConstraint> {

    enum MappedTypes { COLUMN, REF_CONSTRAINT, TABLE, RR_CLASSES, PREDICATE_OBJECT_MAP, REF_OBJECT_MAP }

    private String tripleConstraint;

    private MappedTypes mappedType;

    private String mappedTable;
    private DBColumn mappedColumn;
    private DBRefConstraint mappedRefConstraint;

    private Optional<Boolean> isInverse = Optional.empty();

    TripleConstraint(String mappedTable) {
        this.mappedTable = mappedTable;
        mappedType = MappedTypes.TABLE;
    }

    TripleConstraint(DBColumn mappedColumn) {
        this.mappedColumn = mappedColumn;
        mappedType = MappedTypes.COLUMN;
    }

    TripleConstraint(DBRefConstraint mappedRefConstraint, boolean isInverse) {
        this.mappedRefConstraint = mappedRefConstraint;
        mappedType = MappedTypes.REF_CONSTRAINT;
        this.isInverse = Optional.of(isInverse);
    }

    String getMappedTable() {
        return mappedTable;
    }

    Optional<Boolean> isInverse() {
        return isInverse;
    }

    @Override
    public String toString() {
        if (tripleConstraint == null)
         tripleConstraint = buildTripleConstraint();

        return tripleConstraint;
    }

    private String buildTripleConstraint() {
        String tripleConstraint = null;
        switch (mappedType) {
            case COLUMN:
                tripleConstraint = buildTripleConstraintFromColumn();
                break;
            case REF_CONSTRAINT:
                tripleConstraint = isInverse.get() ? buildInverseTripleConstraintFromRefConstraint() : buildTripleConstraintFromRefConstraint();
                break;
            case TABLE:
                tripleConstraint = buildTripleConstraintFromTable();
                break;
            case RR_CLASSES:
                tripleConstraint = buildTripleConstraintFromClasses();
                break;
            case PREDICATE_OBJECT_MAP:
                tripleConstraint = buildTripleConstraintFromPredicateObjectMap();
                break;
            case REF_OBJECT_MAP:
                tripleConstraint = buildTripleConstraintFromRefObjectMap();
                break;
        }
        return tripleConstraint;
    }

    private String buildInverseTripleConstraintFromRefConstraint() {
        String table = mappedRefConstraint.getTableName();
        String refConstraint = mappedRefConstraint.getRefConstraintName();
        String refProperty = RDFMapper.LT + Shaper.rdfMapper.rdfMappingMD.getMappedReferenceProperty(table, refConstraint) + RDFMapper.GT;

        String prefix = Shaper.rdfMapper.shExSchema.getPrefix();
        String referencedShape = prefix + RDFMapper.COLON + Shaper.rdfMapper.shExSchema.getMappedShapeID(table);

        cardinality = RDFMapper.ASTERISK;

        return RDFMapper.CARET + refProperty + RDFMapper.SPACE + RDFMapper.AT + referencedShape + cardinality;
    }

    private String buildTripleConstraintFromRefConstraint() {
        String table = mappedRefConstraint.getTableName();
        String refConstraint = mappedRefConstraint.getRefConstraintName();
        String refProperty = RDFMapper.LT + Shaper.rdfMapper.rdfMappingMD.getMappedReferenceProperty(table, refConstraint) + RDFMapper.GT;

        String referencedTable = Shaper.localDBSchema.getReferencedTableBy(table, refConstraint);
        String prefix = Shaper.rdfMapper.shExSchema.getPrefix();
        String referencedShape = prefix + RDFMapper.COLON + Shaper.rdfMapper.shExSchema.getMappedShapeID(referencedTable);

        List<String> columns = Shaper.localDBSchema.getReferencingColumnsByOrdinalPosition(table, refConstraint);
        boolean nullable = false;
        for (String column: columns) {
            if (!Shaper.localDBSchema.isNotNull(mappedTable, column)) {
                nullable = true;
                break;
            }
        }
        cardinality = nullable ? "?" : "";

        return refProperty + RDFMapper.SPACE + RDFMapper.AT + referencedShape + cardinality;
    }

    private String buildTripleConstraintFromColumn() {
        String table = mappedColumn.getTableName();
        String column = mappedColumn.getColumnName();
        String litProperty = RDFMapper.LT + Shaper.rdfMapper.rdfMappingMD.getMappedLiteralProperty(table, column) + RDFMapper.GT;

        String prefix = Shaper.rdfMapper.shExSchema.getPrefix();
        String nodeConstraint = prefix + RDFMapper.COLON + Shaper.rdfMapper.shExSchema.getMappedNodeConstraintID(table, column);

        boolean nullable = false;
        if (!Shaper.localDBSchema.isNotNull(table, column))
            nullable = true;

        cardinality = nullable ? "?" : "";

        return litProperty + RDFMapper.SPACE + RDFMapper.AT + nodeConstraint + cardinality;
    }

    private String buildTripleConstraintFromTable() {
        String rdfType = RDFMapper.A;

        String tableIRI = RDFMapper.LT + Shaper.rdfMapper.rdfMappingMD.getMappedTableIRI(mappedTable) + RDFMapper.GT;

        cardinality = "";

        return rdfType + RDFMapper.SPACE + RDFMapper.OPEN_BRACKET + tableIRI + RDFMapper.CLOSE_BRACKET + cardinality;
    }

    @Override
    public int compareTo(TripleConstraint o) {
        return toString().compareTo(o.toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<URI> classIRIs;
    private PredicateMap predicateMap;
    private ObjectMap objectMap;
    private RefObjectMap refObjectMap;

    private String cardinality;

    TripleConstraint(List<URI> classIRIs) {
        this.classIRIs = new TreeSet<>(classIRIs);
        mappedType = MappedTypes.RR_CLASSES;
    }

    TripleConstraint(PredicateMap predicateMap, ObjectMap objectMap) {
        this.predicateMap = predicateMap;
        this.objectMap = objectMap;
        mappedType = MappedTypes.PREDICATE_OBJECT_MAP;
    }

    TripleConstraint(PredicateMap predicateMap, RefObjectMap refObjectMap) {
        this.predicateMap = predicateMap;
        this.refObjectMap = refObjectMap;
        mappedType = MappedTypes.REF_OBJECT_MAP;
    }

    Optional<RefObjectMap> getRefObjectMap() { return Optional.ofNullable(refObjectMap); }

    private String buildTripleConstraintFromRefObjectMap() {
        // property
        String property = buildProperty(predicateMap);

        // shapeRef
        String prefix = Shaper.rdfMapper.shExSchema.getPrefix();
        String shapeRef = prefix + RDFMapper.COLON + Shaper.rdfMapper.shExSchema.getMappedShape(refObjectMap.getParentTriplesMap()).getShapeID();

        // cardinality
        cardinality = RDFMapper.ASTERISK;

        return property + RDFMapper.SPACE + RDFMapper.AT + shapeRef + RDFMapper.SPACE + cardinality;
    }

    private String buildProperty(PredicateMap predicateMap) {
        String property = predicateMap.getConstant().get();
        Optional<String> relativeIRI = Shaper.rdfMapper.r2rmlModel.getRelativeIRI(URI.create(property));
        if (relativeIRI.isPresent())
            property = relativeIRI.get();
        else
            property = RDFMapper.LT + property + RDFMapper.GT;

        return property;
    }

    private String buildTripleConstraintFromClasses() {
        String rdfType = RDFMapper.A;

        StringBuffer classes = new StringBuffer(RDFMapper.SPACE);
        for (URI classIRI: classIRIs) {
            Optional<String> relativeIRI = Shaper.rdfMapper.r2rmlModel.getRelativeIRI(classIRI);
            String clsIRI = relativeIRI.isPresent() ? relativeIRI.get() : classIRI.toString();

            classes.append(clsIRI + RDFMapper.SPACE);
        }

        // cardinality
        int sizeOfClassIRIs = classIRIs.size();
        if (sizeOfClassIRIs == 1) cardinality = "";
        else cardinality = RDFMapper.OPEN_BRACE + sizeOfClassIRIs + RDFMapper.CLOSE_BRACE;

        return rdfType + RDFMapper.SPACE + RDFMapper.OPEN_BRACKET + classes + RDFMapper.CLOSE_BRACKET + RDFMapper.SPACE + cardinality;
    }

    private String buildTripleConstraintFromPredicateObjectMap() {
        // property
        String property = buildProperty(predicateMap);

        // cardinality
        Optional<SQLSelectField> sqlSelectField = objectMap.getColumn();
        if (sqlSelectField.isPresent()) {
            switch (sqlSelectField.get().getNullable()) {
                case ResultSetMetaData.columnNoNulls:
                    cardinality = ""; // the default of "exactly one"
                    break;
                case ResultSetMetaData.columnNullable:
                case ResultSetMetaData.columnNullableUnknown:
                    cardinality = RDFMapper.QUESTION_MARK; // "?" - zero or one
            }
        } else {
            Optional<Template> template = objectMap.getTemplate();
            if (template.isPresent()) {
                List<SQLSelectField> columnNames = template.get().getColumnNames();
                cardinality = ""; // the default of "exactly one"
                for (SQLSelectField columnName: columnNames) {
                    if (columnName.getNullable() != ResultSetMetaData.columnNoNulls) {
                        cardinality = RDFMapper.QUESTION_MARK; // "?" - zero or one
                        break;
                    }
                }
            }
        }

        if (NodeConstraint.isPossibleToHaveXSFacet(objectMap)) {
            String prefix = Shaper.rdfMapper.shExSchema.getPrefix();
            return property + RDFMapper.SPACE + RDFMapper.AT + prefix + RDFMapper.COLON + Shaper.rdfMapper.shExSchema.getMappedNodeConstraintID(objectMap) + RDFMapper.SPACE + cardinality;
        } else
            return property + RDFMapper.SPACE + Shaper.rdfMapper.shExSchema.getMappedNodeConstraint(objectMap) + RDFMapper.SPACE + cardinality;
    }

    MappedTypes getMappedType() { return mappedType; }

    Set<URI> getClassIRIs() { return classIRIs; }
}