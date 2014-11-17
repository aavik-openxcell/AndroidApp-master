package com.icanvass.database;

public abstract class SDTable {
	public static String TABLE_NAME = "";

	public static String dropStatement(String tableName) {
		return String.format(DROP_STATMENT, TABLE_NAME);
	}

	public static String emptyStatement(String tableName) {
		return String.format(EMPTY_STATEMENT, tableName);
	}

	protected static final String CREATE_STATMENT = "CREATE TABLE %s (%s);";
	protected static final String DROP_STATMENT = "DROP TABLE IF EXISTS %s";
	protected static final String EMPTY_STATEMENT = "DELETE FROM %s";

	/** use it to separate values while creating SQL statements */
	protected static final String COMMA = ",";
	/** use it to save it as a null value */
	protected static final String NULL = " NULL ";
	/** use it to specify not null is required */
	protected static final String NOT_NULL = " NOT NULL ";
	/** use it to save integers, primary keys */
	protected static final String INTEGER = " INTEGER ";

	protected static final String AUTOINCREMENT = " AUTOINCREMENT ";
	/** use it to save doubles, floats */
	protected static final String REAL = " REAL ";
	/** use it to save text, varchar, char */
	protected static final String TEXT = " TEXT ";
	/** use it to save fotos, videos, audio, data etc. */
	protected static final String BLOB = " BLOB ";
	/** */
	protected static final String PRIMARY_KEY = " PRIMARY KEY ";

	protected static final String UNIQUE = " UNIQUE ";
}
