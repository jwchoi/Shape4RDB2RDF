package shaper.mapping;

import shaper.Shaper;
import janus.database.DBField;
import janus.database.SQLResultSet;
import shaper.mapping.metadata.rdf.RDFMappingMetadataFactory;
import shaper.mapping.metadata.shex.ShExSchemaFactory;

import java.io.File;
import java.net.URI;
import java.util.*;

public class DirectMappingBasedRDFMapper extends RDFMapper {

	public DirectMappingBasedRDFMapper() {
		rdfMappingMD = RDFMappingMetadataFactory.generateMappingMetaData();
	}

	private void writeDirectives(Extension extension) {
		try {
			switch (extension) {
				case Turtle:
					// base
					writer.println(AT + base + SPACE + LT + Shaper.baseURI + GT + SPACE + DOT);

					// prefixID
					writer.println(AT + prefix + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns")) + COLON + SPACE + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + SPACE + DOT);
					writer.println(AT + prefix + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2001/XMLSchema")) + COLON + SPACE + "<http://www.w3.org/2001/XMLSchema#>" + SPACE + DOT);
					writer.println(AT + prefix + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2000/01/rdf-schema")) + COLON + SPACE + "<http://www.w3.org/2000/01/rdf-schema#>" + SPACE + DOT);
					writer.println();
					break;
				case ShEx:
					// base
					writer.println(BASE + SPACE + LT + shExSchema.getBaseIRI() + GT); // for an RDF Graph by direct mapping

				    // prefix for newly created shape expressions
					writer.println(PREFIX + SPACE + shExSchema.getPrefix() + COLON + SPACE + LT + shExSchema.getBaseIRI() + HASH + GT);

					// prefixID
					writer.println(PREFIX + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns")) + COLON + SPACE + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
					writer.println(PREFIX + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2001/XMLSchema")) + COLON + SPACE + "<http://www.w3.org/2001/XMLSchema#>");
					writer.println(PREFIX + SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2000/01/rdf-schema")) + COLON + SPACE + "<http://www.w3.org/2000/01/rdf-schema#>");
					writer.println();
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeShEx() {
		Set<String> tables = Shaper.localDBSchema.getTableNames();
		
		for(String table : tables) {
			List<String> columns = Shaper.localDBSchema.getColumns(table);
			for (String column: columns) {
				String id = shExSchema.getPrefix() + COLON + shExSchema.getMappedNodeConstraintID(table, column);
				writer.println(id + SPACE + shExSchema.getMappedNodeConstraint(table, column));
			}
			writer.println();

			writer.println(shExSchema.getMappedShape(table));
			writer.println();
		}
	}
	
	private void postProcess() {
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeRDF() {
		Set<String> tableNames = Shaper.localDBSchema.getTableNames();
		
		for (String tableName : tableNames) {
			String tableIRI = rdfMappingMD.getMappedTableIRI(tableName);
			
			String query = "SELECT * FROM " + tableName;
			SQLResultSet resultSet = Shaper.dbBridge.executeQuery(query);
			
			int columnCount = resultSet.getResultSetColumnCount();
			
			List<String> columnNames = new Vector<>(columnCount);
			for(int column = 1; column <= columnCount; column++) {
				String columnName = resultSet.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> primaryKeys = Shaper.localDBSchema.getPrimaryKey(tableName);

			int rowCount = resultSet.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = resultSet.getResultSetRowAt(rowIndex);

				StringBuffer buffer = new StringBuffer();

				String subject;

				if (!primaryKeys.isEmpty()) {
                    List<DBField> pkFields = new Vector<>();
                    for (String pk : primaryKeys) {
                        int pkIndex = columnNames.indexOf(pk);
                        String pkData = rowData.get(pkIndex);
                        DBField pkField = new DBField(tableName, pk, pkData);

                        pkFields.add(pkField);
                    }

                    subject = LT + rdfMappingMD.getMappedRowNode(tableName, pkFields) + GT;
                } else {
                    List<DBField> fields = new Vector<>();
                    for (String columnName : columnNames) {
                        int index = columnNames.indexOf(columnName);
                        String fieldData = rowData.get(index);
                        DBField field = new DBField(tableName, columnName, fieldData);

                        fields.add(field);
                    }

                    subject = rdfMappingMD.getMappedBlankNode(tableName, fields);
                }

                buffer.append(subject + NEWLINE);
				buffer.append(TAB + A + SPACE + LT + tableIRI + GT + SPACE + SEMICOLON + NEWLINE);

				for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
					String cellData = rowData.get(columnIndex);
					
					if (cellData == null) continue;
					
					String columnName = columnNames.get(columnIndex);
					String literalProperty = rdfMappingMD.getMappedLiteralProperty(tableName, columnName);

					String literal = rdfMappingMD.getMappedLiteral(tableName, columnName, cellData);

					buffer.append(TAB + LT + literalProperty + GT + SPACE + literal + SPACE + SEMICOLON + NEWLINE);
				}

				int lastSemicolon = buffer.lastIndexOf(SEMICOLON);
				buffer.replace(lastSemicolon, lastSemicolon+1, DOT);
				writer.println(buffer);
			}
		}

		for (String tableName : tableNames) {

			String referencingTable = tableName;

			List<String> pkOfReferencingTable = Shaper.localDBSchema.getPrimaryKey(referencingTable);
			List<String> columnsOfReferencingTable = Shaper.localDBSchema.getColumns(referencingTable);

			Set<String> refConstraints = Shaper.localDBSchema.getRefConstraints(tableName);

			for (String refConstraint: refConstraints) {

				String referenceProperty = rdfMappingMD.getMappedReferenceProperty(referencingTable, refConstraint);

				String referencedTable = Shaper.localDBSchema.getReferencedTableBy(referencingTable, refConstraint);
				List<String> pkOfReferencedTable = Shaper.localDBSchema.getPrimaryKey(referencedTable);
				List<String> columnsOfReferencedTable = Shaper.localDBSchema.getColumns(referencedTable);

				NaturalJoinQuery query = getNaturalJoinQueryFor(tableName, refConstraint);
				SQLResultSet resultSet = Shaper.dbBridge.executeQuery(query.toString());

				int rowCount = resultSet.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = resultSet.getResultSetRowAt(rowIndex);

					String subject;

					if (!pkOfReferencingTable.isEmpty()) {
						List<DBField> pkFields = new Vector<>();
						for (String pk : pkOfReferencingTable) {
							int pkIndex = query.getSelectColumnIndexOf(referencingTable, query.getAliasOfReferencingTable(), pk);
							String pkData = rowData.get(pkIndex);
							DBField pkField = new DBField(referencingTable, pk, pkData);

							pkFields.add(pkField);
						}


						subject = LT + rdfMappingMD.getMappedRowNode(referencingTable, pkFields) + GT;
					} else {
						List<DBField> fields = new Vector<>();
						for (String col : columnsOfReferencingTable) {
							int index = query.getSelectColumnIndexOf(referencingTable, query.getAliasOfReferencingTable(), col);
							String fieldData = rowData.get(index);
							DBField field = new DBField(referencingTable, col, fieldData);

							fields.add(field);
						}

						subject = rdfMappingMD.getMappedBlankNode(referencingTable, fields);
					}

					String object;

					if (!pkOfReferencedTable.isEmpty()) {
						List<DBField> pkFields = new Vector<>();
						for (String pk : pkOfReferencedTable) {
							int pkIndex = query.getSelectColumnIndexOf(referencedTable, query.getAliasOfReferencedTable(), pk);
							String pkData = rowData.get(pkIndex);
							DBField pkField = new DBField(referencedTable, pk, pkData);

							pkFields.add(pkField);
						}

						object = LT + rdfMappingMD.getMappedRowNode(referencedTable, pkFields) + GT;
					} else {
						List<DBField> fields = new Vector<>();
						for (String col : columnsOfReferencedTable) {
							int index = query.getSelectColumnIndexOf(referencedTable, query.getAliasOfReferencedTable(), col);
							String fieldData = rowData.get(index);
							DBField field = new DBField(referencedTable, col, fieldData);

							fields.add(field);
						}

						object = rdfMappingMD.getMappedBlankNode(referencedTable, fields);
					}

					writer.println(subject + SPACE + LT + referenceProperty + GT + SPACE + object + SPACE + DOT);
				}
			}
		}
	}

	public File generateShExFile() {
		shExSchema = ShExSchemaFactory.buildShExSchemaModel();

		preProcess(Extension.ShEx);
		writeDirectives(Extension.ShEx);
		writeShEx();
		postProcess();
		
		System.out.println("Translating the schema into ShEx has finished.");
		
		return output;
	}
	
	public File generateRDFFile() {
		preProcess(Extension.Turtle);
		writeDirectives(Extension.Turtle);
		writeRDF();
		postProcess();
		
		System.out.println("The Direct Mapping has finished.");
		
		return output;
	}

	private NaturalJoinQuery getNaturalJoinQueryFor(String tableName, String refConstraint) {
		String referencingTable = tableName;
		String aliasOfReferencingTable = "T1";

		String referencedTable = Shaper.localDBSchema.getReferencedTableBy(referencingTable, refConstraint);
		String aliasOfReferencedTable = "T2";

		NaturalJoinQuery naturalJoinQuery = new NaturalJoinQuery(referencingTable, aliasOfReferencingTable, referencedTable, aliasOfReferencedTable);

		StringBuffer query = new StringBuffer("SELECT ");

		List<String> pkOfReferencingTable = Shaper.localDBSchema.getPrimaryKey(referencingTable);
		if (!pkOfReferencingTable.isEmpty()) {
			for (String column: pkOfReferencingTable) {
				naturalJoinQuery.addSQLSelectColumn(referencingTable, aliasOfReferencingTable, column);
				query.append(aliasOfReferencingTable + "." + column);
				query.append(", ");
			}
		} else {
			List<String> columns = Shaper.localDBSchema.getColumns(referencingTable);
			for (String column: columns) {
				naturalJoinQuery.addSQLSelectColumn(referencingTable, aliasOfReferencingTable, column);
				query.append(aliasOfReferencingTable + "." + column);
				query.append(", ");
			}
		}

		List<String> pkOfReferencedTable = Shaper.localDBSchema.getPrimaryKey(referencedTable);
		if (!pkOfReferencedTable.isEmpty()) {
			for (String column: pkOfReferencedTable) {
				naturalJoinQuery.addSQLSelectColumn(referencedTable, aliasOfReferencedTable, column);
				query.append(aliasOfReferencedTable + "." + column);
				query.append(", ");
			}
		} else {
			List<String> columns = Shaper.localDBSchema.getColumns(referencedTable);
			for (String column: columns) {
				naturalJoinQuery.addSQLSelectColumn(referencedTable, aliasOfReferencedTable, column);
				query.append(aliasOfReferencedTable + "." + column);
				query.append(", ");
			}
		}

		query.deleteCharAt(query.lastIndexOf(","));

		query.append("FROM " + referencingTable + " AS " + aliasOfReferencingTable + ", " + referencedTable + " AS " + aliasOfReferencedTable);
		query.append(" WHERE ");

		List<String> referencingColumns = Shaper.localDBSchema.getReferencingColumnsByOrdinalPosition(referencingTable, refConstraint);
		for (String referencingColumn: referencingColumns) {
			String referencedColumn = Shaper.localDBSchema.getReferencedColumnBy(referencingTable, refConstraint, referencingColumn);
			query.append(aliasOfReferencingTable + "." + referencingColumn + " = " + aliasOfReferencedTable + "." + referencedColumn + " AND ");
		}

		query.delete(query.lastIndexOf(" AND "), query.length());

		naturalJoinQuery.setQuery(query.toString());

		return naturalJoinQuery;
    }

    private class NaturalJoinQuery {

		class SQLSelectColumn {
			private String tableName;
			private String columnName;

			private String aliasOfTableName;

			SQLSelectColumn(String tableName, String columnName, String aliasOfTableName) {
				this.tableName = tableName;
				this.columnName = columnName;
				this.aliasOfTableName = aliasOfTableName;
			}

			String getTableName() {
				return tableName;
			}

			String getColumnName() {
				return columnName;
			}

			String getAliasOfTableName() {
				return aliasOfTableName;
			}
		}

		private String query;

		private String referencingTable;
		private String referencedTable;

		private String aliasOfReferencingTable;
		private String aliasOfReferencedTable;

		private List<SQLSelectColumn> selectColumns;

		NaturalJoinQuery(String referencingTable, String aliasOfReferencingTable, String referencedTable, String aliasOfReferencedTable) {
			this.referencingTable = referencingTable;
			this.aliasOfReferencingTable = aliasOfReferencingTable;

			this.referencedTable = referencedTable;
			this.aliasOfReferencedTable = aliasOfReferencedTable;

			selectColumns = new ArrayList<>();
		}

		void addSQLSelectColumn(String tableName, String aliasOfTableName, String columnName) {
			selectColumns.add(new SQLSelectColumn(tableName, columnName, aliasOfTableName));
		}

		int getSelectColumnIndexOf(String tableName, String aliasOfTableName, String columnName) {
			for (int i = 0; i < selectColumns.size(); i++) {
				SQLSelectColumn selectColumn = selectColumns.get(i);
				if (selectColumn.getTableName().equals(tableName)
						&& selectColumn.getAliasOfTableName().equals(aliasOfTableName)
						&& selectColumn.getColumnName().equals(columnName))
					return i;
			}

			return -1;
		}

		void setQuery(String query) { this.query = query; }

		@Override
		public String toString() {
			return query;
		}

		String getAliasOfReferencingTable() {
			return aliasOfReferencingTable;
		}

		String getAliasOfReferencedTable() {
			return aliasOfReferencedTable;
		}
	}
}
