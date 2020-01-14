package shaper.mapping.model.shacl;

import shaper.mapping.model.r2rml.ObjectMap;
import shaper.mapping.model.r2rml.PredicateMap;
import shaper.mapping.model.r2rml.RefObjectMap;

public class PropertyShape extends Shape {
    private PredicateMap mappedPredicateMap;
    private ObjectMap mappedObjectMap;
    private RefObjectMap mappedRefObjectMap;

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, ObjectMap mappedObjectMap) {
        super(id);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedObjectMap = mappedObjectMap;
    }

    PropertyShape(IRI id, PredicateMap mappedPredicateMap, RefObjectMap mappedRefObjectMap) {
        super(id);
        this.mappedPredicateMap = mappedPredicateMap;
        this.mappedRefObjectMap = mappedRefObjectMap;
    }
}
