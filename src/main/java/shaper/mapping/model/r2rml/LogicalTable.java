package shaper.mapping.model.r2rml;

import java.net.URI;
import java.util.Optional;

public class LogicalTable {

    private Optional<URI> uri;

    private Optional<String> tableName; // rr:tableName
    private Optional<String> sqlQuery; // rr:sqlQuery
    private Optional<String> sqlVersion; // rr:sqlVersion, which is an IRI

    public LogicalTable() {
        uri = Optional.empty();

        tableName = Optional.empty();
        sqlQuery = Optional.empty();
        sqlVersion = Optional.empty();
    }

    public void setUri(URI uri) {
        this.uri = Optional.ofNullable(uri);
    }

    public void setTableName(String tableName) {
        this.tableName = Optional.ofNullable(tableName);
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = Optional.ofNullable(sqlQuery);
    }

    public void setSqlVersion(String sqlVersion) {
        this.sqlVersion = Optional.ofNullable(sqlVersion);
    }

    public Optional<String> getSqlQuery() { return sqlQuery; }

    public Optional<URI> getUri() { return uri; }
}
