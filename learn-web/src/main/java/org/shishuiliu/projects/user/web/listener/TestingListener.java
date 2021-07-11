package org.shishuiliu.projects.user.web.listener;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.shishuiliu.core.context.ComponentContext;
import org.shishuiliu.projects.user.domain.User;
import org.shishuiliu.projects.user.sql.DBConnectionManager;

/**
 * 测试用途
 */
@Deprecated
public class TestingListener implements ServletContextListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ComponentContext context = ComponentContext.getInstance();
//        DBConnectionManager dbConnectionManager = context.getComponent("bean/DBConnectionManager");
//        dbConnectionManager.getConnection();
//        testUser(dbConnectionManager.getEntityManager());
        System.out.println();
        logger.info("所有的 JNDI 组件名称：[");
        context.getComponentNames().forEach(logger::info);
        logger.info("]");
    }
//
//    private void testUser(EntityManager entityManager) {
//        User user = new User();
//        user.setName("小马哥");
//        user.setPassword("******");
//        user.setEmail("mercyblitz@gmail.com");
//        user.setPhoneNumber("abcdefg");
//        EntityTransaction transaction = entityManager.getTransaction();
//        transaction.begin();
//        entityManager.persist(user);
//        transaction.commit();
//        System.out.println(entityManager.find(User.class, user.getId()));
//    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
