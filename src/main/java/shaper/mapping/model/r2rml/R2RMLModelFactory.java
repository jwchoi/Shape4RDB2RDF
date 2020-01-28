package shaper.mapping.model.r2rml;

import janus.database.SQLResultSet;
import janus.database.SQLSelectField;
import shaper.Shaper;
import shaper.mapping.r2rml.R2RMLParser;

import java.net.URI;
import java.util.*;

public class R2RMLModelFactory {

    public static R2RMLModel getR2RMLModel(R2RMLParser parser) {
        R2RMLModel r2rmlModel = new R2RMLModel();

        // prefixes
        Map<String, String> prefixMap = parser.getPrefixes();
        Set<String> keySet = prefixMap.keySet();
        for (String key: keySet)
            r2rmlModel.addPrefixMap(key, prefixMap.get(key));

        // logical tables
        Set<URI> logicalTablesWithURI = parser.getLogicalTables();
        for (URI logicalTableAsIRI: logicalTablesWithURI) {
            LogicalTable logicalTable = new LogicalTable();

            logicalTable.setUri(logicalTableAsIRI);
            logicalTable.setSqlQuery(parser.getSQLQuery(logicalTableAsIRI.toString()));
            logicalTable.setTableName(parser.getTableName(logicalTableAsIRI.toString()));

            Set<URI> sqlVersions = parser.getSQLVersions(logicalTableAsIRI.toString());
            for (URI sqlVersion: sqlVersions)
                logicalTable.addSqlVersion(sqlVersion);

            r2rmlModel.addLogicalTable(logicalTable);
        }

        // triples maps
        Set<String> triplesMaps = parser.getTriplesMaps();
        for (String triplesMapAsResource: triplesMaps) {

            // logical table
            String logicalTableAsResource = parser.getLogicalTable(triplesMapAsResource);

            LogicalTable logicalTable;

            if (R2RMLParser.isURI(logicalTableAsResource))
                logicalTable = r2rmlModel.getLogicalTableBy(URI.create(logicalTableAsResource));
            else {
                // when logical table is a blank node
                logicalTable = new LogicalTable();
                logicalTable.setSqlQuery(parser.getSQLQuery(logicalTableAsResource));
                logicalTable.setTableName(parser.getTableName(logicalTableAsResource));

                Set<URI> sqlVersions = parser.getSQLVersions(logicalTableAsResource);
                for (URI sqlVersion: sqlVersions)
                    logicalTable.addSqlVersion(sqlVersion);
            }

            // subject map
            String subjectMapAsResource = parser.getSubjectMap(triplesMapAsResource);

            Set<URI> classes = parser.getClasses(subjectMapAsResource); // the size of classes could be zero.

            SubjectMap subjectMap = new SubjectMap(classes);

            subjectMap.setConstant(parser.getIRIConstant(subjectMapAsResource).toString()); // IRI in SubjectMap

            String query = logicalTable.getSqlQuery();
            SQLResultSet resultSet = Shaper.dbBridge.executeQuery(query);

            String column = parser.getColumn(subjectMapAsResource);
            if (column != null)
                subjectMap.setColumn(createSQLSelectField(column, query, resultSet));

            String template = parser.getTemplate(subjectMapAsResource);
            if (template != null) {
                List<SQLSelectField> columnNames = createSQLSelectFields(getColumnNamesIn(template), query, resultSet);
                boolean isIRIFormat = isValidIRITemplate(template);
                subjectMap.setTemplate(new Template(template, columnNames, isIRIFormat));
            }






            System.out.println("term type: " + parser.getTermType(subjectMapAsResource));
            System.out.println("inverse expression: " + parser.getInverseExpression(subjectMapAsResource));

            // predicate object map
            Set<String> predicateObjectMaps = parser.getPredicateObjectMaps(triplesMapAsResource);
            for (String predicateObjectMap: predicateObjectMaps) {
                System.out.println("predicate object map: " + predicateObjectMap);

                // predicate or predicate map
                URI predicate = parser.getPredicate(predicateObjectMap);
                if (predicate == null) {
                    String predicateMap = parser.getPredicateMap(predicateObjectMap);
                    predicate = parser.getIRIConstant(predicateMap);
                }
                System.out.println("predicate: " + predicate);

                // object or object map
                URI IRIObject = parser.getIRIObject(predicateObjectMap);
                System.out.println("object: " + IRIObject + ", which is an IRI");
                String literalObject = parser.getLiteralObject(predicateObjectMap);
                System.out.println("object: " + literalObject + ", which is a literal");
                if (IRIObject == null && literalObject == null) {
                    String objectMap = parser.getObjectMap(predicateObjectMap);
                    System.out.println(objectMap);

                    URI IRIConstant = parser.getIRIConstant(objectMap);
                    System.out.println("constant: " + IRIConstant + ", which is an IRI");
                    String literalConstant = parser.getLiteralConstant(objectMap);
                    System.out.println("constant: " + literalConstant + ", which is a literal");

                    System.out.println("column: " + parser.getColumn(objectMap));
                    System.out.println("template: " + parser.getTemplate(objectMap));
                    System.out.println("term type: " + parser.getTermType(objectMap));
                    System.out.println("inverse expression: " + parser.getInverseExpression(objectMap));
                    System.out.println("language tag: " + parser.getLanguage(objectMap));
                    System.out.println("datatype: " + parser.getDatatype(objectMap));

                    // referencing object map
                    String parentTriplesMap = parser.getParentTriplesMap(objectMap);
                    System.out.println("parent triples map: " + parentTriplesMap);
                    if (parentTriplesMap != null) {
                        Set<String> joinConditions = parser.getJoinConditions(objectMap);
                        for (String joinCondition: joinConditions) {
                            System.out.println("child: " + parser.getChild(joinCondition));
                            System.out.println("parent: " + parser.getParent(joinCondition));
                        }
                    }
                }
            }
        }

        // triples maps
        List<LogicalTableMapping> logicalTableMappings = mappingDocument.getLogicalTableMappings();
        for (LogicalTableMapping logicalTableMapping: logicalTableMappings) {
            // uri
            String uriOfTriplesMap = logicalTableMapping.getUri();

            // logical table
            LogicalTableView logicalTableView = logicalTableMapping.getView();
            String uriOfLogicalTable = logicalTableView.getUri();

            LogicalTable logicalTable = new LogicalTable();
            if (uriOfLogicalTable != null)
                logicalTable.setUri(URI.create(uriOfLogicalTable));

            SelectQuery selectQuery = logicalTableView.getSelectQuery();
            if (selectQuery != null)
                logicalTable.setSqlQuery(selectQuery.getQuery());

            // subject map
            gr.seab.r2rml.entities.SubjectMap subjectMap = logicalTableMapping.getSubjectMap();

            List<String> classUris = subjectMap.getClassUris();
            List<URI> classIRIs = new ArrayList<>();

            for (String classUri: classUris)
                classIRIs.add(URI.create(classUri));

            SubjectMap sMap = new SubjectMap(classIRIs);

            gr.seab.r2rml.entities.Template template = subjectMap.getTemplate();

            // rr:termType
            TermType termType = template.getTermType();
            if (termType.equals(TermType.BLANKNODE))
                sMap.setTermType(TermMap.TermTypes.BLANKNODE);
            else
                sMap.setTermType(TermMap.TermTypes.IRI);

            /*// rr:constant: Not Supported in Parser
            List<String> fields = template.getFields();
            if (template.isUri() && (termType.equals(TermType.IRI) || fields.size() < 1))
                sMap.setConstant(template.getText());

            // rr:column: handle as template case in parser
            String text = template.getText();
            if (fields.size() == 1) {
                if (text.replace("{" + fields.get(0) + "}", "").length() < 1)
                    sMap.setColumn(fields.get(0));
            }*/

            // execute sql query
            String query = selectQuery != null ? selectQuery.getQuery() : mappingDocument.findLogicalTableViewByUri(uriOfLogicalTable).getSelectQuery().getQuery();
            SQLResultSet sqlResultSet = Shaper.dbBridge.executeQuery(query);

            // rr:template
            sMap.setTemplate(new Template(template.getText(), createSQLSelectFields(template.getFields(), query, sqlResultSet), template.isUri()));

            TriplesMap triplesMap = new TriplesMap(URI.create(uriOfTriplesMap), logicalTable, sMap);

            // rr:predicateObjectMap
            List<gr.seab.r2rml.entities.PredicateObjectMap> predicateObjectMaps = logicalTableMapping.getPredicateObjectMaps();
            for (gr.seab.r2rml.entities.PredicateObjectMap predicateObjectMap: predicateObjectMaps) {
                // rr:predicateMap
                PredicateMap pMap = new PredicateMap(predicateObjectMap.getPredicates().get(0));

                // rr:objectMap
                String objectColumn = predicateObjectMap.getObjectColumn();
                gr.seab.r2rml.entities.Template objectTemplate = predicateObjectMap.getObjectTemplate();
                gr.seab.r2rml.entities.RefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap();
                if (refObjectMap != null) {
                    // rr:parentTriplesMap
                    String parentTriplesMapUri = refObjectMap.getParentTriplesMapUri();

                    RefObjectMap referencingObjectMap = new RefObjectMap(URI.create(parentTriplesMapUri));

                    // rr:child & rr:parent
                    String child = refObjectMap.getChild();
                    String parent = refObjectMap.getParent();
                    if (child != null && parent != null)
                        referencingObjectMap.addJoinCondition(child, parent);

                    triplesMap.addPredicateObjectMap(new PredicateObjectMap(pMap, referencingObjectMap));
                } else {
                    ObjectMap objectMap;

                    if (objectColumn != null)
                        objectMap = new ObjectMap(createSQLSelectField(objectColumn, query, sqlResultSet)); // rr:column
                    else
                        objectMap = new ObjectMap(new Template(objectTemplate.getText(), createSQLSelectFields(objectTemplate.getFields(), query, sqlResultSet), objectTemplate.isUri())); // rr:template

                    // rr:termType
                    switch (objectTemplate.getTermType()) {
                        case IRI: objectMap.setTermType(TermMap.TermTypes.IRI);
                        case BLANKNODE: objectMap.setTermType(TermMap.TermTypes.BLANKNODE);
                        case LITERAL: objectMap.setTermType(TermMap.TermTypes.LITERAL);
                        case AUTO: break;
                    }

                    // rr:language
                    String language = objectTemplate.getLanguage();
                    if (language != null && language.length() > 0)
                        objectMap.setLanguage(language);

                    // rr:datatype
                    BaseDatatype dataType = predicateObjectMap.getDataType();
                    if (dataType != null)
                        objectMap.setDatatype(dataType.getURI());

                    triplesMap.addPredicateObjectMap(new PredicateObjectMap(pMap, objectMap));
                }
            }

            r2rmlModel.addTriplesMap(triplesMap);
        }

        return r2rmlModel;
    }

