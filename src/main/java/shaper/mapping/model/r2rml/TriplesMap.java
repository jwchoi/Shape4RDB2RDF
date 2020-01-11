package shaper.mapping.model.r2rml;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TriplesMap {

    private URI uri;
    private LogicalTable logicalTable;
    private SubjectMap subjectMap;
    private List<PredicateObjectMap> predicateObjectMaps;

    public TriplesMap(URI uri, LogicalTable logicalTable, SubjectMap subjectMap) {
        this.uri = uri;
        this.logicalTable = logicalTable;
        this.subjectMap = subjectMap;
        predicateObjectMaps = new ArrayList<>();
    }

    public void addPredicateObjectMap(PredicateObjectMap predicateObjectMap) { predicateObjectMaps.add(predicateObjectMap); }

    public LogicalTable getLogicalTable() { return logicalTable; }

    public SubjectMap getSubjectMap() { return subjectMap; }

    public URI getUri() { return uri; }

    public List<PredicateObjectMap> getPredicateObjectMaps() { return predicateObjectMaps; }
}
