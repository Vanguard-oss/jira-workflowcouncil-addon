## Jira Workflow Council Add-On

The Jira Workflow Council Add-On responds to the event of a project admin publishing a draft workflow in Jira Software. It will examine the workflow’s transitions and for each transition that does not have a post-function that sets or clears the Resolution field, it will add the correct post-function according to the status category of the transition’s destination status. For example, if a transition’s destination status is "Delivered" and this has a status category of "Done", then the add-on will add a post-function that sets the Resolution to "Done". If a transition’s destination status is "Not Started" and this has a status category of "To Do", then the add-on will add a post-function that clears the Resolution.

## Why was this add-on created?

Starting in [Jira Software 7.3](https://confluence.atlassian.com/jirasoftware/jira-software-7-3-x-release-notes-861181590.html), project admins are able to edit any workflows that they are not shared with other projects. However, they are not allowed to edit any transitions they create in workflows. This is significant because keeping the Resolution issue field in sync with an issue's workflow status is critical to keeping the reports, dashboards, and issue statistics within Jira Software accurate and telling the same story. For example, if an issue has a status of "Done" and a Resolution of Unresolved, different reports within Jira Software will show or not show this issue, depending on whether the report is looking for unresolved issues, or issues with a "Done" status. It is considered a best practice to keep these 2 fields in agreement.
 
To date, Atlassian has not provided a way for users to both set and clear the Resolution field outside of a workflow transition, nor have they provided a way for project admins to self-serve editing workflow transitions to manipulate the Resolution field. This task, out of the box, is still a job for system administrators. Without a solution, this nearly completely cancels out the intended benefits of extended project administration. This pain is felt throughout the Jira Software community ([JRASERVER-62881](https://jira.atlassian.com/browse/JRASERVER-62881) & [JRASERVER-65574](https://jira.atlassian.com/browse/JRASERVER-65574)) and some admins have even indicated that the gap led them to disable the functionality entirely:
 
 
>Another item to note - is the Resolution system field given special treatment?  We had lots of issues in the past where a JIRA novice was given admin rights, put the Resolution field onto a screen (like the default Edit screen), and then suddenly any time an issue was edited, the Resolution was set to Fixed/Done even if the issue was still in a status like OPEN or IN PROGRESS, which created all kinds of havoc for our Filters
 
>Not much use of this functionality indeed if the properties of the transition cannot be changed by the project admin. That is the whole point of having workflow transitions. You want to be able to control what happens during this transition not just add it. Not sure what the requirements are that lead to this feature being implemented but it does not seem to meet basic workflow editing requirements. Pity, I hoped this feature would have enabled us to handover some basic workflow editing privileges to our project admins,.....
 
 
When Vanguard was working on updating our Jira Software instance from 7.1.6 to 7.11.0, this same challenge was faced. After an extensive period of research & experimentation on existing possible solutions, the team reached out to Atlassian for official guidance. Atlassian indicated that while they are aware of the gap, they do not have immediate plans to provide a solution until at least after Jira Software 8.0.0 next year. The team decided to create a custom add-on, the Jira Workflow Council Add-On, to solve the problem. 



## How to use this add-on

* Simply build this add-on using the Atlassian SDK using the following Maven goals: 
```
clean install deploy
```

* Once the JAR file is created, install the add-on in your Jira Software instance. 
* Create a user with the username "jira-workflow-council-user" to be the author of the workflow changes.
* Enable logging, if desired, for the following packages (either temporarily via the admin screen, or permanently by editing Jira Software's server logging configuration).
  * com.vanguard.jira.workflowcouncil.app
  * com.vanguard.jira.workflowcouncil.domain

## Known issues

At times, we have observed that the add-on fails to start upon a restart of Jira Software with the following error.
```
    ********************************************************************************************************************************************************************************************************
    ___ FAILED PLUGIN REPORT _____________________
     
    1 plugin failed to load during JIRA startup.
     
        'com.vanguard.jira.workflowcouncil.jira-workflowcouncil-addon' - 'Jira Workflow Council Add-on'  failed to load.
                Error creating bean with name 'resolutionService': Injection of autowired dependencies failed; nested exception is org.springframework.beans.factory.BeanCreationException: Could not autowire field: private com.atlassian.j
ira.workflow.WorkflowManager com.vanguard.jira.workflowcouncil.domain.ResolutionService.workflowManager; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type [com.atlassian.jira.
workflow.WorkflowManager] found for dependency: expected at least 1 bean which qualifies as autowire candidate for this dependency. Dependency annotations: {@com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport(value=), @jav
ax.inject.Inject()}
                        Could not autowire field: private com.atlassian.jira.workflow.WorkflowManager com.vanguard.jira.workflowcouncil.domain.ResolutionService.workflowManager; nested exception is org.springframework.beans.factory.NoSuc
hBeanDefinitionException: No qualifying bean of type [com.atlassian.jira.workflow.WorkflowManager] found for dependency: expected at least 1 bean which qualifies as autowire candidate for this dependency. Dependency annotations: {@com.at
lassian.plugin.spring.scanner.annotation.imports.JiraImport(value=), @javax.inject.Inject()}
                                No qualifying bean of type [com.atlassian.jira.workflow.WorkflowManager] found for dependency: expected at least 1 bean which qualifies as autowire candidate for this dependency. Dependency annotations: {@
com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport(value=), @javax.inject.Inject()}
     
                It was loaded from .../application-data/jira/plugins/installed-plugins/plugin_2762737362126248886_jira-workflowcouncil-addon-1.0-M20180920-01.jar
     
    ********************************************************************************************************************************************************************************************************
```
To resolve this, restart your instance. We are working on a fix and will publish the fix here when identified.

In order to find out who has published workflow in a period when the add-on is disabled, you can use the following command.
```
grep "publishDraft" ${JIRA_HOME}/log/atlassian-jira.log
```