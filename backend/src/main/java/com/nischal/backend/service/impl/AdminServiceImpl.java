package com.nischal.backend.service.impl;

import com.nischal.backend.dto.auth.UserResponse;
import com.nischal.backend.dto.user.PageResponse;
import com.nischal.backend.dto.user.UpdateUserRequest;
import com.nischal.backend.entity.Role;
import com.nischal.backend.entity.User;
import com.nischal.backend.exception.ResourceNotFoundException;
import com.nischal.backend.mapper.UserMapper;
import com.nischal.backend.repository.UserRepository;
import com.nischal.backend.service.AdminService;
import com.nischal.backend.service.UserService;
import com.nischal.backend.service.util.PaginationHelper;
import com.nischal.backend.service.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    // Dependency Injection of abstractions (interfaces and helpers)
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ValidationHelper validationHelper;
    private final PaginationHelper paginationHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllStaff(Pageable pageable) {
        log.debug("Fetching all staff members with pagination: {}", pageable);

        List<Role> staffRoles = Arrays.asList(Role.STAFF, Role.OWNER);
        Page<User> staffPage = userRepository.findByRoleIn(staffRoles, pageable);
        
        log.debug("Found {} staff members", staffPage.getTotalElements());

        return paginationHelper.buildUserPageResponse(staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getStaffByRole(Role role, Pageable pageable) {
        log.debug("Fetching staff members with role: {} and pagination: {}", role, pageable);
        validationHelper.validateStaffRole(role);
        
        Page<User> staffPage = userRepository.findByRole(role, pageable);
        
        log.debug("Found {} staff members with role {}", staffPage.getTotalElements(), role);
        
        return paginationHelper.buildUserPageResponse(staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getStaffById(Long id) {
        log.debug("Fetching staff member with id: {}", id);
        User user = userService.getUserById(id);
        
        // Validate staff role
        validationHelper.validateIsStaffMember(user);
        
        log.debug("Found staff member: {}", user.getEmail());
        
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStaff(Long id, UpdateUserRequest request) {
        log.debug("Updating staff member with id: {}", id);

        User user = userService.getUserById(id);
        validationHelper.validateIsStaffMember(user);

        updateUserFields(user, request);

        // Save and return
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated staff member with id: {}", id);
        
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteStaff(Long id) {
        log.debug("Deleting staff member with id: {}", id);
        
        // Retrieve and validate user
        User user = userService.getUserById(id);
        validationHelper.validateIsStaffMember(user);
        
        // Delete user
        userRepository.delete(user);
        
        log.info("Successfully deleted staff member with id: {}", id);
    }

    private void updateUserFields(User user, UpdateUserRequest request) {
        // Update full name
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        
        // Update email with uniqueness validation
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            validationHelper.validateEmailUniqueness(request.getEmail(), user.getEmail());
            user.setEmail(request.getEmail());
        }
        
        // Update phone number with uniqueness validation
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            validationHelper.validatePhoneNumberUniqueness(request.getPhoneNumber(), user.getPhoneNumber());
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update role (only staff roles allowed)
        if (request.getRole() != null) {
            validationHelper.validateStaffRole(request.getRole());
            user.setRole(request.getRole());
        }

        // Update active status
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
    }
}
