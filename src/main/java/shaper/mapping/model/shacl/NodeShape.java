package shaper.mapping.model.shacl;

import janus.database.SQLSelectField;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.SubjectMap;
import shaper.mapping.model.r2rml.Template;
import shaper.mapping.model.r2rml.TermMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeShape extends Shape {
    private Optional<URI> mappedTriplesMap = Optional.empty(); // mapped rr:TriplesMap
    private Optional<SubjectMap> subjectMapOfMappedTriplesMap = Optional.empty();

    private List<IRI> propertyShapes;

    NodeShape(IRI id, URI mappedTriplesMap, SubjectMap subjectMapOfMappedTriplesMap, ShaclDocModel shaclDocModel) {
        super(id, shaclDocModel);
        this.mappedTriplesMap = Optional.of(mappedTriplesMap);
        this.subjectMapOfMappedTriplesMap = Optional.of(subjectMapOfMappedTriplesMap);

        propertyShapes = new ArrayList<>();
    }

    public List<IRI> getPropertyShapeIDs() { return propertyShapes; }

    Optional<URI> getMappedTriplesMap() { return mappedTriplesMap; }

    void addPropertyShape(IRI propertyShape) { propertyShapes.add(propertyShape); }

    String buildSerializedNodeShape(SubjectMap subjectMap) {
        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        // sh:nodeKind
        Optional<TermMap.TermTypes> termType = subjectMap.getTermType();
        if (termType.isPresent()) {
            if (termType.get().equals(TermMap.TermTypes.BLANKNODE))
                o = "sh:BlankNode";
        }

        o = "sh:IRI";

        buffer.append(getPO("sh:nodeKind", o));
        buffer.append(getSNT());

        // sh:pattern
        Optional<String> regex = getRegex(subjectMap);
        if (regex.isPresent()) {
            o = regex.get();
            buffer.append(getPO("sh:pattern", o));
            buffer.append(getSNT());
        }

        return buffer.toString();
    }

    @Override
    public String toString() {
        String serializedNodeShape = getSerializedShape();
        if (serializedNodeShape != null) return serializedNodeShape;

        StringBuffer buffer = new StringBuffer();

        String o; // to be used as objects of different RDF triples

        String id = getShaclDocModel().getRelativeIRIOr(getID().toString());
        buffer.append(id);
        buffer.append(getNT());

        // rdf:type
        buffer.append(getPO(Symbols.A, "sh:NodeShape"));
        buffer.append(getSNT());

        // if SubjectMap
        if (subjectMapOfMappedTriplesMap.isPresent())
            buffer.append(buildSerializedNodeShape(subjectMapOfMappedTriplesMap.get()));

        // sh:property
        for (IRI propertyShapeIRI: propertyShapes) {
            o = getShaclDocModel().getRelativeIRIOr(propertyShapeIRI.toString());
            buffer.append(getPO("sh:property", o));
            buffer.append(getSNT());
        }

        buffer.setLength(buffer.lastIndexOf(Symbols.SEMICOLON));
        buffer.append(getDNT());

        serializedNodeShape = buffer.toString();
        setSerializedShape(serializedNodeShape);
        return serializedNodeShape;
    }

    private Optional<String> getRegex(SubjectMap subjectMap) {
        Optional<Template> template = subjectMap.getTemplate();

        if (!isPossibleToHavePattern(template)) return Optional.empty();

        String regex = template.get().getFormat();

        // column names
        List<SQLSelectField> columnNames = template.get().getColumnNames();
        for (SQLSelectField columnName: columnNames)
            regex = regex.replace("{" + columnName.getColumnNameOrAlias() + "}", "(.+)");

        return Optional.of(Symbols.DOUBLE_QUOTATION_MARK + Symbols.CARET + regex + Symbols.DOLLAR + Symbols.DOUBLE_QUOTATION_MARK);
    }
}
