package shaper.mapping.model.shacl;

import shaper.mapping.Symbols;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeShape extends Shape {
    private Optional<URI> mappedTriplesMap; // mapped rr:TriplesMap

    private List<IRI> propertyShapes;

    NodeShape(IRI id, URI mappedTriplesMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedTriplesMap = Optional.of(mappedTriplesMap);

        propertyShapes = new ArrayList<>();
    }

    public List<IRI> getPropertyShapeIDs() { return propertyShapes; }

    Optional<URI> getMappedTriplesMap() { return mappedTriplesMap; }

    void addPropertyShape(IRI propertyShape) { propertyShapes.add(propertyShape); }

    @Override
    public String toString() {
        String serializedNodeShape = getSerializedShape();
        if (serializedNodeShape != null) return serializedNodeShape;

        StringBuffer buffer = new StringBuffer();

        String id = getShaclDocModel().getRelativeIRIOr(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        buffer.append(getPO(Symbols.A, "sh:NodeShape"));
        buffer.append(getSNT());

        for (IRI propertyShapeIRI: propertyShapes) {
            String p = getShaclDocModel().getRelativeIRIOr(propertyShapeIRI.toString());
            buffer.append(getPO("sh:property", p));
            buffer.append(getSNT());
        }

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        serializedNodeShape = buffer.toString();
        setSerializedShape(serializedNodeShape);
        return serializedNodeShape;
    }
}
