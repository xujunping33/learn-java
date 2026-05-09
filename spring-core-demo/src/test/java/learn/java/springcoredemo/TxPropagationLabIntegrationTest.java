package learn.java.springcoredemo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import learn.java.springcoredemo.tx.TxPropagationLab;

/** Week22 Day153：事务传播 lab；{@link TxPropagationLab#requiredJoin()} 内部已有 DB 断言。 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class TxPropagationLabIntegrationTest {

    @Autowired
    private TxPropagationLab txPropagationLab;

    @Test
    void requiredJoin_leavesNoSpringTx148RowsAfterInnerFailure() {
        assertDoesNotThrow(() -> txPropagationLab.requiredJoin());
    }
}
