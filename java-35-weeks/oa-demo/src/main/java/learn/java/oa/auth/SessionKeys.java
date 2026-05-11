package learn.java.oa.auth;

/** HttpSession 属性名（登录成功后由 {@link learn.java.oa.servlet.LoginServlet} 写入）。 */
public final class SessionKeys {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String DISPLAY_NAME = "displayName";
    /** {@link java.util.Set}{@code <String>}，元素为角色 {@code code}，如 {@code EMPLOYEE}、{@code ADMIN} */
    public static final String ROLE_CODES = "roleCodes";

    private SessionKeys() {}
}
