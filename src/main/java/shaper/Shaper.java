package shaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import janus.database.*;
import shaper.mapping.DirectMappingBasedRDFMapper;
import shaper.mapping.R2RMLBasedRDFMapper;
import shaper.mapping.RDFMapper;

public class Shaper {
	private static final String SHAPER_PROPERTIES_FILE = "shaper.properties";
	private static final String R2RML_PARSER_PROPERTIES_FILE = "r2rml-parser.properties";

	public static final String DEFAULT_DIR_FOR_RDF_FILE = "./output/rdf/";
	public static final String DEFAULT_DIR_FOR_SHACL_FILE = "./output/shacl/";
	public static final String DEFAULT_DIR_FOR_SHEX_FILE = "./output/shex/";

	private static Properties properties;
	
	public static String baseURI;
	public static String prefix;
	
	public static DBBridge dbBridge;
	public static LocalDBSchema localDBSchema;
	public static RDFMapper rdfMapper;

	public static void main (String[] args) {
		if (!readPropertiesFile(SHAPER_PROPERTIES_FILE))
			return;

		baseURI = properties.getProperty("base.iri");
		prefix = properties.getProperty("base.prefix");

		if (!connectDatabase())
			return;

		if (!buildLocalDBSchema())
			return;

		String generationType = properties.getProperty("generation.type");

		if (generationType.equals("both") || generationType.equals("data")) {
			if (!generateRDFFile())
				return;
		}

		if (generationType.equals("both") || generationType.equals("shape")) {
			if (!generateShapeFile())
				return;
		}

		System.out.println("It's all done. See you later!");
	}

	private static boolean generateShapeFile() {
		if (rdfMapper == null) {
			if (!createRDFMapper())
				return false;
		}

		String shapeType = properties.getProperty("shape.type");

		if (shapeType.equals("shex")) {
			File file = rdfMapper.generateShExFile();

			try {
				System.out.println("The ShEx file \"" + file.getCanonicalPath() + "\" is generated.");
			} catch (IOException e) {
				System.out.println("The ShEx file \"" + file.getAbsolutePath() + "\" is generated.");
			}

		}

		return true;
	}

	private static boolean createRDFMapper() {
		RDFMapper rdfMapper = null;

		String mappingType = properties.getProperty("mapping.type");

		if (mappingType.equals(RDFMapper.MapperTypes.DIRECT_MAPPING.toString()))
			rdfMapper = new DirectMappingBasedRDFMapper();
		else if (mappingType.equals(RDFMapper.MapperTypes.R2RML.toString()))
			rdfMapper = new R2RMLBasedRDFMapper(R2RML_PARSER_PROPERTIES_FILE);

		if (rdfMapper != null) {
			Shaper.rdfMapper = rdfMapper;
			return true;
		}

		return false;
	}

	private static boolean generateRDFFile() {
		if (rdfMapper == null) {
			if (!createRDFMapper())
				return false;
		}

		File file = rdfMapper.generateRDFFile();

		try {
			System.out.println("The RDF file \"" + file.getCanonicalPath() + "\" is generated.");
		} catch (IOException e) {
			System.out.println("The RDF file \"" + file.getAbsolutePath() + "\" is generated.");
		}

		return true;
	}

	private static boolean buildLocalDBSchema() {
		LocalDBSchema localDBSchema;
		localDBSchema = LocalDBSchemaFactory.generateLocalDatabaseMetaData(dbBridge);

		if (localDBSchema != null) {
			Shaper.localDBSchema = localDBSchema;
			System.out.println("The schema connected is built in local.");
			return true;
		}

		System.err.println("Building the local database schema was failed.");
		return false;
	}

	private static boolean connectDatabase() {
		String driver = properties.getProperty("db.driver");

		DBMSTypes DBMSType = null;
		if (DBMSTypes.MYSQL.driver().equals(driver))
			DBMSType = DBMSTypes.MYSQL;
		else if (DBMSTypes.MARIADB.driver().equals(driver))
			DBMSType = DBMSTypes.MARIADB;

		String host = properties.getProperty("db.host");
		String port = properties.getProperty("db.port");
		String id = properties.getProperty("db.id");
		String password = properties.getProperty("db.password");
		String schema = properties.getProperty("db.schema");

		DBBridge dbBridge = DBBridgeFactory.getDBBridge(DBMSType, host, port, id, password, schema);

		if(dbBridge != null) {
			Shaper.dbBridge = dbBridge;
			System.out.println("The database is connected.");
			return true;
		}

		System.err.println("Could not connect to the DBMS.");
		return false;
	}

	private static boolean readPropertiesFile(String propertiesFile) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (Exception ex) {
			System.err.println("Error reading properties file (" + propertiesFile + ").");
			properties = null;
		}

		if (properties != null) {
			Shaper.properties = properties;
			System.out.println("The properties file is loaded.");
			return true;
		}

		return false;
	}
}