package org.hydev.clock_api

import org.hydev.clock_api.entity.User
import org.hydev.clock_api.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.RequestHeader



typealias H = RequestHeader
typealias Str = String
typealias Bool = Boolean

fun <T> T.http(code: Int): ResponseEntity<T> = ResponseEntity.status(code).body<T>(this)
fun pinHash(fid: Long, pin: Str) = DigestUtils.md5DigestAsHex("$fid-$pin".toByteArray()).toLowerCase()
fun <T> List<T>.csv() = joinToString(";")
fun Str.csv() = split(";").toMutableList().apply { removeIf { it.isEmpty() } }

/**
 * Create salted hash for user's password
 * Format: "$username + $password".toLowerMd5();
 *
 * @param username Unique username used as a salt
 * @param password Password initial hash
 * @return Salted hash
 */
fun userToSaltedMd5(username: String?, password: String?): String
{
    val beforeMd5 = String.format("%s + %s", username, password)
    return DigestUtils.md5DigestAsHex(beforeMd5.toByteArray()).toLowerCase()
}

/**
 * Login and do callback
 *
 * @param username
 * @param password
 * @param callback
 * @return Execution result
 */
fun UserRepository.login(username: Str, password: Str, callback: (User) -> Any?): Any
{
    // Verify login
    val user: User = queryByUsername(username) ?: return "Username not found".http(404)
    if (user.passwordMd5 != userToSaltedMd5(username, password)) return "Login invalid".http(401)

    return callback(user) ?: "Success"
}
