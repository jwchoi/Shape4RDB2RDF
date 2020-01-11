package shaper.mapping.shex;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import gr.seab.r2rml.beans.Database;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.LogicalTableMapping;
import gr.seab.r2rml.entities.LogicalTableView;
import gr.seab.r2rml.entities.MappingDocument;
import gr.seab.r2rml.entities.TermType;
import gr.seab.r2rml.entities.sql.SelectQuery;
import janus.database.SQLResultSet;
import janus.database.SQLSelectField;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import shaper.Shaper;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;
import shaper.mapping.model.shex.NodeConstraint;
import shaper.mapping.model.shex.ShExSchemaFactory;
import shaper.mapping.model.shex.Shape;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.*;

public class R2RMLShExMapper extends ShExMapper {
    //-> for R2RML parser
    private String propertiesFile;
    Properties properties;
    private MappingDocument mappingDocument;
    //<- for R2RML parser

    public R2RMLShExMapper(String propertiesFile) { this.propertiesFile = propertiesFile; }

    private boolean loadPropertiesFile() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (Exception ex) {
            System.err.println("Error reading properties file (" + propertiesFile + ").");
            return false;
        }

        return true;
    }

    private void generateMappingDocument() {
        if (mappingDocument != null) return;

        if (properties == null) {
            if (!loadPropertiesFile()) return;
        }

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");

        Database db = (Database) context.getBean("db");
        db.setProperties(properties);

        Parser parser = (Parser) context.getBean("parser");
        parser.setProperties(properties);

        mappingDocument = parser.parse();

        context.close();
    }

    private R2RMLModel buildMappingModel(MappingDocument mappingDocument) {
        R2RMLModel r2rmlModel = new R2RMLModel();

        // prefixes
        Map<String, String> prefixMap = mappingDocument.getPrefixes();
        Set<Map.Entry<String, String>> entrySet = prefixMap.entrySet();
        for (Map.Entry<String, String> entry: entrySet)
            r2rmlModel.addPrefixMap(entry.getKey(), entry.getValue());

        // logical tables
        List<LogicalTableView> logicalTableViews = mappingDocument.getLogicalTableViews();
        for (LogicalTableView logicalTableView: logicalTableViews) {
            String uri = logicalTableView.getUri();
            if (uri != null) {
                LogicalTable logicalTable = new LogicalTable();

                logicalTable.setUri(URI.create(uri));

                SelectQuery selectQuery = logicalTableView.getSelectQuery();
                if (selectQuery != null)
                    logicalTable.setSqlQuery(selectQuery.getQuery());

                r2rmlModel.addLogicalTable(logicalTable);
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

    private List<SQLSelectField> createSQLSelectFields(List<String> selectFields, String selectQuery, SQLResultSet sqlResultSet) {
        List<SQLSelectField> selectFieldList = new ArrayList<>();
        for (String selectField: selectFields)
            selectFieldList.add(createSQLSelectField(selectField, selectQuery, sqlResultSet));

        return selectFieldList;
    }

    private SQLSelectField createSQLSelectField(String selectField, String selectQuery, SQLResultSet sqlResultSet) {
        SQLSelectField sqlSelectField = new SQLSelectField(selectField, selectQuery);

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

    private void writeDirectives() {
        // base
        String defaultNamespace = properties.getProperty("default.namespace");
        if (defaultNamespace != null)
            writer.println(Symbols.BASE + Symbols.SPACE + Symbols.LT + defaultNamespace + Symbols.GT); // for a default RDF Graph namespace by r2rml

        // prefix for newly created shape expressions
        writer.println(Symbols.PREFIX + Symbols.SPACE + shExSchema.getPrefix() + Symbols.COLON + Symbols.SPACE + Symbols.LT + shExSchema.getBaseIRI() + Symbols.HASH + Symbols.GT);

        // prefixes
        Set<Map.Entry<String, String>> entrySet = r2rmlModel.getPrefixMap().entrySet();
        for (Map.Entry<String, String> entry: entrySet)
            writer.println(Symbols.PREFIX + Symbols.SPACE + entry.getKey() + Symbols.COLON + Symbols.SPACE + Symbols.LT + entry.getValue() + Symbols.GT);
        writer.println();
    }

    private void writeShEx() {
        List<TriplesMap> triplesMaps = r2rmlModel.getTriplesMaps();

        for (TriplesMap triplesMap : triplesMaps) {
            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            for (PredicateObjectMap predicateObjectMap: predicateObjectMaps) {
                Optional<ObjectMap> objectMap = predicateObjectMap.getObjectMap();
                if (objectMap.isPresent()) {
                    if (NodeConstraint.isPossibleToHaveXSFacet(objectMap.get())) {
                        String nodeConstraintID = shExSchema.getMappedNodeConstraintID(objectMap.get());
                        String nodeConstraint = shExSchema.getMappedNodeConstraint(objectMap.get());
                        if (nodeConstraintID != null && nodeConstraint != null) {
                            String id = shExSchema.getPrefix() + Symbols.COLON + nodeConstraintID;
                            writer.println(id + Symbols.SPACE + nodeConstraint);
                        }
                    }
                }
            }
            writer.println();

            writer.println(shExSchema.getMappedShape(triplesMap.getUri()));
            writer.println();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Set<Shape> derivedShapes = shExSchema.getDerivedShapes();
        for (Shape derivedShape: derivedShapes) {
            writer.println(derivedShape);
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

    @Override
    public File generateShExFile() {
        generateMappingDocument();

        r2rmlModel = buildMappingModel(mappingDocument);
        shExSchema = ShExSchemaFactory.getShExSchemaModel(r2rmlModel);

        preProcess();
        writeDirectives();
        writeShEx();
        postProcess();

        System.out.println("Translating the schema into ShEx has finished.");

        return output;
    }
}
