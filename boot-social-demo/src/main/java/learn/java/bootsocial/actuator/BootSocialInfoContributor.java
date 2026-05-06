package learn.java.bootsocial.actuator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class BootSocialInfoContributor implements InfoContributor {

    private final BuildProperties buildProperties;

    public BootSocialInfoContributor(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties.getIfAvailable();
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> svc = new LinkedHashMap<>();
        svc.put("name", "boot-social-demo");
        svc.put("note", "W26 Day179 — actuator health/info");
        builder.withDetail("service", svc);

        if (buildProperties != null) {
            Map<String, Object> build = new LinkedHashMap<>();
            build.put("artifact", buildProperties.getArtifact());
            build.put("version", buildProperties.getVersion());
            build.put("time", buildProperties.getTime().toString());
            builder.withDetail("build", build);
        }
    }
}
