package org.shishuiliu.projects.user.repository;

import java.util.Collection;
import java.util.List;

import org.shishuiliu.projects.user.domain.User;

/**
 * 用户存储仓库
 *
 * @since 1.0
 */
public interface UserRepository {

    boolean save(User user);

    boolean deleteById(Long userId);

    boolean update(User user);

    User getById(Long userId);

    User getByEmailAndPassword(String userName, String password);

    Collection<User> getAll();
}
