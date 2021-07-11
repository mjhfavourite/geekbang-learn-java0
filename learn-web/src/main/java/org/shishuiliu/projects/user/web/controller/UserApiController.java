package org.shishuiliu.projects.user.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.ws.rs.Path;

import org.shishuiliu.projects.user.domain.User;
import org.shishuiliu.projects.user.service.UserService;
import org.shishuiliu.projects.user.service.impl.UserServiceImpl;
import org.shishuiliu.web.mvc.controller.RestController;

/**
 * @author 逝水流/mjhfavourite@126.com
 * @date 2021-03-03 17:53:47
 * @version 1.0
 * @description
 *
 */

@Path("/api/user")
public class UserApiController implements RestController {

	private UserService userService = new UserServiceImpl();

	@Path("/init")
	public void InitDataBase(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException {
		userService.initDatabase();
		writeResponse(response, "数据库初始化成功");
	}

	@Path("/register")
	public void getJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String email = "test@126.com";
		String name = "test";
		String password = "test1";
		String phoneNumber = "13800138000";
		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setPassword(password);
		user.setPhoneNumber(phoneNumber);

		boolean flag = userService.register(user);
		if (flag) {
			writeResponse(response, "注册成功");
		} else {
			writeResponse(response, "注册失败");
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
	public void exit(HttpServletRequest request, HttpServletResponse response) throws NamingException, SQLException {
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
