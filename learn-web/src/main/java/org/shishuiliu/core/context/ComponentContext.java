package org.shishuiliu.core.context;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.shishuiliu.core.function.ThrowableAction;
import org.shishuiliu.core.function.ThrowableFunction;

/**
 * 组件上下文（Web 应用全局使用）
 */
public class ComponentContext {

	public static final String CONTEXT_NAME = ComponentContext.class.getName();

	/**
	 * 环境名固定值
	 */
	private static final String COMPONENT_ENV_CONTEXT_NAME = "java:comp/env";

	private static final Logger logger = Logger.getLogger(CONTEXT_NAME);

	/**
	 * 请注意 <br/>
	 * 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）<br/>
	 * static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）
	 */
	private static ServletContext servletContext;

	/**
	 * Component Env Context
	 * 
	 */
	private Context envContext;

	private ClassLoader classLoader;

	private Map<String, Object> componentsMap = new LinkedHashMap<>();

	/**
	 * 初始化，在加载时获得servletContext
	 * 
	 * @param servletContext
	 * @throws RuntimeException
	 */
	public void init(ServletContext servletContext) throws RuntimeException {
		ComponentContext.servletContext = servletContext;
		servletContext.setAttribute(CONTEXT_NAME, this);
		// 获取当前 ServletContext（WebApp）ClassLoader
		this.classLoader = servletContext.getClassLoader();
		initEnvContext();
		instantiateComponents();
		initializeComponents();
	}

	/**
	 * 获取 ComponentContext
	 *
	 * @return
	 */
	public static ComponentContext getInstance() {
		return (ComponentContext) servletContext.getAttribute(CONTEXT_NAME);
	}

	/**
	 * 通过名称进行依赖查找
	 *
	 * @param name
	 * @param <C>
	 * @return
	 */
	public <C> C getComponent(String name) {
		return (C) componentsMap.get(name);
	}

	/**
	 * 获取所有的组件名称
	 *
	 * @return
	 */
	public List<String> getComponentNames() {
		return new ArrayList<>(componentsMap.keySet());
	}

	/**
	 * 初始化envContext
	 * 
	 * @throws RuntimeException
	 */
	private void initEnvContext() throws RuntimeException {
		if (this.envContext != null) {
			return;
		}
		Context context = null;
		try {
			context = new InitialContext();
			this.envContext = (Context) context.lookup(COMPONENT_ENV_CONTEXT_NAME);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			close(context);
		}
	}

	/**
	 * 实例化组件
	 */
	private void instantiateComponents() {
		// 遍历获取所有的组件名称
		List<String> componentNames = listAllComponentNames();
		// 通过依赖查找，实例化对象（ Tomcat BeanFactory setter 方法的执行，仅支持简单类型）
		componentNames.forEach(name -> componentsMap.put(name, lookupComponent(name)));
	}

	/**
	 * 初始化组件（支持 Java 标准 Commons Annotation 生命周期）
	 * <ol>
	 * <li>注入阶段 - {@link Resource}</li>
	 * <li>初始阶段 - {@link PostConstruct}</li>
	 * <li>销毁阶段 - {@link PreDestroy}</li>
	 * </ol>
	 */
	private void initializeComponents() {
		componentsMap.values().forEach(component -> {
			Class<?> componentClass = component.getClass();
			// 注入阶段 - {@link Resource}
			injectComponents(component, componentClass);
			// 初始阶段 - {@link PostConstruct}
			processPostConstruct(component, componentClass);
			// TODO 实现销毁阶段 - {@link PreDestroy}
			processPreDestroy();
		});
	}

	/**
	 * 进行注入，需要还原field状态
	 * 
	 * @param component
	 * @param componentClass
	 */
	private void injectComponents(Object component, Class<?> componentClass) {
		Stream.of(componentClass.getDeclaredFields()).filter(field -> {
			int mods = field.getModifiers();
			return !Modifier.isStatic(mods) && field.isAnnotationPresent(Resource.class);
		}).forEach(field -> {
			Resource resource = field.getAnnotation(Resource.class);
			String resourceName = resource.name();
			Object injectedObject = lookupComponent(resourceName);
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			try {
				// 注入目标对象
				field.set(component, injectedObject);
			} catch (IllegalAccessException e) {
				logger.warning(e.getMessage());
			} finally {
				field.setAccessible(accessible);
			}
		});
	}

	private void processPostConstruct(Object component, Class<?> componentClass) {
		Stream.of(componentClass.getMethods()).filter(method -> !Modifier.isStatic(method.getModifiers()) && // 非 static
				method.getParameterCount() == 0 && // 没有参数
				method.isAnnotationPresent(PostConstruct.class) // 标注 @PostConstruct
		).forEach(method -> {
			// 执行目标方法
			try {
				method.invoke(component);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void processPreDestroy() {
		// TODO
	}

	/**
	 * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
	 *
	 * @param function ThrowableFunction
	 * @param <R>      返回结果类型
	 * @return 返回
	 * @see ThrowableFunction#apply(Object)
	 */
	protected <R> R executeInContext(ThrowableFunction<Context, R> function) {
		return executeInContext(function, false);
	}

	/**
	 * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
	 *
	 * @param function         ThrowableFunction
	 * @param ignoredException 是否忽略异常
	 * @param <R>              返回结果类型
	 * @return 返回
	 * @see ThrowableFunction#apply(Object)
	 */
	protected <R> R executeInContext(ThrowableFunction<Context, R> function, boolean ignoredException) {
		return executeInContext(this.envContext, function, ignoredException);
	}

	private <R> R executeInContext(Context context, ThrowableFunction<Context, R> function, boolean ignoredException) {
		R result = null;
		try {
			result = ThrowableFunction.execute(context, function);
		} catch (Throwable e) {
			if (ignoredException) {
				logger.warning(e.getMessage());
			} else {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	protected <C> C lookupComponent(String name) {
		return executeInContext(context -> (C) context.lookup(name));
	}

	private List<String> listAllComponentNames() {
		return listComponentNames("/");
	}

	protected List<String> listComponentNames(String name) {
		return executeInContext(context -> {
			NamingEnumeration<NameClassPair> e = executeInContext(context, ctx -> ctx.list(name), true);

			// 目录 - Context
			// 节点 -
			if (e == null) { // 当前 JNDI 名称下没有子节点
				return Collections.emptyList();
			}

			List<String> fullNames = new LinkedList<>();
			while (e.hasMoreElements()) {
				NameClassPair element = e.nextElement();
				String className = element.getClassName();
				Class<?> targetClass = classLoader.loadClass(className);
				if (Context.class.isAssignableFrom(targetClass)) {
					// 如果当前名称是目录（Context 实现类）的话，递归查找
					fullNames.addAll(listComponentNames(element.getName()));
				} else {
					// 否则，当前名称绑定目标类型的话话，添加该名称到集合中
					String fullName = name.startsWith("/") ? element.getName() : name + "/" + element.getName();
					fullNames.add(fullName);
				}
			}
			return fullNames;
		});
	}

	public void destroy() throws RuntimeException {
		close(this.envContext);
	}

	private static void close(Context context) {
		if (context != null) {
			ThrowableAction.execute(context::close);
		}
	}
}
