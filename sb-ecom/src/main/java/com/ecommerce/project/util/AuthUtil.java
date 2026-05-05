package com.ecommerce.project.util;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    private static final boolean DISABLE_AUTH_FOR_DEVELOPMENT = true;
    private static final String DEVELOPMENT_USERNAME = "admin";

    @Autowired
    UserRepository userRepository;

    public String loggedInEmail(){
        User user = loggedInUser();

        return user.getEmail();
    }

    public Long loggedInUserId(){
        User user = loggedInUser();

        return user.getUserId();
    }

    public User loggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = resolveUsername(authentication);

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return user;

    }

    private String resolveUsername(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            if (DISABLE_AUTH_FOR_DEVELOPMENT) {
                return DEVELOPMENT_USERNAME;
            }
            throw new UsernameNotFoundException("Full authentication is required for this action");
        }

        return authentication.getName();
    }

}
