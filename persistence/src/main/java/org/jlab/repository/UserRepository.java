package org.jlab.repository;

import org.jlab.domain.User;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Created by scorpiovn on 1/3/15.
 */
public interface UserRepository extends Repository<User, Long> {
    List<User> findByUsername(String username);
}
