package shaper.mapping.model.rdf;

import shaper.mapping.Symbols;

public class Utils {
    static String encode(String value) {
        return value.replace(Symbols.SPACE, "%20");
    }
}
