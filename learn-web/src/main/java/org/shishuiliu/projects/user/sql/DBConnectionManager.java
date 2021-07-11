package org.shishuiliu.projects.user.sql;

import java.sql.Connection;
import java.sql.SQLException;

//import javax.persistence.EntityManager;

public class DBConnectionManager {

	private Connection connection;
	private static final DBConnectionManager DBCONNECTIONMANAGER;

	static {
		DBCONNECTIONMANAGER = new DBConnectionManager();
	}
	
//    public EntityManager getEntityManager() {
//        logger.info("当前 EntityManager 实现类：" + entityManager.getClass().getName());
//        return entityManager;
//    }

	private DBConnectionManager() {

	}

	public static DBConnectionManager getDbConnectionManager() {
		return DBCONNECTIONMANAGER;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void releaseConnection() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e.getCause());
			}
		}
	}

}
