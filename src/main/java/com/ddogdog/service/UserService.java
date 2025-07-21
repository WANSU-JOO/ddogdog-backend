package com.ddogdog.service;

import org.springframework.stereotype.Service;
import com.ddogdog.dao.UserDao;
import com.ddogdog.dto.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;  // final로 선언하고 생성자 주입 방식 사용
    
    public User getUser(String userId) {
        return userDao.getUserById(userId);
    }

    public void createUser(User user) {
        userDao.insertUser(user);
    }
}
