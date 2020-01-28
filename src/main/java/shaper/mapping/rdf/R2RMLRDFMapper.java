package shaper.mapping.rdf;

import java.io.File;

public class R2RMLRDFMapper extends RDFMapper {
    private String R2RMLPathname;

    public R2RMLRDFMapper(String R2RMLPathname) { this.R2RMLPathname = R2RMLPathname; }

    @Override
    public File generateRDFFile() {
        System.out.println("R2RML Mapping is not supported yet.");
        return null;
    }
}
