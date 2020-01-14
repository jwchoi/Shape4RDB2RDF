package shaper.mapping.model.shacl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeShape extends Shape {
    private Optional<URI> mappedTriplesMap; // mapped rr:TriplesMap

    private List<IRI> propertyShapes;

    NodeShape(IRI id, URI mappedTriplesMap) {
        super(id);
        this.mappedTriplesMap = Optional.of(mappedTriplesMap);

        propertyShapes = new ArrayList<>();
    }

    Optional<URI> getMappedTriplesMap() { return mappedTriplesMap; }

    void addPropertyShape(IRI propertyShape) { propertyShapes.add(propertyShape); }
}
