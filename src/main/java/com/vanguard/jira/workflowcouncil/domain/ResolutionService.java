/*
 ****************************************************************************
 *
 * Copyright (c)2018 The Vanguard Group of Investment Companies (VGI)
 * All rights reserved.
 *
 * This source code is CONFIDENTIAL and PROPRIETARY to VGI. Unauthorized
 * distribution, adaptation, or use may be subject to civil and criminal
 * penalties.
 *
 ****************************************************************************
 Module Description:

 $HeadURL:$
 $LastChangedRevision:$
 $Author:$
 $LastChangedDate:$
*/
package com.vanguard.jira.workflowcouncil.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.opensymphony.workflow.loader.StepDescriptor;

@Component
public class ResolutionService
{
    public static final String BACKUP_DESCRIPTION_SNIPPET = "Jira Workflow Council Add-on backup";

	public static final String INVALID_INPUT_MSG = "Invalid input - workflow cannot be null";

	public static final String WF_COUNCIL_USER = "jira-workflow-council-user";

	@JiraImport
    @Inject
    private WorkflowManager workflowManager;
    
    @JiraImport
    @Inject
    private UserManager userManager;
    
    @Inject
    private ResolvedWorkflowService resolvedWorkflowService;
    
    private Logger logger;
    
    private Long now;
    
    public ResolutionService(WorkflowManager wfManager, UserManager userManager, ResolvedWorkflowService resolvedWFService, Logger logger, Long now)
    {
    	this.workflowManager = wfManager;
    	this.userManager = userManager;
    	this.resolvedWorkflowService = resolvedWFService;
    	this.logger = logger;
    	this.now = now;
    }
    
    //For dependency injection
    @SuppressWarnings("unused")
	private ResolutionService()
    {
    	
    }

    public void addResolutionPostFunctionsTo(JiraWorkflow workflow)
    {
    	if(inputIsInvalid(workflow))
    	{
    		return;
    	}
    	
    	logger().info("Adding resolution post-functions to " + workflow.getName());
    	
    	JiraWorkflow existingBackup = findExistingBackupOf(workflow);
    	
    	try
		{
    		backupCurrentWorkflow(workflow);
			this.resolvedWorkflowService.addResolutionPostFunctionsTo(workflow.getDescriptor(), buildStepColorsMap(workflow));
			saveWorkflow(workflow);
			
	    	logSuccess(workflow, workflow.getName());
		}
		catch(Exception ex)
		{
			logFailure(workflow.getName(), ex);
		}
    	finally
    	{
			purgeOldBackup(existingBackup);
    	}
    }

	private void purgeOldBackup(JiraWorkflow existingBackup)
	{
		if(existingBackup != null)
		{
			try
			{
				logger().info("Purging old backup: " + existingBackup.getName());
				this.workflowManager.deleteWorkflow(existingBackup);
				logger().info("Purging of old backup: " + existingBackup.getName() + " complete.");
			}
			catch(Exception ex)
			{
				logger().error("Could not purge this backup...manual purge required " + existingBackup.getName());
			}
		}
		
	}

	private JiraWorkflow findExistingBackupOf(JiraWorkflow workflow)
	{
		JiraWorkflow existingBackup = null;
		
		for(JiraWorkflow wf : this.workflowManager.getWorkflows())
		{
			if(wf.getDescription().contains(backupWorkflowDescriptionName(workflow.getName())) && !wf.isActive())
			{
				existingBackup = wf;
				break;
			}
		}
		
		return existingBackup;
	}

	private boolean inputIsInvalid(JiraWorkflow workflow)
	{
		if(workflow == null)
		{
			logger().error(INVALID_INPUT_MSG);
			return true;
		}
		else
		{
			return false;
		}		
	}

	private void logFailure(String workflowName, Exception ex)
	{
		logger().error(stackTraceMessageStringFrom(workflowName, ex));
	}

	private void logSuccess(JiraWorkflow workflow, String workflowName)
	{
		logger().info("Completed adding resolution post-functions to " + workflowName);
		logger().info(workflow.getDescriptor().asXML());
	}


	public static String stackTraceMessageStringFrom(String workflowName, Exception e)
	{
		return "There was a problem adding post-functions to " + workflowName + " - " + ExceptionUtils.getStackTrace(e);
	}

	

	private void backupCurrentWorkflow(JiraWorkflow originalWorkflow)
	{
		String workflowName = originalWorkflow.getName();
		this.workflowManager.copyWorkflow(this.userManager.getUserByName(WF_COUNCIL_USER), 
				backupWorkflowName(workflowName, now()), backupWorkflowDescription(workflowName, now()), originalWorkflow);
	}
	
	public static String backupWorkflowName(String workflowName, Long timeStamp)
	{
		return workflowName + " " + timeStamp;
	}
	
	public static String backupWorkflowDescription(String workflowName, Long timeStamp)
	{
		return backupWorkflowDescriptionName(workflowName) + " " + timeStamp;
	}
	
	public static String backupWorkflowDescriptionName(String workflowName)
	{
		return BACKUP_DESCRIPTION_SNIPPET + " of " + workflowName;
	}

	private Map<Integer, StatusCategory> buildStepColorsMap(JiraWorkflow originalWorkflow)
	{
		Map<Integer, StatusCategory> stepColorMap = new HashMap<Integer, StatusCategory>();
		
		List<Status> statusesInWorkflow = originalWorkflow.getLinkedStatusObjects();
		
		for(Status status : statusesInWorkflow)
		{
			StepDescriptor linkedStep = originalWorkflow.getLinkedStep(status);
			
			logger().debug("Step id " + linkedStep.getId() + " has status category " + status.getStatusCategory().getName());
			
			stepColorMap.put(linkedStep.getId(), status.getStatusCategory());
		}
		
		return stepColorMap;
	}

	private void saveWorkflow(JiraWorkflow wf)
	{
		ApplicationUser user = this.userManager.getUserByName(WF_COUNCIL_USER);
   		this.workflowManager.createWorkflow(user, wf);
	}
	
	private Logger logger()
	{
		if(this.logger == null)
		{
			this.logger = LoggerFactory.getLogger(ResolutionService.class);
		}
		return this.logger;
	}
	
	private Long now()
	{
		if(this.now == null)
		{
			return System.currentTimeMillis();
		}
		else
		{
			return this.now;
		}
	}
}
