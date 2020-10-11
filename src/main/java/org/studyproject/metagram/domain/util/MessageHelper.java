package org.studyproject.metagram.domain.util;

import org.studyproject.metagram.domain.User;

public abstract class MessageHelper {
    public static String getAuthorName(User user) {
        return user != null ? user.getUsername() : "none";

    }
}
