package com.dotmarketing.db;
					connectionsList.put(Constants.DATABASE_DEFAULT_DATASOURCE, connection);
					Logger.debug(DbConnectionFactory.class, "Connection opened for thread " + Thread.currentThread().getId() + "-" +
							Constants.DATABASE_DEFAULT_DATASOURCE);
				}


			return connection;
		} catch (Exception e) {
			Logger.error(DbConnectionFactory.class, "---------- DBConnectionFactory: error : " + e);
			throw new DotRuntimeException(e.toString());
		}
	}

	/**
	 * Retrieves the list of all valid dataSources setup in the dotCMS
	 * @return
	 * @throws NamingException
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<String> getAllDataSources()throws NamingException{
		ArrayList<String> results = new ArrayList<String>();
			ne = ctx.listBindings("jdbc");
		while(ne.hasMore()){
			Binding binding = (Binding)ne.next();
			Connection cn = null;
			try {
				DataSource db = (DataSource) ctx.lookup("jdbc/" + binding.getName());
				cn = db.getConnection();
				results.add("jdbc/" + binding.getName());
			} catch (Exception e) {
				Logger.info(DbConnectionFactory.class, "Unable to add " + binding.getName() + " to list of datasources: " + e.getMessage());
			} finally {
				if(cn != null)
					try {
						cn.close();
					} catch (SQLException e) { }
			}
		}
		return results;
	}

	/**
	 * Retrieves a connection to the given dataSource
	 * @param dataSource
	 * @return
	 */
	public static Connection getConnection(String dataSource) {

		try {
			HashMap<String, Connection> connectionsList = (HashMap<String, Connection>) connectionsHolder.get();
			Connection connection = null;

			if(connectionsList == null) {
				connectionsList = new HashMap<String, Connection>();
				connectionsHolder.set(connectionsList);
			}


				connection = connectionsList.get(dataSource);

				if (connection == null || connection.isClosed()) {
					DataSource db = getDataSource(dataSource);
					Logger.debug(DbConnectionFactory.class, "Opening connection for thread " + Thread.currentThread().getId() + "-" +
							dataSource + "\n" + UtilMethods.getDotCMSStackTrace());
					connection = db.getConnection();
					connectionsList.put(dataSource, connection);
					Logger.debug(DbConnectionFactory.class, "Connection opened for thread " + Thread.currentThread().getId() + "-" +
							dataSource);
				}

			return connection;
		} catch (Exception e) {
			Logger.error(DbConnectionFactory.class, "---------- DBConnectionFactory: error getting dbconnection conn named", e);
			throw new DotRuntimeException(e.toString());
		}

	}

	/**
	 * This method closes all the possible opened connections
	 */
	public static void closeConnection() {
		try {
			HashMap<String, Connection> connectionsList = (HashMap<String, Connection>) connectionsHolder.get();

			if(connectionsList == null) {
				connectionsList = new HashMap<String, Connection>();
				connectionsHolder.set(connectionsList);
			}


				Logger.debug(DbConnectionFactory.class, "Closing all connections for " + Thread.currentThread().getId() +
						"\n" + UtilMethods.getDotCMSStackTrace());
				for (Entry<String, Connection> entry : connectionsList.entrySet()) {

					String ds = entry.getKey();
					Connection cn = entry.getValue();
					if(cn != null)
						try {
							cn.close();
						} catch (Exception e) {
							Logger.warn(DbConnectionFactory.class, "---------- DBConnectionFactory: error closing the db dbconnection: " + ds + " ---------------", e);
						}
				}

				Logger.debug(DbConnectionFactory.class, "All connections closed for " + Thread.currentThread().getId());
				connectionsList.clear();

		} catch (Exception e) {
			Logger.error(DbConnectionFactory.class, "---------- DBConnectionFactory: error closing the db dbconnection ---------------", e);
			throw new DotRuntimeException(e.toString());
		}

	}

	/**
	 * This method closes a connection to the given datasource
	 */
	public static void closeConnection(String ds) {
		try {
			HashMap<String, Connection> connectionsList = (HashMap<String, Connection>) connectionsHolder.get();

			if(connectionsList == null) {
				connectionsList = new HashMap<String, Connection>();
				connectionsHolder.set(connectionsList);
			}


				Connection cn = connectionsList.get(ds);

				if(cn != null) {
					Logger.debug(DbConnectionFactory.class, "Closing connection for " + Thread.currentThread().getId() + "-" + ds +
							"\n" + UtilMethods.getDotCMSStackTrace());
					cn.close();
					connectionsList.remove(ds);
					Logger.debug(DbConnectionFactory.class, "Connection closed for " + Thread.currentThread().getId() + "-" + ds);
				}

		} catch (Exception e) {
			Logger.error(DbConnectionFactory.class, "---------- DBConnectionFactory: error closing the db dbconnection: " + ds + " ---------------", e);
			throw new DotRuntimeException(e.toString());
		}

	}

	public static String getDBType() {

		/*
		 * Here is what this out outputs : MySQL PostgreSQL Microsoft SQL Server
		 * Oracle
		 */

		if (_dbType != null) {
			return _dbType;
		}

		Connection conn = getConnection();

		try {
			_dbType = conn.getMetaData().getDatabaseProductName();

		} catch (Exception e) {

		} finally {
			try {
				closeConnection();
			} catch (Exception e) {

			}
		}

		return _dbType;
	}

	public static String getDBTrue() {
		String x = getDBType();

		if (MYSQL.equals(x)) {
			return "1";
		} else if (POSTGRESQL.equals(x)) {
			return "'true'";
		} else if (MSSQL.equals(x)) {
			return "1";
		} else if (ORACLE.equals(x)) {
			return "1";
		}
		return "true";

	}

	public static String getDBFalse() {
		String x = getDBType();

		if (MYSQL.equals(x)) {
			return "0";
		} else if (POSTGRESQL.equals(x)) {
			return "'false'";
		} else if (MSSQL.equals(x)) {
			return "0";
		} else if (ORACLE.equals(x)) {
			return "0";
		}
		return "false";

	}

	public static boolean isDBTrue(String value) {
}