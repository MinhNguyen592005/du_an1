package com.vu.springapi.service;

import com.vu.springapi.dto.request.ChangePasswordRequest;
import com.vu.springapi.dto.request.UpdateMyInfoRequest;
import com.vu.springapi.dto.request.UserCreateRequest;
import com.vu.springapi.dto.request.UserUpdateRequest;
import com.vu.springapi.dto.response.UserResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.mapper.UserMapper;
import com.vu.springapi.model.Cart;
import com.vu.springapi.model.Order;
import com.vu.springapi.model.User;
import com.vu.springapi.repository.CartRepository;
import com.vu.springapi.repository.RoleRepository;
import com.vu.springapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;

    public UserResponse createUser(UserCreateRequest request){
        if(userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.EMAIL_EXIST);
        if(userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USERNAME_EXIST);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        List<String> roleUser = new ArrayList<>();
        roleUser.add("USER");

        var roles = roleRepository.findAllById(roleUser);
        user.setRoles(new HashSet<>(roles));

        User savedUser = userRepository.save(user);

        Cart cart = Cart.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();


        cartRepository.save(cart);

        return userMapper.toUserResponse(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAuthority('CREATE_DATA')")
    public List<UserResponse> getUsers(){
        log.info("In method get Users");
        return userRepository.findAll().stream()
                .map(user -> {
                    UserResponse userResponse = userMapper.toUserResponse(user);
                    // Calculate total order value
                    BigDecimal totalOrderValue = user.getOrders().stream()
                            .map(Order::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    userResponse.setTotalOrderValue(totalOrderValue);
                    return userResponse;
                })
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id){
        log.info("In method get user by id");
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @PostAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest){
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(user, userUpdateRequest);
        var roles = roleRepository.findAllById(userUpdateRequest.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserResponse userResponse = userMapper.toUserResponse(user);
        // Calculate total order value
        BigDecimal totalOrderValue = user.getOrders().stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        userResponse.setTotalOrderValue(totalOrderValue);

        return userResponse;
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateMyInfo(UpdateMyInfoRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateMyInfo(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        log.info("Deleting user with id: {} and username: {}", id, user.getUsername());
        // With cascade delete configured in User entity, cart, orders, and addresses will be automatically deleted
        userRepository.delete(user);
        log.info("User deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changePassword(Long id, ChangePasswordRequest request){
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }
        else{
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse changePassword(ChangePasswordRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }
        else{
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
