package learn.java.bootsocial.storage;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * W31 Day216：dev-only — 模拟“上传成功但写 DB 失败”，用于验证补偿删除对象。
 */
@Component
@Profile({"dev", "docker"})
public class CoverDbFailureSwitch {

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}

