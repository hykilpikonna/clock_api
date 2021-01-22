package org.hydev.clock_api.controller;

import org.hydev.clock_api.entity.User;
import org.hydev.clock_api.error.ErrorCode;
import org.hydev.clock_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.List;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    // https://www.baeldung.com/spring-rest-http-headers
    // TODO: This method should be synchronized to avoid race condition.
    // Also, this method should not be private, or else cannot use userRepository.

    // TODO: 2021/1/22 Need a better design! 
    // Controller Return error code list as List<String>, or return uuid as String.
    @SuppressWarnings("rawtypes")
    public synchronized ResponseEntity register(
            // username & password shouldn't be null, and should match thr regex.
            // [!] @RequestHeader(required = false) makes no need make another error handler.
            // [!] And also, ExceptionHandler of MissingRequestHeaderException cannot deal with all missing fields.
            @Pattern(regexp = User.RE_USERNAME, message = ErrorCode.USER_NAME_NOT_MATCH_REGEX)
            @RequestHeader String username,
            @Pattern(regexp = User.RE_PASSWORD, message = ErrorCode.USER_PASSWORD_NOT_MATCH_REGEX)
            @RequestHeader String password
    ) {
        // First, spring will check args. If not pass there regex, raise ConstraintViolationException.
        // Then, we check if username not exists. If username already exists, return ErrorCode with 409 conflict.
        if (userRepository.existsByUsername(username))
            // TODO: 2021/1/22 Using library instead of hand make.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(List.of(ErrorCode.USER_NAME_ALREADY_EXISTS));

        User user = new User();
        user.setUsername(username);
        user.setPasswordMd5(userToSaltedMd5(username, password));

        // After save and flush, uuid field will be generated automatically.
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok(user.getUuid());
    }

    // Format: "$username + $password".toLowerMd5();
    private String userToSaltedMd5(String username, String password) {
        String beforeMd5 = String.format("%s + %s", username, password);
        return DigestUtils.md5DigestAsHex(beforeMd5.getBytes()).toLowerCase();
    }

    @PostMapping("/delete")
    public synchronized ResponseEntity<String> delete(@RequestHeader String username, @RequestHeader String password) {
        User user = userRepository.queryByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();

        if (user.getPasswordMd5().equals(userToSaltedMd5(username, password))) {
            userRepository.delete(user);
            return ResponseEntity.ok("");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
    }
}
