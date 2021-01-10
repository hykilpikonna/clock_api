package org.hydev.clock_api.controller;

import org.hydev.clock_api.entity.User;
import org.hydev.clock_api.error.ErrorCode;
import org.hydev.clock_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Validated
@RestController
public class RegistryController {
    private final UserRepository userRepository;

    @Autowired
    public RegistryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    // https://www.baeldung.com/spring-rest-http-headers
    // TODO: This method should be synchronized to avoid race condition.
    // Also, this method should not be private, or else cannot use userRepository.
    public synchronized ResponseEntity<String> register(
            // username & password shouldn't be null, and should match thr regex.
            // [!] @RequestHeader(required = false) makes no need make another error handler.
            // [!] And also, ExceptionHandler of MissingRequestHeaderException cannot deal with all missing fields.
            @Pattern(regexp = User.RE_USERNAME, message = ErrorCode.USER_NAME_NOT_MATCH_REGEX)
            @NotNull(message = ErrorCode.USER_NAME_IS_NULL) @RequestHeader(required = false) String username,
            @Pattern(regexp = User.RE_PASSWORD, message = ErrorCode.USER_PASSWORD_NOT_MATCH_REGEX)
            @NotNull(message = ErrorCode.USER_PASSWORD_IS_NULL) @RequestHeader(required = false) String password
    ) {
        // First, spring will check args. If not pass there regex, raise ConstraintViolationException.
        // Then, the aspect will check username if already exists.

        User user = new User();
        user.setUsername(username);

        // TODO: Using Spring Security instead.
        user.setPasswordMd5(DigestUtils.md5DigestAsHex(password.getBytes()).toLowerCase());

        // After save and flush, uuid field will be generated automatically.
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok(user.getUuid());
    }
}
