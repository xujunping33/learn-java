package learn.java.bootsocialms.post.ratelimit;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LocalSentinelRules {

    private static final Logger log = LoggerFactory.getLogger(LocalSentinelRules.class);

    @Value("${sentinel.local.qps:0}")
    private double localQps;

    @EventListener(ApplicationReadyEvent.class)
    public void initRules() {
        if (localQps <= 0) {
            return;
        }
        FlowRule rule = new FlowRule();
        rule.setResource("getPostById");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(localQps);

        FlowRuleManager.loadRules(List.of(rule));
        log.warn("Loaded local Sentinel rule: resource=getPostById QPS={}", localQps);
    }
}