    private static List<SQLSelectField> createSQLSelectFields(List<String> selectFields, String selectQuery, SQLResultSet sqlResultSet) {
        List<SQLSelectField> selectFieldList = new ArrayList<>();
        for (String selectField: selectFields)
            selectFieldList.add(createSQLSelectField(selectField, selectQuery, sqlResultSet));

        return selectFieldList;
    }

    private static SQLSelectField createSQLSelectField(String selectField, String selectQuery, SQLResultSet sqlResultSet) {
        SQLSelectField sqlSelectField = new SQLSelectField(selectField, selectQuery);

        if (selectField.startsWith("\"") && selectField.endsWith("\""))
            selectField = selectField.substring(1, selectField.length()-1);

        // nullable
        Optional<Integer> nullable =  sqlResultSet.isNullable(selectField);
        if (nullable.isPresent())
            sqlSelectField.setNullable(nullable.get());

        // sql type
        Optional<Integer> columnType = sqlResultSet.getColumnType(selectField);
        if (columnType.isPresent())
            sqlSelectField.setSqlType(columnType.get());

        return sqlSelectField;
    }

    private static List<String> getColumnNamesIn(String template) {
        List<String> columnNames = new LinkedList<>();

        int length = template.length();
        int fromIndex = 0;
        while (fromIndex < length) {
            int beginIndex = template.indexOf("{", fromIndex);
            int endIndex = template.indexOf("}", fromIndex);
            String columnName = template.substring(beginIndex + 1, endIndex);
            columnNames.add(columnName);
            fromIndex = endIndex + 1;
        }

        return columnNames;
    }

    private static boolean isValidIRITemplate(String template) {
        template = template.replace("\"", "");
        template = template.replace("{", "");
        template = template.replace("}", "");

        return R2RMLParser.isURI(template);
    }
}
