package org.hydev.clock_api.error;


public interface ErrorCode {
    // Missing field in header.
    String USER_NAME_IS_NULL = "A0101";
    String USER_PASSWORD_IS_NULL = "A0102";

    // Field not match regex.
    String USER_NAME_NOT_MATCH_REGEX = "A0111";
    String USER_PASSWORD_NOT_MATCH_REGEX = "A0112";

    // Field already exists.
    String USER_NAME_ALREADY_EXISTS = "A0121";

    // Inner system field is null.
    String INNER_USERNAME_IS_NULL = "B0101";
    String INNER_PASSWORD_MD5_IS_NULL = "B0102";

    // Inner system field not match regex.
    String INNER_USERNAME_NOT_MATCH_REGEX = "B0111";
    String INNER_PASSWORD_MD5_NOT_MATCH_REGEX = "B0112";
}
