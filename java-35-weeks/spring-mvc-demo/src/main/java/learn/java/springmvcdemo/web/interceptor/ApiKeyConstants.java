package learn.java.springmvcdemo.web.interceptor;

/**
 * Day160：演示用固定密钥（生产应走配置中心或环境变量，勿提交真实密钥）。
 */
public final class ApiKeyConstants {

    public static final String HEADER = "X-Api-Key";
    /** 与 README / smoke-students.sh / cors-demo.html 对齐。 */
    public static final String DEMO_VALUE = "w23-demo-key";

    private ApiKeyConstants() {}
}
