//package com.mlspamdetection.webapp_backend.service;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.data.domain.Sort.*;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import com.mlspamdetection.webapp_backend.dto.UserDTO;
//import com.mlspamdetection.webapp_backend.model.User;
//import com.mlspamdetection.webapp_backend.repo.UserRepository;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//@RunWith(MockitoJUnitRunner.class)
//public class AdminServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private AdminService adminService;
//
//    @Captor
//    private ArgumentCaptor<Pageable> pageableCaptor;
//
//    @Test
//    public void testGetAllUsersWithResults() {
//        // Setup test data
//        int page = 0;
//        int size = 10;
//        User user1 = new User();
//        User user2 = new User();
//        User user3 = new User();
//        Page<User> mockPage = new PageImpl<>(
//                Arrays.asList(user1, user2, user3),
//                PageRequest.of(page, size, Direction.DESC, "createdAt"),
//                3L
//        );
//
//        // Mock repository response
//        when(userRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
//
//        // Execute method
//        Page<UserDTO> result = adminService.getAllUsers(page, size);
//
//        // Verify interactions
//        verify(userRepository).findAll(pageableCaptor.capture());
//        Pageable capturedPageable = pageableCaptor.getValue();
//
//        // Assert pagination parameters
//        assertEquals(page, capturedPageable.getPageNumber());
//        assertEquals(size, capturedPageable.getPageSize());
//        assertEquals(Direction.DESC, capturedPageable.getSort().getOrderFor("createdAt").getDirection());
//
//        // Assert results
//        assertEquals(3, result.getContent().size());
//        assertTrue(result.getContent().get(0) instanceof UserDTO);
//        assertEquals(3L, result.getTotalElements());
//    }
//
//    @Test
//    public void testGetAllUsersWithEmptyResults() {
//        // Setup test data
//        int page = 1;
//        int size = 5;
//        Page<User> mockPage = new PageImpl<>(
//                Collections.emptyList(),
//                PageRequest.of(page, size, Direction.DESC, "createdAt"),
//                0L
//        );
//
//        // Mock repository response
//        when(userRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
//
//        // Execute method
//        Page<UserDTO> result = adminService.getAllUsers(page, size);
//
//        // Verify interactions
//        verify(userRepository).findAll(pageableCaptor.capture());
//
//        // Assert results
//        assertTrue(result.getContent().isEmpty());
//        assertEquals(0L, result.getTotalElements());
//    }
//}