package com.vu.springapi.config;

import com.vu.springapi.model.Category;
import com.vu.springapi.model.Permission;
import com.vu.springapi.model.Role;
import com.vu.springapi.model.User;
import com.vu.springapi.repository.CategoryRepository;
import com.vu.springapi.repository.PermissionRepository;
import com.vu.springapi.repository.RoleRepository;
import com.vu.springapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    protected ApplicationRunner applicationRunner(UserRepository userRepository, PermissionRepository permissionRepository, RoleRepository roleRepository, CategoryRepository categoryRepository){
        return args -> {
            // Khởi tạo categories mặc định
            if(categoryRepository.count() == 0){
                Category quan = Category.builder()
                        .name("Quần")
                        .description("Các loại quần")
                        .build();
                categoryRepository.save(quan);

                Category ao = Category.builder()
                        .name("Áo")
                        .description("Các loại áo")
                        .build();
                categoryRepository.save(ao);

                Category giay = Category.builder()
                        .name("Giày")
                        .description("Các loại giày")
                        .build();
                categoryRepository.save(giay);

                Category phuKien = Category.builder()
                        .name("Phụ kiện")
                        .description("Các loại phụ kiện thời trang")
                        .build();
                categoryRepository.save(phuKien);

                log.warn("Default categories have been created: Quần, Áo, Giày, Phụ kiện");
            }

            if(userRepository.findByUsername("admin").isEmpty()){
                Set<Permission> permissions = new HashSet<>();
                Permission permission = new Permission("UPDATE_DATA", "Update data permission");
                permissions.add(permission);
                permissionRepository.save(permission);


                Set<Role> roles = new HashSet<>();
                Role role = new Role("ADMIN", "Administrator", permissions);
                roleRepository.save(role);
                roles.add(role);

                Set<Permission> permissionsUser = new HashSet<>();
                Permission permissionUser = new Permission("ADD_TO_CART", "Add item into cart");
                permissionsUser.add(permissionUser);
                permissionRepository.save(permissionUser);


                Set<Role> rolesUser = new HashSet<>();
                Role roleUser = new Role("USER", "Customer", permissionsUser);
                roleRepository.save(roleUser);


                User user = User.builder()
                        .username("admin")
                        .name("Administrator")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .dob(LocalDate.of(1990, 1, 1))
                        .build();
                userRepository.save(user);
                log.warn("admin has been created with default password: admin, please change it!");
            }
        };
    }

}
