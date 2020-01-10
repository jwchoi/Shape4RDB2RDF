package shaper.mapping.metadata.r2rml;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RefObjectMap {

    private URI parentTriplesMap;
    private List<JoinCondition> joinConditions;

    public RefObjectMap(URI parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
        joinConditions = new ArrayList<>();
    }

    public void addJoinCondition(String child, String parent) { joinConditions.add(new JoinCondition(child, parent)); }

    public URI getParentTriplesMap() { return parentTriplesMap; }
}
