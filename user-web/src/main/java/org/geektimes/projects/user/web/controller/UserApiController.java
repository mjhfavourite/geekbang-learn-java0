package org.geektimes.projects.user.web.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Path;

import org.geektimes.web.mvc.controller.RestController;

/**
 * @author 逝水流/mjhfavourite@126.com
 * @date 2021-03-03 17:53:47
 * @version 1.0
 * @description
 *
 */

@Path("/api/user")
public class UserApiController implements RestController {

	@Path("/login")
	public void getJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("hthht");
		HttpSession session = request.getSession();
		if ("true".equals(String.valueOf(session.getAttribute("auth")))) {
			writeResponse(response, "allow visite");
		} else {
			writeResponse(response, "no permission");
		}
	}

	@Path("/login")
	public void auth(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("authhahhaha");
		HttpSession session = request.getSession();
		if (!"true".equals(String.valueOf(session.getAttribute("auth")))) {
			session.setAttribute("auth", "true");
		}
		return;
	}

	@Path("/logout")
	public void exit(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("authhexit");
		HttpSession session = request.getSession();
		if ("true".equals(String.valueOf(session.getAttribute("auth")))) {
			session.setAttribute("auth", "false");
		}
		return;
	}

	private void writeResponse(HttpServletResponse response, String res) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.write(res);
			out.flush();
		} finally {
			out.close();
		}
	}
}
