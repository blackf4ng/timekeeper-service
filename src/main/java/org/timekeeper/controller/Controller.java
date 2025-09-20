package org.timekeeper.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface Controller {

    default String getUserId(OidcUser user) {
        return user.getSubject();
    }

}
