package org.hydev.clock_api

import org.hamcrest.Matchers
import org.hydev.clock_api.entity.User
import org.hydev.clock_api.error.ErrorCode.*
import org.hydev.clock_api.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.*
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.DigestUtils
import org.springframework.util.LinkedMultiValueMap
import java.util.*
import javax.validation.ConstraintViolationException

// https://stackoverflow.com/questions/59097035/springboottest-vs-webmvctest-datajpatest-service-unit-tests-what-is-the-b

// https://stackoverflow.com/questions/45653753/how-to-tell-spring-boot-to-use-another-db-for-test
// Need RANDOM_PORT to inject TestRestTemplate.
// https://stackoverflow.com/questions/39213531/spring-boot-test-unable-to-inject-testresttemplate-and-mockmvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

// https://stackoverflow.com/questions/45653753/how-to-tell-spring-boot-to-use-another-db-for-test
@ActiveProfiles("test")

// https://spring.io/guides/gs/testing-web/
@AutoConfigureMockMvc
class UserRegisterDeleteNodeTest {
    companion object {
        private const val TEST_NODE = "/user"
        private const val REGISTER_NODE = "${TEST_NODE}/register"
        private const val DELETE_NODE = "${TEST_NODE}/delete"

        private const val H_USERNAME = "username"
        private const val V_USERNAME = "vanilla"

        private const val H_PASSWORD = "password"
        private const val V_PASSWORD = "P1ssW0rd"
        private val V_PASSWORD_MD5 = DigestUtils.md5DigestAsHex(V_PASSWORD.toByteArray())

        // https://stackoverflow.com/questions/37615731/java-regex-for-uuid
        private const val R_UUID = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    // Post to register node with headers, expect 406 and ErrorCodes.
    // todo: Using List instead of Array.
    private fun pTRWHsE406AECs(headerMap: Map<String, String>, expectedECList: Array<String>) {
        val tempMultiValueMap = LinkedMultiValueMap<String, String>()
        headerMap.forEach { tempMultiValueMap[it.key] = listOf(it.value) }
        val httpEntity = HttpEntity<String>(tempMultiValueMap)

        // Using exchange to custom headers, etc. Args: (node, method, headers, forObject).
        // https://stackoverflow.com/questions/16781680/http-get-with-headers-using-resttemplate
        val responseEntity =
            restTemplate.exchange(REGISTER_NODE, HttpMethod.POST, httpEntity, Array<String>::class.java)

        // Expect http status is 406 NOT ACCEPTABLE, and ErrorCode array are same.
        assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.statusCode)
        assertArrayEquals(expectedECList, responseEntity.body)
    }

    @Test
    // [A0101, A0102, A0101 + A0102] M1 * 2 + M2.
    fun testWhenMissingField() {
        pTRWHsE406AECs(mapOf(H_PASSWORD to V_PASSWORD), arrayOf(USER_NAME_IS_NULL))
        pTRWHsE406AECs(mapOf(H_USERNAME to V_USERNAME), arrayOf(USER_PASSWORD_IS_NULL))
        pTRWHsE406AECs(mapOf(), arrayOf(USER_NAME_IS_NULL, USER_PASSWORD_IS_NULL))
    }

    @Test
    // [A0111, A0112, A0111 + A0112] W1 * 2 + W2.
    fun testWhenNotMatchRegex() {
        pTRWHsE406AECs(mapOf(H_USERNAME to "", H_PASSWORD to V_PASSWORD), arrayOf(USER_NAME_NOT_MATCH_REGEX))
        pTRWHsE406AECs(mapOf(H_USERNAME to V_USERNAME, H_PASSWORD to ""), arrayOf(USER_PASSWORD_NOT_MATCH_REGEX))
        pTRWHsE406AECs(
            mapOf(H_USERNAME to "", H_PASSWORD to ""),
            arrayOf(USER_NAME_NOT_MATCH_REGEX, USER_PASSWORD_NOT_MATCH_REGEX)
        )
    }

