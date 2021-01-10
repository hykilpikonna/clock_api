package org.hydev.clock_api.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hydev.clock_api.error.ErrorCode;
import org.hydev.clock_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class RegistryControllerAspect {
    private final UserRepository userRepository;

    public RegistryControllerAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Around(value = "execution(* RegistryController.register(String, String)) " +
            "&& args(username, password)", argNames = "pjp, username, password")
    // Even if I'm not use the password argument, I still should write it out!
    private Object checkUsernameExists(ProceedingJoinPoint pjp, String username, String password) throws Throwable {
        if (userRepository.existsByUsername(username))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(List.of(ErrorCode.USER_NAME_ALREADY_EXISTS));
        return pjp.proceed();
    }
}
