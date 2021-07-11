package org.shishuiliu.projects.user.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.shishuiliu.core.context.ComponentContext;

@WebListener
public class ComponentContextInitializerListener implements ServletContextListener {

	private ServletContext servletContext;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.servletContext = sce.getServletContext();
		ComponentContext context = new ComponentContext();
		context.init(servletContext);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}
