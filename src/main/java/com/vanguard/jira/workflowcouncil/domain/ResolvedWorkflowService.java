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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@Component
public class ResolvedWorkflowService
{
	public static final String CLASS_NAME_KEY = "class.name";
	public static final String FIELD_VALUE_KEY = "field.value";
	public static final String MODULE_KEY = "full.module.key";
	public static final String FIELD_NAME_KEY = "field.name";
	
	public static final String CLASS_TYPE = "class";
	public static final String UPDATE_ISSUE_FIELD_MODULE = "com.atlassian.jira.plugin.system.workflowupdate-issue-field-function";
	public static final String RESOLUTION_FIELD_NAME = "resolution";
	public static final String UPDATE_ISSUE_FUNCTION_CLASS = "com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction";

	public static final String UNRESOLVED = "";
	public static final String DONE_RESOLUTION = "10000";
	public static final String COMPLETE_STATUS_CATEGORY_NAME = "Complete";

	public static final int FRONT = 0;
	
	private Logger logger;
	
	public ResolvedWorkflowService(Logger logger)
	{
		this.logger = logger;
	}
	
	//For dependency injection
	@SuppressWarnings("unused")
	private ResolvedWorkflowService()
	{
		
	}
	
	public void addResolutionPostFunctionsTo(WorkflowDescriptor descriptor, Map<Integer, StatusCategory> stepColorMap) throws InvalidWorkflowDescriptorException
	{
		validateInput(descriptor, stepColorMap);
    	processActions(descriptor.getGlobalActions(), stepColorMap);
    	processActions(descriptor.getCommonActions().values(), stepColorMap);
    	processStepActions(descriptor.getSteps(), stepColorMap);
		descriptor.validate();
	}

	private void validateInput(WorkflowDescriptor descriptor, Map<Integer, StatusCategory> stepColorMap) throws InvalidWorkflowDescriptorException
	{
		if(descriptor == null)
		{
			throw new IllegalArgumentException("Workflow descriptor cannot be null");
		}
		
		descriptor.validate();
		
		if(stepColorMap == null || stepColorMap.isEmpty())
		{
			throw new IllegalArgumentException("Workflow step colors must be provided");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void processActions(Collection actions, Map<Integer, StatusCategory> stepColorMap)
	{
		for(Object actionObj : actions)
		{
			ActionDescriptor currentAction = (ActionDescriptor)actionObj;
			processAction(currentAction, stepColorMap);				
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void processStepActions(List steps, Map<Integer, StatusCategory> stepColorMap)
	{
		for(Object stepObj : steps)
		{
			StepDescriptor currentStep = (StepDescriptor)stepObj;
			
			processActions(currentStep.getActions(), stepColorMap);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void processAction(ActionDescriptor currentAction, Map<Integer, StatusCategory> stepColorMap)
	{
		StatusCategory actionStatusCategory = stepColorMap.get(currentAction.getUnconditionalResult().getStep());
		logger().debug("Action " + currentAction.getName() + " has status category name = " + actionStatusCategory.getName());
		
		if(noResolutionPostFunctionExistsIn(currentAction))
		{
			currentAction.getUnconditionalResult().getPostFunctions().add(FRONT, newResolutionPostFunction(actionStatusCategory));
		}
	}

	private boolean noResolutionPostFunctionExistsIn(ActionDescriptor currentAction)
	{
		for(Object fdObj : currentAction.getUnconditionalResult().getPostFunctions())
		{
			FunctionDescriptor fd = (FunctionDescriptor)fdObj;
			if(fd.getArgs().containsKey(FIELD_NAME_KEY) && fd.getArgs().get(FIELD_NAME_KEY).equals(RESOLUTION_FIELD_NAME))
			{
				return false;
			}

		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private FunctionDescriptor newResolutionPostFunction(StatusCategory actionStatusCategory)
	{
		FunctionDescriptor setResolution = DescriptorFactory.getFactory().createFunctionDescriptor();
    	setResolution.setType(CLASS_TYPE);
    	setResolution.getArgs().put(FIELD_NAME_KEY, RESOLUTION_FIELD_NAME);
    	setResolution.getArgs().put(MODULE_KEY, UPDATE_ISSUE_FIELD_MODULE);
    	
    	if(COMPLETE_STATUS_CATEGORY_NAME.equals(actionStatusCategory.getName()))
    	{
    		setResolution.getArgs().put(FIELD_VALUE_KEY, DONE_RESOLUTION);   
    	}
    	else
    	{
    		setResolution.getArgs().put(FIELD_VALUE_KEY, UNRESOLVED); 
    	}
    	
    	
    	setResolution.getArgs().put(CLASS_NAME_KEY, UPDATE_ISSUE_FUNCTION_CLASS);
		return setResolution;
	}
	
	private Logger logger()
	{
		if(this.logger == null)
		{
			this.logger = LoggerFactory.getLogger(ResolvedWorkflowService.class);
		}
		return this.logger;
	}
}
