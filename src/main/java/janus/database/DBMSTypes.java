package janus.database;

public enum DBMSTypes {
	MYSQL("org.gjt.mm.mysql.Driver"),
	MARIADB("org.mariadb.jdbc.Driver");
	
	private final String driver;
	
	private DBMSTypes(String driver){
		this.driver = driver;
	}
	
	public String driver() { return driver; }
}
