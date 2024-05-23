package com.devil.fission.machine.example.service.rule;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;

/**
 * FissionMachineRuleListener.
 *
 * @author Devil
 * @date Created in 2024/5/20 9:49
 */
@Slf4j
public class FissionMachineRuleListener implements RuleListener {
    
    @Override
    public void onEvaluationError(Rule rule, Facts facts, Exception exception) {
        log.error("执行规则引擎失败 规则: {} 数据：{}", rule.getName(), facts, exception);
    }
}
