package shaper.mapping.metadata.r2rml;

import janus.database.SQLSelectField;

public class ObjectMap extends TermMap {
    // column or template
    public ObjectMap(SQLSelectField column) {
        setColumn(column);
    }

    public ObjectMap(Template template) {
        setTemplate(template);
    }
}
