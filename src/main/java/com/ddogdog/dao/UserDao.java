package com.ddogdog.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.ddogdog.dto.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserDao {
    
    private final SqlSessionTemplate sqlSession;
    private static final String NAMESPACE = "com.ddogdog.dao.UserDao.";
    
    // 예시: 사용자 조회
    public User getUserById(String userId) {
        return sqlSession.selectOne(NAMESPACE + "getUserById", userId);
    }
    
    // 예시: 사용자 등록
    public int insertUser(Object user) {
        return sqlSession.insert(NAMESPACE + "insertUser", user);
    }
    
    // 예시: 사용자 정보 수정
    public int updateUser(Object user) {
        return sqlSession.update(NAMESPACE + "updateUser", user);
    }
    
    // 예시: 사용자 삭제
    public int deleteUser(String userId) {
        return sqlSession.delete(NAMESPACE + "deleteUser", userId);
    }
} 