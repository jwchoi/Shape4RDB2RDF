package shaper.mapping.metadata.r2rml;

import java.net.URI;
import java.util.List;

public class SubjectMap extends TermMap {
    private List<URI> classIRIs;

    public SubjectMap(List<URI> classIRIs) {
        this.classIRIs = classIRIs;
    }

    public List<URI> getClassIRIs() { return classIRIs; }
}
