package com.stocat.authapi.config;

import java.security.Key;

public interface JwtSecretProvider {

    /**
     * Return jwt signing key; initialize lazily if needed.
     *
     * @return signing key
     */
    Key getSigningKey();
}
