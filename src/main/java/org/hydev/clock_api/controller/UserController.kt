package org.hydev.clock_api.controller

import org.hydev.clock_api.*
import org.hydev.clock_api.entity.FamilyRepo
import org.hydev.clock_api.entity.User
import org.hydev.clock_api.error.ErrorCode
import org.hydev.clock_api.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/user")
open class UserController(private val userRepo: UserRepository, private val familyRepo: FamilyRepo)
{
    /**
     * Register a user to the database.
     *
     *
     * https://www.baeldung.com/spring-rest-http-headers
     * This method should be synchronized to avoid race condition.
     * Also, this method should not be private, or else cannot use userRepository.
     *
     *
     * TODO: 2021/1/22 Need a better design!
     * Controller Return error code list as List<String>, or return uuid as String.
     *
     * @param username Unique username (Not empty, and should match the regex `User.RE_USERNAME`)
     * @param password Password initial hash (Not empty)
     * @return Success or error
     */
    @PostMapping("/register")
    @Synchronized
    fun register( 
        // [!] @H(required = false) makes no need make another error handler.
        // [!] And also, ExceptionHandler of MissingHException cannot deal with all missing fields.
        @H username: @Pattern(regexp = User.RE_USERNAME, message = ErrorCode.USER_NAME_NOT_MATCH_REGEX) Str,
        @H password: @Pattern(regexp = User.RE_PASSWORD, message = ErrorCode.USER_PASSWORD_NOT_MATCH_REGEX) Str): Any
    {
        // First, spring will check args. If not pass there regex, raise ConstraintViolationException.
        // Then, we check if username not exists. If username already exists, return ErrorCode with 409 conflict.
        // TODO: 2021/1/22 Using library instead of hand make.
        if (userRepo.existsByUsername(username)) return listOf(ErrorCode.USER_NAME_ALREADY_EXISTS).http(409)
        
        val user = User().apply {
            this.username = username
            passwordMd5 = userToSaltedMd5(username, password) 
        }

        // After save and flush, uuid field will be generated automatically.
        userRepo.saveAndFlush(user)
        return user.uuid
    }

    @PostMapping("/delete")
    @Synchronized
    fun delete(@H username: Str, @H password: Str): Any
    {
        return userRepo.login(username, password) { user ->

            // Remove user from family
            if (user.family != null)
            {
                val family = familyRepo.findByIdOrNull(user.family)
                if (family != null)
                {
                    family.members = family.members.csv().apply { remove(username) }.csv()
                    familyRepo.save(family)
                }
            }

            // Remove user
            userRepo.delete(user)

            // Done
            "Success"
        }
    }

    @PostMapping("/login")
    @Synchronized
    fun login(@H username: Str, @H password: Str): Any
    {
        return userRepo.login(username, password) { it.uuid }
    }

    @PostMapping("/backup/upload")
    fun backupUpload(@H username: Str, @H password: Str, @H config: Str): Any
    {
        return userRepo.login(username, password) { user ->

            user.backup = config
            userRepo.save(user)
            "Success"
        }
    }

    @PostMapping("/backup/download")
    fun backupDownload(@H username: Str, @H password: Str): Any
    {
        return userRepo.login(username, password) { user -> user.backup }
    }
}
