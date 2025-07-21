package com.ddogdog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ddogdog.dao.UserDao;
import com.ddogdog.dto.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("testUser1")
                .username("테스트사용자")
                .password("password123")
                .email("test@example.com")
                .status("A")
                .build();
    }

    @Test
    @DisplayName("사용자 조회 - 성공")
    void getUser_Success() {
        // given
        String userId = "testUser1";
        when(userDao.getUserById(userId)).thenReturn(testUser);

        // when
        User result = userService.getUser(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("테스트사용자", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userDao, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("사용자 조회 - 존재하지 않는 사용자")
    void getUser_NotFound() {
        // given
        String userId = "nonexistent";
        when(userDao.getUserById(userId)).thenReturn(null);

        // when
        User result = userService.getUser(userId);

        // then
        assertNull(result);
        verify(userDao, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // given
        when(userDao.insertUser(any(User.class))).thenReturn(1);

        // when
        assertDoesNotThrow(() -> userService.createUser(testUser));

        // then
        verify(userDao, times(1)).insertUser(testUser);
    }

    @Test
    @DisplayName("사용자 생성 - 실패 (중복된 사용자)")
    void createUser_DuplicateUser() {
        // given
        when(userDao.insertUser(any(User.class))).thenReturn(0);

        // when & then
        assertDoesNotThrow(() -> userService.createUser(testUser));
        verify(userDao, times(1)).insertUser(testUser);
    }
} 