    @Test
    // [A0121] Insert user, check it if already exists.
    fun testWhenUserAlreadyExists() {
        mockMvc.perform(post(REGISTER_NODE).header(H_USERNAME, V_USERNAME).header(H_PASSWORD, V_PASSWORD))
            .andExpect(status().isOk)
            .andExpect(content().string(Matchers.matchesPattern(R_UUID)))
            .andDo { result: MvcResult ->
                // Notice: inserted user should be delete.
                val tempUuid = result.response.contentAsString
                mockMvc.perform(post(REGISTER_NODE).header(H_USERNAME, V_USERNAME).header(H_PASSWORD, V_PASSWORD))
                    .andExpect(status().isNotAcceptable)
                    .andExpect(content().json(String.format("[\"%s\"]", USER_NAME_ALREADY_EXISTS)))
                userRepository.deleteById(tempUuid)
            }
    }

    // -- Begin inner system test.
    // Get ConstraintViolationException contains error messages set.
    private fun ConstraintViolationException.getCveMsgSet(): Set<String> {
        return this.constraintViolations.map { it.message }.toSet()
    }

    // Insert custom User, and assert CVE with ErrorCodes.
    private fun iUACVEWECs(user: User, cveSet: Set<String>) {
        // Expect throw error, and message set = given cve set.
        val cve = assertThrows(ConstraintViolationException::class.java) { userRepository.saveAndFlush(user) }
        assertEquals(cveSet, cve.getCveMsgSet())
    }

    @Test
    // [B0101, B0102, B0101 + B0102] M1 * 2 + M2.
    fun innerTestWhenFieldIsNull() {
        iUACVEWECs(User().apply { passwordMd5 = V_PASSWORD_MD5 }, setOf(INNER_USERNAME_IS_NULL))
        iUACVEWECs(User().apply { username = V_USERNAME }, setOf(INNER_PASSWORD_MD5_IS_NULL))
        iUACVEWECs(User(), setOf(INNER_USERNAME_IS_NULL, INNER_PASSWORD_MD5_IS_NULL))
    }

    @Test
    // [B0111, B0112, B0111 + B0112] W1 * 2 + W2.
    fun innerTestWhenFieldNotMatchRegex() {
        iUACVEWECs(User().apply { username = ""; passwordMd5 = V_PASSWORD_MD5 }, setOf(INNER_USERNAME_NOT_MATCH_REGEX))
        iUACVEWECs(User().apply { username = V_USERNAME; passwordMd5 = "" }, setOf(INNER_PASSWORD_MD5_NOT_MATCH_REGEX))
        iUACVEWECs(
            User().apply { username = ""; passwordMd5 = "" },
            setOf(INNER_USERNAME_NOT_MATCH_REGEX, INNER_PASSWORD_MD5_NOT_MATCH_REGEX)
        )
    }

    // Post to delete node with headers and expect HttpStatus.
    private fun pTDWHsAEHS(headerMap: Map<String, String>, expectedHttpStatus: HttpStatus) {
        val tempMultiValueMap = LinkedMultiValueMap<String, String>()
        headerMap.forEach { tempMultiValueMap[it.key] = listOf(it.value) }
        val httpEntity = HttpEntity<String>(tempMultiValueMap)

        val responseEntity = restTemplate.exchange(DELETE_NODE, HttpMethod.POST, httpEntity, String::class.java)
        assertEquals(expectedHttpStatus, responseEntity.statusCode)
    }

    @Test
    fun testDeleteUser() {
        mockMvc.perform(post(REGISTER_NODE).header(H_USERNAME, V_USERNAME).header(H_PASSWORD, V_PASSWORD))
            .andExpect(status().isOk)
            .andExpect(content().string(Matchers.matchesPattern(R_UUID)))
            .andDo {
                // Missing headers, 400 bad request.
                pTDWHsAEHS(mapOf(H_USERNAME to ""), HttpStatus.BAD_REQUEST)
                // Username not exist, 404 not found.
                pTDWHsAEHS(mapOf(H_USERNAME to "", H_PASSWORD to ""), HttpStatus.NOT_FOUND)
                // Username exist, but password not match. 401 unauthorized.
                pTDWHsAEHS(mapOf(H_USERNAME to V_USERNAME, H_PASSWORD to ""), HttpStatus.UNAUTHORIZED)
                // Username & password matched, delete user and 200 ok.
                pTDWHsAEHS(mapOf(H_USERNAME to V_USERNAME, H_PASSWORD to V_PASSWORD), HttpStatus.OK)

                // And assert user is gone.
                assertEquals(false, userRepository.existsByUsername(V_USERNAME))
            }
    }
}
