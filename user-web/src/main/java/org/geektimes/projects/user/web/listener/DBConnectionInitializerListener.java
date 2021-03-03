package org.geektimes.projects.user.web.listener;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.geektimes.projects.user.sql.DBConnectionManager;

@WebListener
public class DBConnectionInitializerListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			Context iniContext = new InitialContext();
			// java:comp/env 查找环境
			DataSource dataSource = (DataSource) iniContext.lookup("java:comp/env/jdbc/UserPlatformDB");
			DBConnectionManager.getDbConnectionManager().setConnection(dataSource.getConnection());
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		DBConnectionManager.getDbConnectionManager().releaseConnection();
	}
}
