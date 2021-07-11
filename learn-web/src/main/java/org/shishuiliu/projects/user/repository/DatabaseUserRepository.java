package org.shishuiliu.projects.user.repository;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ConnectionCallback;

import org.shishuiliu.core.function.ThrowableFunction;
import org.shishuiliu.projects.user.domain.User;
import org.shishuiliu.projects.user.sql.DBConnectionManager;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

public class DatabaseUserRepository implements UserRepository {

    private static Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());

    /**
     * 通用处理方式
     */
    private static Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

	public static final String DROP_USERS_TABLE_DDL_SQL = "DROP TABLE users";

	public static final String CREATE_USERS_TABLE_DDL_SQL = "CREATE TABLE users("
			+ "id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			+ "name VARCHAR(16) NOT NULL, " + "password VARCHAR(64) NOT NULL, " + "email VARCHAR(64) NOT NULL, "
			+ "phoneNumber VARCHAR(64) NOT NULL" + ")";
    
    public static final String INSERT_USER_DML_SQL =
            "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
                    "(?,?,?,?)";

    public static final String QUERY_ALL_USERS_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users";

    private final DBConnectionManager dbConnectionManager;

    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    private Connection getConnection() {
        return dbConnectionManager.getConnection();
    }

    @Override
    public boolean save(User user) {
    	 Connection connection = getConnection();
    	 try {
			PreparedStatement psmtp = connection.prepareStatement(INSERT_USER_DML_SQL);
			psmtp.setString(1, user.getName());
			psmtp.setString(2, user.getPassword());
			psmtp.setString(3, user.getEmail());
			psmtp.setString(4, user.getPhoneNumber());
			System.out.println(user.toString());
			System.out.println(psmtp.executeUpdate());
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	 
        return true;
    }

    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public User getByEmailAndPassword(String email, String password) {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users WHERE email=? and password=?",
                resultSet -> {
                	User user = new User();
                    BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
                    while (resultSet.next()) { // 如果存在并且游标滚动 // SQLException
                        for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                            String fieldName = propertyDescriptor.getName();
                            Class fieldType = propertyDescriptor.getPropertyType();
                            String methodName = resultSetMethodMappings.get(fieldType);
                            // 可能存在映射关系（不过此处是相等的）
                            String columnLabel = mapColumnLabel(fieldName);
                            Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                            // 通过放射调用 getXXX(String) 方法
                            Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                            // 获取 User 类 Setter方法
                            // PropertyDescriptor ReadMethod 等于 Getter 方法
                            // PropertyDescriptor WriteMethod 等于 Setter 方法
                            Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                            // 以 id 为例，  user.setId(resultSet.getLong("id"));
                            setterMethodFromUser.invoke(user, resultValue);
                        }
                    }
                    return user;
                }, COMMON_EXCEPTION_HANDLER, email, password);
    }

    @Override
    public Collection<User> getAll() {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users", resultSet -> {
            // BeanInfo -> IntrospectionException
            BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
            List<User> users = new ArrayList<>();
            while (resultSet.next()) { // 如果存在并且游标滚动 // SQLException
                User user = new User();
                for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                    String fieldName = propertyDescriptor.getName();
                    Class fieldType = propertyDescriptor.getPropertyType();
                    String methodName = resultSetMethodMappings.get(fieldType);
                    // 可能存在映射关系（不过此处是相等的）
                    String columnLabel = mapColumnLabel(fieldName);
                    Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                    // 通过放射调用 getXXX(String) 方法
                    Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                    // 获取 User 类 Setter方法
                    // PropertyDescriptor ReadMethod 等于 Getter 方法
                    // PropertyDescriptor WriteMethod 等于 Setter 方法
                    Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                    // 以 id 为例，  user.setId(resultSet.getLong("id"));
                    setterMethodFromUser.invoke(user, resultValue);
                }
            }
            return users;
        }, e -> {
            e.printStackTrace();
        });
    }

    /**
     * @param sql
     * @param function
     * @param <T>
     * @return
     */
    protected <T> T executeQuery(String sql, ThrowableFunction<ResultSet, T> function,
                                 Consumer<Throwable> exceptionHandler, Object... args) {
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Class argType = arg.getClass();

                Class wrapperType = wrapperToPrimitive(argType);

                if (wrapperType == null) {
                    wrapperType = argType;
                }
                // Boolean -> boolean
                String methodName = preparedStatementMethodMappings.get(argType);
                Method method = PreparedStatement.class.getMethod(methodName, int.class, wrapperType);
                method.invoke(preparedStatement, i + 1, wrapperType.cast(arg));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            return function.apply(resultSet);
        } catch (Throwable e) {
            e.printStackTrace();;
        }
        return null;
    }


    public void initDatabase() throws SQLException {
    	Connection connection = getConnection();
    	Statement statement = connection.createStatement();
    	statement.execute(DROP_USERS_TABLE_DDL_SQL);
    	statement.execute(CREATE_USERS_TABLE_DDL_SQL);
    	statement.close();
    }
    
    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> resultSetMethodMappings = new HashMap<>();

    static Map<Class, String> preparedStatementMethodMappings = new HashMap<>();

    static {
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        preparedStatementMethodMappings.put(Long.class, "setLong"); // long
        preparedStatementMethodMappings.put(String.class, "setString"); //


    }
}
