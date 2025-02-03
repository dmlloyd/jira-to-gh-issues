package io.pivotal.migration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
@ConditionalOnProperty(name = "jira.projectId", havingValue = "MODULES")
public class JBossModulesMigrationConfig {

    @Bean
    public MilestoneFilter milestoneFilter() {
        return fixVersion -> true;
    }

    @Bean
    public IssueProcessor issueProcessor() {
        return new IssueProcessor() {
        };
    }

    @Bean
    public JiraIssueFilter issueFilter() {
        return new JiraIssueFilter() {
        };
    }

    @Bean
    public LabelHandler labelHandler() {

        FieldValueLabelHandler fieldValueHandler = new FieldValueLabelHandler();
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Bug", "bug");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Feature Request", "enhancement");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Enhancement", "enhancement");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Task", "task");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Support Patch", "bug");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Patch", "bug");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "CTS Challenge", "bug");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Release", "task");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Quality Risk", "bug");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Component Upgrade", "dependencies");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Sub-task", "task");
        // "Backport" - ignore

        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Won't Do", "declined");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Cannot Reproduce", "declined");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Can't Do", "declined");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Duplicate", "duplicate");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Not a Bug", "invalid");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Done-Errata", "done");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Obsolete", "declined");
        fieldValueHandler.addMapping(FieldValueLabelHandler.FieldType.RESOLUTION, "Declined", "declined");
        // "Test Pending", "MirrorOrphan" - no label

        // "Resolved", "Closed" -- it should be obvious if issue is closed (distinction not of interest)
        // "Open", "Re Opened" -- it should be obvious from GH timeline
        // "Coding In Progress" -- ignored

        CompositeLabelHandler handler = new CompositeLabelHandler();
        handler.addLabelHandler(fieldValueHandler);

        return handler;
    }
}
