package learn.java.bootsocial.mq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "docker"})
public class MqFailureSwitch {

    /** 默认关闭：避免进程启动后未显式调用 dev 接口就出现模拟失败（W30 smoke / 本地调试更安全）。 */
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}

