package com.erkang.security;

/**
 * 用户上下文（ThreadLocal）
 */
public class UserContext {
    
    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();
    
    public static void setUser(LoginUser user) {
        USER_HOLDER.set(user);
    }
    
    public static LoginUser getUser() {
        return USER_HOLDER.get();
    }
    
    public static Long getUserId() {
        LoginUser user = getUser();
        return user != null ? user.getUserId() : null;
    }
    
    public static String getUsername() {
        LoginUser user = getUser();
        return user != null ? user.getUsername() : null;
    }
    
    public static void clear() {
        USER_HOLDER.remove();
    }
}
