package org.hydev.clock_api.error;


public interface ErrorCode {
    // Field not match regex.
    String USER_NAME_NOT_MATCH_REGEX = "A0101";
    String USER_PASSWORD_NOT_MATCH_REGEX = "A0102";

    // Field already exists.
    String USER_NAME_ALREADY_EXISTS = "A0111";

    // Inner system field is null.
    String INNER_USERNAME_IS_NULL = "B0101";
    String INNER_PASSWORD_MD5_IS_NULL = "B0102";

    // Inner system field not match regex.
    String INNER_USERNAME_NOT_MATCH_REGEX = "B0111";
    String INNER_PASSWORD_MD5_NOT_MATCH_REGEX = "B0112";
}
