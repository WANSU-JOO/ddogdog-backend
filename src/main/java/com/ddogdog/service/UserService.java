package com.ddogdog.service;

import com.ddogdog.dao.UserDao;
import com.ddogdog.dto.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findByKakaoId(Long kakaoId) {
        return userDao.findByKakaoId(kakaoId);
    }

    public void register(User user) {
        userDao.save(user);
    }

    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }
}
