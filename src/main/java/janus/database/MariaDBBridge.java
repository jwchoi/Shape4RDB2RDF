package janus.database;

import java.sql.*;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class MariaDBBridge extends DBBridge {

    private final String regexForXSDDate = "^([1-9][0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";

    private final String defaultRegexForXSDDateTimeFromDateTime = "^([1-9][0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])((.[0-9]{1,6})?)$";

    private final String defaultRegexForXSDDateTimeFromTimeStamp = "(^(19[7-9][0-9]|20([0-2][0-9]|3[0-7]))-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])((.[0-9]{1,6})?)$)|(^2038-01-(0[1-9]|1[0-8])T(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])((.[0-9]{1,6})?)$)|(^2038-01-19T0[0-4]:([0-5][0-9]):([0-5][0-9])((.[0-9]{1,6})?)$)|(^2038-01-19T05:(0[0-9]|1[0-3]):([0-5][0-9])((.[0-9]{1,6})?))|(2038-01-19T05:14:0[0-6]((.[0-9]{1,6})?)$)|(^2038-01-19T05:14:07((.0{1,6})?)$)";

    private final String defaultRegexForXSDTime = "^(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](.[0-9]{1,6})?$";

    private enum IntegerTypes {
        TINYINT_SIGNED_MINIMUM_VALUE("-128"),
        TINYINT_SIGNED_MAXIMUM_VALUE("127"),
        TINYINT_UNSIGNED_MINIMUM_VALUE("0"),
        TINYINT_UNSIGNED_MAXIMUM_VALUE("255"),

        SMALLINT_SIGNED_MINIMUM_VALUE("-32768"),
        SMALLINT_SIGNED_MAXIMUM_VALUE("32767"),
        SMALLINT_UNSIGNED_MINIMUM_VALUE("0"),
        SMALLINT_UNSIGNED_MAXIMUM_VALUE("65535"),

        MEDIUMINT_SIGNED_MINIMUM_VALUE("-8388608"),
        MEDIUMINT_SIGNED_MAXIMUM_VALUE("8388607"),
        MEDIUMINT_UNSIGNED_MINIMUM_VALUE("0"),
        MEDIUMINT_UNSIGNED_MAXIMUM_VALUE("16777215"),

        INT_SIGNED_MINIMUM_VALUE("-2147483648"),
        INT_SIGNED_MAXIMUM_VALUE("2147483647"),
        INT_UNSIGNED_MINIMUM_VALUE("0"),
        INT_UNSIGNED_MAXIMUM_VALUE("4294967295"),

        BIGINT_SIGNED_MINIMUM_VALUE("-9223372036854775808"),
        BIGINT_SIGNED_MAXIMUM_VALUE("9223372036854775807"),
        BIGINT_UNSIGNED_MINIMUM_VALUE("0"),
        BIGINT_UNSIGNED_MAXIMUM_VALUE("18446744073709551615");

        private final String value;

        IntegerTypes(String value) {
            this.value = value;
        }

        @Override
        public String toString() { return value; }
    }

    private Connection informationSchemaConnection;

    private String informationSchemaName = "information_schema";

	private MariaDBBridge(String host, String port, String id, String password, String schema) {
		loadDriver(DBMSTypes.MARIADB.driver());
		
		connection = getConnection(buildURL(host, port, schema), id, password);

        informationSchemaConnection = getConnection(buildURL(host, port, informationSchemaName), id, password);
	}
	
	static MariaDBBridge getInstance(String host, String port, String id, String password, String schema) {
		MariaDBBridge dbBridge = new MariaDBBridge(host, port, id, password, schema);

		return dbBridge.isConnected() ? dbBridge : null;
	}

    @Override
	String buildURL(String host, String port, String schema) {
		return "jdbc:mysql://" + host + ":" + port + "/" + schema;
	}

    @Override
    SQLResultSet executeQueryFromInformationSchema(String query) {
        SQLResultSet SQLRS = null;

        try {
            Statement stmt = informationSchemaConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);

            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            SQLRS = new SQLResultSet(rs, rsmd);
        } catch(SQLException e) { e.printStackTrace(); }

        return SQLRS;
    }

    @Override
    String getRegexForXSDDate() { return regexForXSDDate; }

    @Override
    Optional<String> getRegexForXSDTime(String catalog, String table, String column) {
        String regex = null;

        String SQLDataType = getSQLDataType(catalog, table, column);

        if (SQLDataType.toUpperCase().equals("TIME")) {

            Integer msPrecision = getDateTimePrecision(catalog, table, column).get();

            if (SQLDataType.toUpperCase().equals("TIME")) {
                if (msPrecision == 0)
                    regex = defaultRegexForXSDTime.replace("?", "{0}");
                else if (msPrecision > 0 && msPrecision < 6)
                    regex = defaultRegexForXSDTime.replace("6", msPrecision.toString());
            }
        }

        return Optional.ofNullable(regex);
    }

    @Override
    Optional<String> getRegexForXSDDateTime(String catalog, String table, String column) {
	    String regex = null;

	    String SQLDataType = getSQLDataType(catalog, table, column);

	    if (SQLDataType.toUpperCase().equals("DATETIME") || SQLDataType.toUpperCase().equals("TIMESTAMP")) {

	        Integer msPrecision = getDateTimePrecision(catalog, table, column).get();

            if (SQLDataType.toUpperCase().equals("DATETIME")) {
                if (msPrecision == 0)
                    regex = defaultRegexForXSDDateTimeFromDateTime.replace("?", "{0}");
                else if (msPrecision > 0 && msPrecision < 6)
                    regex = defaultRegexForXSDDateTimeFromDateTime.replace("6", msPrecision.toString());
            } else if (SQLDataType.toUpperCase().equals("TIMESTAMP")) {
                if (msPrecision == 0)
                    regex = defaultRegexForXSDDateTimeFromTimeStamp.replace("?", "{0}");
                else if (msPrecision > 0 && msPrecision < 6)
                    regex = defaultRegexForXSDDateTimeFromTimeStamp.replace("6}", msPrecision + "}");
            }
        }

        return Optional.ofNullable(regex);
    }

    private Optional<Integer> getDateTimePrecision(String catalog, String table, String column) {
        Integer dateTimePrecision = null;

        String query = "SELECT DATETIME_PRECISION " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        try {
            dateTimePrecision = Integer.valueOf(rowData.get(0));
        } catch (NumberFormatException e) {}

        return Optional.ofNullable(dateTimePrecision);
    }

    @Override
    Set<String> getReferentialConstraints(String catalog, String table) {
		Set<String> referentialConstraints = new CopyOnWriteArraySet<>();

        String query = "SELECT CONSTRAINT_NAME " +
                "FROM REFERENTIAL_CONSTRAINTS " +
                "WHERE CONSTRAINT_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        int rowCount = resultSet.getResultSetRowCount();
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            List<String> rowData = resultSet.getResultSetRowAt(rowIndex);
            String constraintName = rowData.get(0);
            referentialConstraints.add(constraintName);
        }

        return referentialConstraints;
	}

    @Override
    Set<String> getReferencingColumns(String catalog, String table, String refConstraint) {
        Set<String> referencingColumns = new CopyOnWriteArraySet<>();

        String query = "SELECT COLUMN_NAME " +
                "FROM KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + refConstraint + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        int rowCount = resultSet.getResultSetRowCount();
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            List<String> rowData = resultSet.getResultSetRowAt(rowIndex);
            String columnName = rowData.get(0);
            referencingColumns.add(columnName);
        }

        return referencingColumns;
    }

    @Override
    short getOrdinalPositionInTheRefConstraint(String catalog, String table, String refConstraint, String column) {
        String query = "SELECT ORDINAL_POSITION " +
                "FROM KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + refConstraint + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);
        String ordinalPosition = rowData.get(0);

        return Short.valueOf(ordinalPosition);
    }

    @Override
    String getReferencedTable(String catalog, String table, String refConstraint) {
        String query = "SELECT REFERENCED_TABLE_NAME " +
                "FROM REFERENTIAL_CONSTRAINTS " +
                "WHERE CONSTRAINT_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + refConstraint + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        return rowData.get(0);
    }

    @Override
    String getColumnReferencedBy(String catalog, String table, String refConstraint, String column) {
        String query = "SELECT REFERENCED_COLUMN_NAME " +
                "FROM KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + refConstraint + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        return rowData.get(0);
    }

    @Override
    Set<String> getUniqueConstraints(String catalog, String table) {
        Set<String> uniqueConstraints = new CopyOnWriteArraySet<>();

        String query = "SELECT CONSTRAINT_NAME " +
                "FROM TABLE_CONSTRAINTS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_TYPE" + " = " + "'" + "UNIQUE" + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        int rowCount = resultSet.getResultSetRowCount();
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            List<String> rowData = resultSet.getResultSetRowAt(rowIndex);
            String constraintName = rowData.get(0);
            uniqueConstraints.add(constraintName);
        }

        return uniqueConstraints;
    }

    @Override
    Set<String> getUniqueConstraintColumns(String catalog, String table, String uniqueConstraint) {
        Set<String> uniqueConstraintColumns = new CopyOnWriteArraySet<>();

        String query = "SELECT COLUMN_NAME " +
                "FROM KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + uniqueConstraint + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        int rowCount = resultSet.getResultSetRowCount();
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            List<String> rowData = resultSet.getResultSetRowAt(rowIndex);
            String columnName = rowData.get(0);
            uniqueConstraintColumns.add(columnName);
        }

        return uniqueConstraintColumns;
    }

    @Override
    short getOrdinalPositionInTheUniqueConstraint(String catalog, String table, String uniqueConstraint, String column) {
        String query = "SELECT ORDINAL_POSITION " +
                "FROM KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "CONSTRAINT_NAME" + " = " + "'" + uniqueConstraint + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);
        String ordinalPosition = rowData.get(0);

        return Short.valueOf(ordinalPosition);
    }

	@Override
	String getColumnType(String catalog, String table, String column) {
        String query = "SELECT COLUMN_TYPE " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        return rowData.get(0);
	}

	@Override
    String getSQLDataType(String catalog, String table, String column) {
        String query = "SELECT DATA_TYPE " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        return rowData.get(0);
    }

    @Override
    Optional<Integer> getCharacterMaximumLength(String catalog, String table, String column) {
        Integer characterMaximumLength = null;

        String query = "SELECT CHARACTER_MAXIMUM_LENGTH " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        try {
            characterMaximumLength = Integer.valueOf(rowData.get(0));
        } catch (NumberFormatException e) {}

        return Optional.ofNullable(characterMaximumLength);
    }

    @Override
    Optional<Integer> getCharacterOctetLength(String catalog, String table, String column) {
        Integer characterOctetLength = null;

        String query = "SELECT CHARACTER_OCTET_LENGTH " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        try {
            characterOctetLength = Integer.valueOf(rowData.get(0));
        } catch (NumberFormatException e) {}

        return Optional.ofNullable(characterOctetLength);
    }

    @Override
    public Optional<String> getMaximumIntegerValue(String catalog, String table, String column) {
        String maximumIntegerValue = null;

	    String SQLDataType = getSQLDataType(catalog, table, column).toUpperCase();
	    if (isUnsigned(catalog, table, column)) {
	        switch (SQLDataType) {
                case "TINYINT": maximumIntegerValue = IntegerTypes.TINYINT_UNSIGNED_MAXIMUM_VALUE.toString(); break;
                case "SMALLINT": maximumIntegerValue = IntegerTypes.SMALLINT_UNSIGNED_MAXIMUM_VALUE.toString(); break;
                case "MEDIUMINT": maximumIntegerValue = IntegerTypes.MEDIUMINT_UNSIGNED_MAXIMUM_VALUE.toString(); break;
                case "INT": maximumIntegerValue = IntegerTypes.INT_UNSIGNED_MAXIMUM_VALUE.toString(); break;
                case "BIGINT": maximumIntegerValue = IntegerTypes.BIGINT_UNSIGNED_MAXIMUM_VALUE.toString(); break;
            }
        } else {
            switch (SQLDataType) {
                case "TINYINT": maximumIntegerValue = IntegerTypes.TINYINT_SIGNED_MAXIMUM_VALUE.toString(); break;
                case "SMALLINT": maximumIntegerValue = IntegerTypes.SMALLINT_SIGNED_MAXIMUM_VALUE.toString(); break;
                case "MEDIUMINT": maximumIntegerValue = IntegerTypes.MEDIUMINT_SIGNED_MAXIMUM_VALUE.toString(); break;
                case "INT": maximumIntegerValue = IntegerTypes.INT_SIGNED_MAXIMUM_VALUE.toString(); break;
                case "BIGINT": maximumIntegerValue = IntegerTypes.BIGINT_SIGNED_MAXIMUM_VALUE.toString(); break;
            }
        }

        return Optional.ofNullable(maximumIntegerValue);
    }

    @Override
    public Optional<String> getMinimumIntegerValue(String catalog, String table, String column) {
        String minimumIntegerValue = null;

        String SQLDataType = getSQLDataType(catalog, table, column).toUpperCase();
        if (isUnsigned(catalog, table, column)) {
            switch (SQLDataType) {
                case "TINYINT": minimumIntegerValue = IntegerTypes.TINYINT_UNSIGNED_MINIMUM_VALUE.toString(); break;
                case "SMALLINT": minimumIntegerValue = IntegerTypes.SMALLINT_UNSIGNED_MINIMUM_VALUE.toString(); break;
                case "MEDIUMINT": minimumIntegerValue = IntegerTypes.MEDIUMINT_UNSIGNED_MINIMUM_VALUE.toString(); break;
                case "INT": minimumIntegerValue = IntegerTypes.INT_UNSIGNED_MINIMUM_VALUE.toString(); break;
                case "BIGINT": minimumIntegerValue = IntegerTypes.BIGINT_UNSIGNED_MINIMUM_VALUE.toString(); break;
            }
        } else {
            switch (SQLDataType) {
                case "TINYINT": minimumIntegerValue = IntegerTypes.TINYINT_SIGNED_MINIMUM_VALUE.toString(); break;
                case "SMALLINT": minimumIntegerValue = IntegerTypes.SMALLINT_SIGNED_MINIMUM_VALUE.toString(); break;
                case "MEDIUMINT": minimumIntegerValue = IntegerTypes.MEDIUMINT_SIGNED_MINIMUM_VALUE.toString(); break;
                case "INT": minimumIntegerValue = IntegerTypes.INT_SIGNED_MINIMUM_VALUE.toString(); break;
                case "BIGINT": minimumIntegerValue = IntegerTypes.BIGINT_SIGNED_MINIMUM_VALUE.toString(); break;
            }
        }

        return Optional.ofNullable(minimumIntegerValue);
    }

    @Override
    Optional<Integer> getNumericPrecision(String catalog, String table, String column) {
	    Integer numericPrecision = null;

        String query = "SELECT NUMERIC_PRECISION " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        try {
            numericPrecision = Integer.valueOf(rowData.get(0));
        } catch (NumberFormatException e) {}

        return Optional.ofNullable(numericPrecision);
    }

    @Override
    Optional<Integer> getNumericScale(String catalog, String table, String column) {
	    Integer numericScale = null;

        String query = "SELECT NUMERIC_SCALE " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        try {
            numericScale = Integer.valueOf(rowData.get(0));
        } catch (NumberFormatException e) {}

        return Optional.ofNullable(numericScale);
    }

    @Override
    Optional<Set<String>> getValueSet(String catalog, String table, String column) {
	    String columnType = getColumnType(catalog, table, column);

	    if (columnType.toUpperCase().startsWith("ENUM") ||
                columnType.toUpperCase().startsWith("SET")) {

	        Set<String> valueSet = new CopyOnWriteArraySet<>();

	        String set = columnType.substring(columnType.indexOf('(')+1, columnType.lastIndexOf(')'));
            StringTokenizer st = new StringTokenizer(set, ",");

            while (st.hasMoreTokens())
                valueSet.add(st.nextToken().trim());

            return Optional.of(valueSet);
        } else
            return Optional.empty();
    }

    @Override
    boolean isUnsigned(String catalog, String table, String column) {
	    boolean isUnsigned = false;

        String columnType = getColumnType(catalog, table, column);

        int firstSpace = columnType.indexOf(" ");
        int lastCloseParenthesis = columnType.lastIndexOf(")");
        int beginIndex = Integer.max(firstSpace, lastCloseParenthesis) + 1;

        if (beginIndex > -1 && beginIndex < columnType.length()) {
            String substring = columnType.substring(beginIndex);
            if (substring.toUpperCase().contains("UNSIGNED"))
                isUnsigned = true;
        }

        return isUnsigned;
    }

	@Override
	String getDefaultValue(String catalog, String table, String column) {
        String query = "SELECT COLUMN_DEFAULT " +
                "FROM COLUMNS " +
                "WHERE TABLE_SCHEMA" + " = " + "'" + catalog + "'" +
                " AND " + "TABLE_NAME" + " = " + "'" + table + "'" +
                " AND " + "COLUMN_NAME" + " = " + "'" + column + "'";

        SQLResultSet resultSet = executeQueryFromInformationSchema(query);

        List<String> rowData = resultSet.getResultSetRowAt(1);

        return rowData.get(0);
	}

	@Override
    int getJDBCDataType(String catalog, String table, String column) {
	    int JDBCDataType = super.getJDBCDataType(catalog, table, column);

	    if (JDBCDataType == Types.TINYINT && getNumericPrecision(catalog, table, column).get() == 1)
	        JDBCDataType = Types.BOOLEAN;

	    return JDBCDataType;
    }
}