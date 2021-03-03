package org.geektimes.projects.user.web.controller;

import org.apache.commons.lang.StringUtils;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.service.impl.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 输出 “Hello,World” Controller
 */
@Path("/user/page")
public class UserPageController implements PageController {
	
	private UserService userService = new UserServiceImpl();

    @GET
    @POST
    @Path("/login") // /hello/world -> HelloWorldController
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
    	String email = request.getParameter("inputEmail");
    	String password = request.getParameter("inputPassword");
    	if (!StringUtils.isBlank(email) && !StringUtils.isBlank(password)) {
    		User user = userService.queryUserByEmailAndPassword(email, password);
    		if (user != null) {
    			return "page/home.jsp";
    		}
    	}
        return "page/login.jsp";
    }
    
    @GET
    @POST
    @Path("/home") // /hello/world -> HelloWorldController
    public String execute1(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "page/home.jsp";
    }
}
