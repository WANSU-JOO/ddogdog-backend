package com.ddogdog.dao;

import com.ddogdog.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    User findByKakaoId(Long kakaoId);
    void save(User user);
    User findByUsername(String username);
} 