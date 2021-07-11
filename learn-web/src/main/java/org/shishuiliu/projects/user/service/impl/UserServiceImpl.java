package org.shishuiliu.projects.user.service.impl;

import java.sql.SQLException;

import org.shishuiliu.projects.user.domain.User;
import org.shishuiliu.projects.user.repository.DatabaseUserRepository;
import org.shishuiliu.projects.user.service.UserService;
import org.shishuiliu.projects.user.sql.DBConnectionManager;

/**
* @author 逝水流/mjhfavourite@126.com
* @date 2021-03-04 01:13:21
* @version 1.0
* @description 
*
*/

public class UserServiceImpl implements UserService {

	DatabaseUserRepository databaseUserRepository = new DatabaseUserRepository(DBConnectionManager.getDbConnectionManager());
	
	@Override
	public boolean register(User user) {
		return databaseUserRepository.save(user);
	}

	@Override
	public boolean deregister(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User queryUserById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User queryUserByEmailAndPassword(String email, String password) {
		return databaseUserRepository.getByEmailAndPassword(email,password);
	}

	@Override
	public void initDatabase() throws SQLException {
		databaseUserRepository.initDatabase();		
	}

}
