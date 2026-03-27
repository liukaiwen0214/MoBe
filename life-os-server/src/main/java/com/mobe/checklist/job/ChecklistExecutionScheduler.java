package com.mobe.checklist.job;

import com.mobe.checklist.service.ChecklistExecutionScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChecklistExecutionScheduler {

    private final ChecklistExecutionScheduleService checklistExecutionScheduleService;

    public ChecklistExecutionScheduler(ChecklistExecutionScheduleService checklistExecutionScheduleService) {
        this.checklistExecutionScheduleService = checklistExecutionScheduleService;
    }

    /**
     * 每天 00:05 自动生成当天清单
     */
    @Scheduled(cron = "0 5 0 * * ?")
    // @Scheduled(cron = "0 */1 * * * ?")
    public void generateTodayChecklistExecutionsJob() {
        checklistExecutionScheduleService.generateTodayChecklistExecutions();
    }

    /**
     * 每天 23:55 自动处理过期数据
     */
    @Scheduled(cron = "0 55 23 * * ?")
    // @Scheduled(cron = "0 6 15 * * ?")
    public void expirePendingChecklistExecutionsJob() {
        checklistExecutionScheduleService.expirePendingChecklistExecutions();
    }
}