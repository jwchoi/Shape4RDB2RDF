package shaper.mapping.model.shex;

import janus.database.DBSchema;
import shaper.Shaper;
import janus.database.DBColumn;
import janus.database.DBRefConstraint;
import shaper.mapping.model.r2rml.*;

import java.net.URI;
import java.util.*;

public class ShExSchemaFactory {

    // R2RML
    public static ShExSchema getShExSchemaModel(R2RMLModel r2rmlModel) {
        ShExSchema shExSchema = new ShExSchema(URI.create(Shaper.baseURI), Shaper.prefix);

        List<TriplesMap> triplesMaps = r2rmlModel.getTriplesMaps();

        for (TriplesMap triplesMap : triplesMaps) {

            SubjectMap subjectMap = triplesMap.getSubjectMap();

            // create a shape constraint
            Shape shape = new Shape(triplesMap.getUri(), subjectMap);

            // create Triple Constraint From rr:class
            TripleConstraint tcFromClasses = new TripleConstraint(subjectMap.getClassIRIs());
            shape.addTripleConstraint(tcFromClasses);
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            int sizeOfPredicateObjectMaps = predicateObjectMaps.size();
            for (int i = 0; i < sizeOfPredicateObjectMaps; i++) {
                PredicateObjectMap predicateObjectMap = predicateObjectMaps.get(i);

                // when referencing object map
                if (predicateObjectMap.getRefObjectMap().isPresent()) continue;

                PredicateMap predicateMap = predicateObjectMap.getPredicateMap();
                ObjectMap objectMap = predicateObjectMap.getObjectMap().get();

                // create Node Constraint From predicate-object map
                String nodeConstraintID = shape.getShapeID() + "_Obj" + (i+1);
                NodeConstraint nodeConstraint = new NodeConstraint(nodeConstraintID, objectMap);
                shExSchema.addNodeConstraint(nodeConstraint);

                // create Triple Constraint From predicate-object map
                TripleConstraint tcFromPOMap = new TripleConstraint(predicateMap, objectMap);
                shape.addTripleConstraint(tcFromPOMap);
            }

            shExSchema.addShape(shape);
        }

        for (TriplesMap triplesMap : triplesMaps) {
            // find the mapped shape
            Shape shape = shExSchema.getMappedShape(triplesMap.getUri());
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {

                // when object map
                if (predicateObjectMap.getObjectMap().isPresent()) continue;

                PredicateMap predicateMap = predicateObjectMap.getPredicateMap();
                RefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap().get();

                // create Triple Constraint From referencing-object map
                TripleConstraint tcFromPROMap = new TripleConstraint(predicateMap, refObjectMap);
                shape.addTripleConstraint(tcFromPROMap);
            }
        }


        Set<URI> checkedTriplesMaps = new HashSet<>();
        for (TriplesMap triplesMap : triplesMaps) {
            URI uriOfTriplesMap = triplesMap.getUri();
            if (!checkedTriplesMaps.contains(uriOfTriplesMap)) {
                // find the mapped shape
                Shape mappedShape = shExSchema.getMappedShape(uriOfTriplesMap);
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                Set<Shape> baseShapes = shExSchema.getShapesToShareTheSameSubjects(mappedShape);
                for (Shape baseShape : baseShapes)
                    checkedTriplesMaps.add(baseShape.getMappedTriplesMap().get());

                Set<Set<Shape>> setsForDerivedShapes = shExSchema.createSetsForDerivedShapes(baseShapes);
                for (Set<Shape> set: setsForDerivedShapes) {
                    Shape derivedShape = new Shape(set);

                    // tripleConstraint
                    Set<URI> classIRIs = new TreeSet<>();
                    for (Shape shape: set) {
                        Set<TripleConstraint> tripleConstraints = shape.getTripleConstraints();
                        for (TripleConstraint tc: tripleConstraints) {
                            if (tc.getMappedType().equals(TripleConstraint.MappedTypes.RR_CLASSES))
                                classIRIs.addAll(tc.getClassIRIs());
                            else
                                derivedShape.addTripleConstraint(tc); // except for RR_CLASSES tripleConstraints
                        }
                    }

                    // RR_CLASSES tripleConstraint
                    derivedShape.addTripleConstraint(new TripleConstraint(new ArrayList<>(classIRIs)));

                    shExSchema.addShape(derivedShape);
                }
            }
        }

        return shExSchema;
    }

    //Direct Mapping
    public static ShExSchema getShExSchemaModel(DBSchema dbSchema) {
        ShExSchema schemaMD = new ShExSchema(URI.create(Shaper.baseURI), Shaper.prefix);

        Set<String> tables = dbSchema.getTableNames();

        for(String table : tables) {
            //-> node constraints
            List<String> columns = dbSchema.getColumns(table);
            for(String column: columns) {
                NodeConstraint ncMD = new NodeConstraint(table, column);

                schemaMD.addNodeConstraint(ncMD);
            } // END COLUMN
            //<- node constraints

            //-> shape
            Shape shapeMD = new Shape(table);

            // Triple Constraint From Table
            TripleConstraint tcFromTable = new TripleConstraint(table);
            shapeMD.addTripleConstraint(tcFromTable);

            // Triple Constraint From Column
            for(String column: columns) {
                TripleConstraint tcFromColumn = new TripleConstraint(new DBColumn(table, column));

                shapeMD.addTripleConstraint(tcFromColumn);
            } // END COLUMN

            // Triple Constraint From Referential Constraint
            Set<String> refConstraints = dbSchema.getRefConstraints(table);
            for(String refConstraint: refConstraints) {
                TripleConstraint tcFromRefConstraint = new TripleConstraint(new DBRefConstraint(table, refConstraint), false);

                shapeMD.addTripleConstraint(tcFromRefConstraint);
            } // END REFERENTIAL CONSTRAINT

            // Inverse Triple Constraint From Referential Constraint
            Set<DBRefConstraint> refConstraintsPointingTo = dbSchema.getRefConstraintsPointingTo(table);
            for(DBRefConstraint refConstraint: refConstraintsPointingTo) {
                TripleConstraint tcFromRefConstraint = new TripleConstraint(refConstraint, true);

                shapeMD.addTripleConstraint(tcFromRefConstraint);
            } // END Inverse Triple Constraint From Referential Constraint

            schemaMD.addShape(shapeMD);
            //<- shape
        } // END TABLE

        return schemaMD;
    }
}
