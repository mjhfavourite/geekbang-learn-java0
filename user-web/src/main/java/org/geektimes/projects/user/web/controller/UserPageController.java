package org.geektimes.projects.user.web.controller;

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

    @GET
    @POST
    @Path("/login") // /hello/world -> HelloWorldController
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "page/login.jsp";
    }
    
    @GET
    @POST
    @Path("/home") // /hello/world -> HelloWorldController
    public String execute1(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "page/home.jsp";
    }
}
