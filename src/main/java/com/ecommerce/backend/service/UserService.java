package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.UserRequest;
import com.ecommerce.backend.dto.request.UserUpdateRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.entity.Customer;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.NotAuthorizedException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.model.enums.Role;
import com.ecommerce.backend.model.mapper.UserMapper;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserMapper userMapper;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::userToUserResponse);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.userToUserResponse(user);
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.userToUserResponse(user);
    }

    public Page<OrderResponse> getOrdersByUserMailAndStatus(String userEmail, OrderStatus status, Pageable pageable) {
        return orderService.getOrdersByUserMailAndStatus(userEmail, status, pageable);
    }


    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(user.getRole().equals(Role.ADMIN)){
            throw new NotAuthorizedException("Cannot deactivate an admin user");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUserProfile(String email, UserUpdateRequest updateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setPhone(updateRequest.getPhone());
        user.setAddress(updateRequest.getAddress());

        User updatedUser = userRepository.save(user);
        return userMapper.userToUserResponse(updatedUser);
    }
}
