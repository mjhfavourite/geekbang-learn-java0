package org.geektimes.web.mvc;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.substringAfter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.geektimes.web.mvc.controller.Controller;
import org.geektimes.web.mvc.controller.PageController;
import org.geektimes.web.mvc.controller.RestController;

public class FrontControllerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 静态资源等可以跳过servlet的配置
	 */
	private List<String> excludePathList = new ArrayList<String>();

	/**
	 * 请求路径和 Controller 的映射关系缓存
	 */
	private Map<String, Controller> controllersMapping = new HashMap<>();

	/**
	 * 请求路径和 {@link HandlerMethodInfo} 映射关系缓存
	 */
	private Map<String, HandlerMethodInfo> handleMethodInfoMapping = new HashMap<>();

	/**
	 * 初始化 Servlet
	 *
	 * @param servletConfig
	 * @throws ServletException 
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		initHandleMethods();
		initParameters(servletConfig.getInitParameter("excludePathList"));
	}

	/**
	 * 参数初始化
	 * 
	 * @param initParameter1
	 */
	private void initParameters(String initParameter1) {
		if (StringUtils.isNotBlank(initParameter1)) {
			excludePathList.addAll(Arrays.asList(StringUtils.split(initParameter1, ",", -1)));
		}
	}

	/**
	 * 读取所有的 RestController 的注解元信息 @Path 利用 ServiceLoader 技术（Java SPI）
	 */
	private void initHandleMethods() {
		for (Controller controller : ServiceLoader.load(Controller.class)) {
			Class<?> controllerClass = controller.getClass();
			Path pathFromClass = controllerClass.getAnnotation(Path.class);
			String requestPath = pathFromClass.value();
			Method[] publicMethods = controllerClass.getMethods();
			// 处理方法支持的 HTTP 方法集合
			for (Method method : publicMethods) {
				Set<String> supportedHttpMethods = findSupportedHttpMethods(method);
				Path pathFromMethod = method.getAnnotation(Path.class);
				if (pathFromMethod != null) {
					String methodPath = requestPath + pathFromMethod.value();
					handleMethodInfoMapping.put(methodPath,
							new HandlerMethodInfo(methodPath, method, supportedHttpMethods));
					controllersMapping.put(methodPath, controller);
				}
			}
		}
	}

	/**
	 * 获取处理方法中标注的 HTTP方法集合
	 *
	 * @param method 处理方法
	 * @return
	 */
	private Set<String> findSupportedHttpMethods(Method method) {
		Set<String> supportedHttpMethods = new LinkedHashSet<>();
		for (Annotation annotationFromMethod : method.getAnnotations()) {
			HttpMethod httpMethod = annotationFromMethod.annotationType().getAnnotation(HttpMethod.class);
			if (httpMethod != null) {
				supportedHttpMethods.add(httpMethod.value());
			}
		}

		if (supportedHttpMethods.isEmpty()) {
			supportedHttpMethods.addAll(asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
					HttpMethod.HEAD, HttpMethod.OPTIONS));
		}

		return supportedHttpMethods;
	}

	/**
	 * SCWCD
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 建立映射关系
		// requestURI = /a/hello/world
		String requestURI = request.getRequestURI();
		// contextPath = /a or "/" or ""
		String servletContextPath = request.getContextPath();
		String prefixPath = servletContextPath;
		// 映射路径（子路径）
		String requestMappingPath = substringAfter(requestURI, StringUtils.replace(prefixPath, "//", "/"));

		// 检测路径是否是接口路径,如果是不需要经过servlet的路径直接返回
		for (String excludePath : excludePathList) {
			if (StringUtils.startsWith(requestMappingPath, excludePath)) {
				return;
			}
		}

		// 映射到 Controller
		Controller controller = controllersMapping.get(requestMappingPath);

		// 如果是接口路径但是没有对应的controller，直接返回资源不存在
		if (controller != null) {
			HandlerMethodInfo handlerMethodInfo = handleMethodInfoMapping.get(requestMappingPath);

			try {
				if (handlerMethodInfo != null) {
					String httpMethod = request.getMethod();

					if (!handlerMethodInfo.getSupportedHttpMethods().contains(httpMethod)) {
						// HTTP 方法不支持
						response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
						return;
					}

					if (controller instanceof PageController) {
						// 根据方法请求
						String viewPath = String.class
								.cast(handlerMethodInfo.getHandlerMethod().invoke(controller, request, response));

						// PageController pageController = PageController.class.cast(controller);
						// String viewPath = pageController.execute(request, response);
						// 页面请求 forward
						// request -> RequestDispatcher forward
						// RequestDispatcher requestDispatcher = request.getRequestDispatcher(viewPath);
						// ServletContext -> RequestDispatcher forward
						// ServletContext -> RequestDispatcher 必须以 "/" 开头

						ServletContext servletContext = request.getServletContext();
						if (!viewPath.startsWith("/")) {
							viewPath = "/" + viewPath;
						}
						RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(viewPath);
						requestDispatcher.forward(request, response);
						return;
					} else if (controller instanceof RestController) { // 直接调用方法，由response写输出流
						handlerMethodInfo.getHandlerMethod().invoke(controller, request, response);
						return;
					}

				} else {
					// 没有对应的路径
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			} catch (Throwable throwable) {
				if (throwable.getCause() instanceof IOException) {
					throw (IOException) throwable.getCause();
				} else {
					throw new ServletException(throwable.getCause());
				}
			}
		} else {
			// 没有对应的路径
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

//    private void beforeInvoke(Method handleMethod, HttpServletRequest request, HttpServletResponse response) {
//
//        CacheControl cacheControl = handleMethod.getAnnotation(CacheControl.class);
//
//        Map<String, List<String>> headers = new LinkedHashMap<>();
//
//        if (cacheControl != null) {
//            CacheControlHeaderWriter writer = new CacheControlHeaderWriter();
//            writer.write(headers, cacheControl.value());
//        }
//    }
}
