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

import static com.vanguard.jira.workflowcouncil.domain.CommonTestUtils.*;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class ResolvedWorkflowServiceTest
{	
	private static ResolvedWorkflowService objectUnderTest;

	private static final StepDescriptor ANY_STEP_DESCRIPTOR = createStepDescriptor(ANY_NUMBER);
	private static final String SOME_NOT_DONE_RESOLUTION = randomString();

	private static final Integer ACTION_ID_ALPHA = randomInteger();
	private static final Integer ACTION_ID_BETA = ACTION_ID_ALPHA + ACTION_ID_ALPHA;
	private static final Integer ACTION_ID_THETA = ACTION_ID_ALPHA + ACTION_ID_BETA;
	private static final Integer ACTION_ID_RHO = ACTION_ID_ALPHA + ACTION_ID_THETA;
	private static final Integer ACTION_ID_PHI = ACTION_ID_ALPHA + ACTION_ID_RHO;
	
	private static final Logger mockLogger = LoggerFactory.getLogger(ResolutionService.class);
	
	@BeforeClass
	public static void beforeAll()
	{
		//Needed because the status categories used in thenReturn() are themselves mocks, so they need to be setup first
		initStatusColors();
	}
	
	@Before
	public void setUp()
	{
		objectUnderTest = new ResolvedWorkflowService(mockLogger);
		
	}
	
	@After
	public void tearDown()
	{
		objectUnderTest = null;
	}
	
	@Test
	public void testThatPostFunctionAddedToSingleBlueGlobalAction() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithASingleBlueGlobalAction();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithASingleBlueStatus();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionAddedToClearResolution(workflow);
	}
	
	@Test
	public void testThatPostFunctionsAddedToOneOfEachColor() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithOneOfEachColorAction();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithAllThreeColors();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionsAreAddedToAllThree(workflow);
	}

	@Test
	public void testThatPostFunctionsAddedToCommonActions() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithCommonActions();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithAllThreeColors();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionsAreAddedToGlobalAndCommonAction(workflow);
	}


	@Test
	public void testThatPostFunctionsAddedToStepActions() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithStepActions();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithAllThreeColors();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionsAreAddedToGlobalAndStepActions(workflow);
	}
	

	@Test
	public void testThatPostFunctionsAddedToStepActionsWithReferences() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithStepActionsWithReferences();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithAllThreeColors();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionsAreAddedToGlobalAndAllStepActions(workflow);
	}
	
	@Test
	public void testThatActionAlreadyWithPostFunctionIsUnchanged() throws Exception
	{
		WorkflowDescriptor workflow = givenAWorkflowDescriptorWithASingleBlueGlobalActionWithAPostFunction();
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithASingleBlueStatus();
				
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		thenPostFunctionIsUnchanged(workflow);
	}

	
	
	private Map<Integer, StatusCategory> givenAnyNonEmptyStatusColorMap() {
		
		Map<Integer, StatusCategory> nonEmptyMap = new HashMap<Integer, StatusCategory>();
		
		nonEmptyMap.put(ANY_NUMBER, BLUE);
		
		return nonEmptyMap;
	}

	private Map<Integer, StatusCategory> givenEmptyStatusColorMap() {
		
		Map<Integer, StatusCategory> EmptyMap = new HashMap<>();
		
		return EmptyMap;
	}
	
	private Map<Integer, StatusCategory> givenNullColormap() {
		
		return null;
	}

	private WorkflowDescriptor givenANullWorkflowDescriptor() {
		
		return null;
	}
	
	private WorkflowDescriptor givenAEmptyWorkflowDescriptor() {
		
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		WorkflowDescriptor Workflow = descriptorFactory.createWorkflowDescriptor();
		
		return Workflow;
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testWorkflowInput() throws Exception
	{
		WorkflowDescriptor nullWorkflow = givenANullWorkflowDescriptor();
		Map<Integer, StatusCategory> anyNotEmptyColorMap = givenAnyNonEmptyStatusColorMap();
		
		whenResolvedWFServiceIsCalled(nullWorkflow, anyNotEmptyColorMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullColorMapInput() throws Exception
	{
		WorkflowDescriptor Workflow = givenAWorkflowDescriptorWithASingleBlueGlobalActionWithAPostFunction();
		Map<Integer, StatusCategory> nullColorMap = givenNullColormap();
		
		whenResolvedWFServiceIsCalled(Workflow, nullColorMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testWorkflowColorMapNotEmpty() throws Exception
	{
		WorkflowDescriptor Workflow = givenAWorkflowDescriptorWithASingleBlueGlobalActionWithAPostFunction();
		Map<Integer, StatusCategory> EmptyColorMap = givenEmptyStatusColorMap();
		
		whenResolvedWFServiceIsCalled(Workflow, EmptyColorMap);
	}
	
	@Test(expected = InvalidWorkflowDescriptorException.class)
	public void testWorkflowValidateFunction() throws Exception
	{
		WorkflowDescriptor Workflow = givenAEmptyWorkflowDescriptor();
		Map<Integer, StatusCategory> anyNotEmptyColorMap = givenAnyNonEmptyStatusColorMap();
		
		whenResolvedWFServiceIsCalled(Workflow, anyNotEmptyColorMap);
	}
	
	
	@Test
	public void testPostFunctionFirst() throws Exception
	{
		WorkflowDescriptor workflow = createWorkflowDescriptorWithBlueAndGreenGlobalActions();
		
		Map<Integer, StatusCategory> stepColorMap = givenAStepMapWithAllThreeColors();	
		
		
		workflow = whenSetupMockData(workflow, stepColorMap);
		
		whenResolvedWFServiceIsCalled(workflow, stepColorMap);
		
		
		assertClearResolutionPostFunction((ActionDescriptor)workflow.getGlobalActions().get(objectUnderTest.FRONT)); 
		
	}
	
	private WorkflowDescriptor whenSetupMockData(WorkflowDescriptor workflow, Map<Integer, StatusCategory> stepColorMap){
		
		ActionDescriptor currentAction = (ActionDescriptor)workflow.getGlobalActions().get(objectUnderTest.FRONT);
			
		StatusCategory actionStatusCategory = stepColorMap.get(currentAction.getUnconditionalResult().getStep());	
		
		// setting up mock data 
		addMockResolutionPostFunction(currentAction, actionStatusCategory);	
		
		return workflow; 
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void addMockResolutionPostFunction(ActionDescriptor currentAction, StatusCategory actionStatusCategory )
	{
		for(int i = 0; i<3; i++){		
			currentAction.getUnconditionalResult().getPostFunctions().add(RandomResolutionPostFunction(actionStatusCategory));
			}			
	}

	
	@SuppressWarnings("unchecked")
	private FunctionDescriptor RandomResolutionPostFunction(StatusCategory actionStatusCategory)
	{
		FunctionDescriptor setResolution = DescriptorFactory.getFactory().createFunctionDescriptor();
    	setResolution.setType("objectUnderTest.test class " + randomString());
    	setResolution.getArgs().put("objectUnderTest.FIELD_NAME_KEY " + randomString(), "objectUnderTest.RESOLUTION_FIELD_NAME "+ randomString() );
    	setResolution.getArgs().put("objectUnderTest.MODULE_KEY "+ randomString(), "objectUnderTest.UPDATE_ISSUE_FIELD_MODULE "+ randomString());
    	

    		setResolution.getArgs().put(objectUnderTest.FIELD_VALUE_KEY, objectUnderTest.UNRESOLVED);   
    	
    	setResolution.getArgs().put(objectUnderTest.CLASS_NAME_KEY + randomString()  , objectUnderTest.UPDATE_ISSUE_FUNCTION_CLASS + randomString());
		return setResolution;
	}

	
	private void thenPostFunctionIsUnchanged(WorkflowDescriptor workflow)
	{
		FunctionDescriptor postFunction = (FunctionDescriptor)workflow.getAction(ACTION_ID_ALPHA).getUnconditionalResult().getPostFunctions().get(objectUnderTest.FRONT);
		assertEquals("There is more than 1 resolution post function", 1, workflow.getAction(ACTION_ID_ALPHA).getUnconditionalResult().getPostFunctions().size());
		
		assertEquals("Post function resolution field name arg does not match expected", 
				objectUnderTest.RESOLUTION_FIELD_NAME, postFunction.getArgs().get(objectUnderTest.FIELD_NAME_KEY));
		assertEquals("Post function resolution field value does not match expected", 
				SOME_NOT_DONE_RESOLUTION, postFunction.getArgs().get(objectUnderTest.FIELD_VALUE_KEY));
	}

	@SuppressWarnings("unchecked")
	private WorkflowDescriptor givenAWorkflowDescriptorWithASingleBlueGlobalActionWithAPostFunction()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithInitAction();

		ActionDescriptor blueActionDescriptor = createActionLinkedToStep(ACTION_ID_ALPHA, STEP_ID_ALPHA);
		FunctionDescriptor resolutionPostFunction = createExistingResolutionPostFunction();
		blueActionDescriptor.getUnconditionalResult().getPostFunctions().add(resolutionPostFunction);

		
		workflowDescriptor.addGlobalAction(blueActionDescriptor);
	
		return workflowDescriptor;
	}

	private WorkflowDescriptor createWorkflowDescriptorWithInitAction()
	{
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		
		ActionDescriptor initialActionDescriptor = createActionLinkedToStep(ACTION_ID_BETA, STEP_ID_ALPHA);
		
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithInitAction(descriptorFactory,
		        initialActionDescriptor);
		return workflowDescriptor;
	}
	
	@SuppressWarnings("unchecked")
	private FunctionDescriptor createExistingResolutionPostFunction()
	{
		FunctionDescriptor resolutionPostFunction = DescriptorFactory.getFactory().createFunctionDescriptor();
		resolutionPostFunction.getArgs().put(objectUnderTest.FIELD_NAME_KEY, objectUnderTest.RESOLUTION_FIELD_NAME);
		resolutionPostFunction.getArgs().put(objectUnderTest.FIELD_VALUE_KEY, SOME_NOT_DONE_RESOLUTION);
		return resolutionPostFunction;
	}

	private void thenPostFunctionsAreAddedToGlobalAndAllStepActions(WorkflowDescriptor workflow)
	{
		assertBlueAndGreenGlobalActionPostFunctions(workflow);
		assertClearResolutionPostFunction(workflow.getAction(ACTION_ID_BETA));
		assertClearResolutionPostFunction((ActionDescriptor)workflow.getCommonActions().get(ACTION_ID_PHI));
	}

	@SuppressWarnings("unchecked")
	private WorkflowDescriptor givenAWorkflowDescriptorWithStepActionsWithReferences()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithBlueGreenGlobalYellowStep();
		ActionDescriptor commonAction = createActionLinkedToStep(ACTION_ID_PHI, STEP_ID_BETA);
		workflowDescriptor.addCommonAction(commonAction);
		
		StepDescriptor stepDescriptor = createStepDescriptor(STEP_ID_PHI);
		stepDescriptor.getCommonActions().add(ACTION_ID_PHI);
		stepDescriptor.setParent(workflowDescriptor);
		
		workflowDescriptor.addStep(stepDescriptor);
		
		return workflowDescriptor;
	}

	private WorkflowDescriptor givenAWorkflowDescriptorWithStepActions()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithBlueGreenGlobalYellowStep();
	
		return workflowDescriptor;
	}

	@SuppressWarnings("unchecked")
	private WorkflowDescriptor createWorkflowDescriptorWithBlueGreenGlobalYellowStep()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithBlueAndGreenGlobalActions();
		
		ActionDescriptor yellowGlobalActionDescriptor = createActionLinkedToStep(ACTION_ID_BETA, STEP_ID_BETA);
		
		
		StepDescriptor stepDescriptor = createStepDescriptor(STEP_ID_RHO);
		stepDescriptor.getActions().add(yellowGlobalActionDescriptor);
		
		workflowDescriptor.addStep(stepDescriptor);
		
		
		return workflowDescriptor;
	}

	private void thenPostFunctionsAreAddedToGlobalAndStepActions(WorkflowDescriptor workflow)
	{
		thenPostFunctionsAreAddedToAllThree(workflow);
	}

	private void thenPostFunctionsAreAddedToGlobalAndCommonAction(WorkflowDescriptor workflow)
	{
		assertBlueAndGreenGlobalActionPostFunctions(workflow);
		assertClearResolutionPostFunction((ActionDescriptor)workflow.getCommonActions().get(ACTION_ID_BETA));
	}

	private void assertBlueAndGreenGlobalActionPostFunctions(WorkflowDescriptor workflow)
	{
		assertClearResolutionPostFunction(workflow.getAction(ACTION_ID_ALPHA));
		assertDoneResolutionPostFunction(workflow.getAction(ACTION_ID_THETA));
	}

	private WorkflowDescriptor givenAWorkflowDescriptorWithCommonActions()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithBlueAndGreenGlobalActions();
		
		ActionDescriptor yellowGlobalActionDescriptor = createActionLinkedToStep(ACTION_ID_BETA, STEP_ID_BETA);
		workflowDescriptor.addCommonAction(yellowGlobalActionDescriptor);

		return workflowDescriptor;
	}

	private WorkflowDescriptor createWorkflowDescriptorWithBlueAndGreenGlobalActions()
	{
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		ActionDescriptor blueGlobalActionDescriptor = createActionLinkedToStep(ACTION_ID_ALPHA, STEP_ID_ALPHA); 
		ActionDescriptor greenGlobalActionDescriptor = createActionLinkedToStep(ACTION_ID_THETA, STEP_ID_THETA);
		ActionDescriptor initialActionDescriptor = createActionLinkedToStep(ACTION_ID_RHO, STEP_ID_ALPHA);
		
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithInitAction(descriptorFactory,
		        initialActionDescriptor);
		
		workflowDescriptor.addGlobalAction(blueGlobalActionDescriptor);
		workflowDescriptor.addGlobalAction(greenGlobalActionDescriptor);
		return workflowDescriptor;
	}

	private void thenPostFunctionsAreAddedToAllThree(WorkflowDescriptor workflow)
	{
		assertBlueAndGreenGlobalActionPostFunctions(workflow);
		assertClearResolutionPostFunction(workflow.getAction(ACTION_ID_BETA));
	}

	private Map<Integer, StatusCategory> givenAStepMapWithAllThreeColors()
	{
		Map<Integer, StatusCategory> stepColorMap = new HashMap<Integer, StatusCategory>();
		stepColorMap.put(STEP_ID_ALPHA, BLUE);
		stepColorMap.put(STEP_ID_BETA, YELLOW);
		stepColorMap.put(STEP_ID_THETA, GREEN);
		
		return stepColorMap;
	}

	private WorkflowDescriptor givenAWorkflowDescriptorWithOneOfEachColorAction()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithBlueAndGreenGlobalActions();
		
		ActionDescriptor yellowGlobalActionDescriptor = createActionLinkedToStep(ACTION_ID_BETA, STEP_ID_BETA);
		workflowDescriptor.addGlobalAction(yellowGlobalActionDescriptor);
	
		return workflowDescriptor;
	}

	private void thenPostFunctionAddedToClearResolution(WorkflowDescriptor workflow)
	{
		assertClearResolutionPostFunction(workflow.getAction(ACTION_ID_ALPHA));
	}
	
	private void assertResolutionPostFunction(FunctionDescriptor postFunction)
	{
		assertEquals("Post function class does not match expected", 
				objectUnderTest.CLASS_TYPE, postFunction.getType());
		
		assertEquals("Post function update issue field function class value does not match expected", 
				objectUnderTest.UPDATE_ISSUE_FUNCTION_CLASS, postFunction.getArgs().get(objectUnderTest.CLASS_NAME_KEY));
		assertEquals("Post function update issue field module does not match expected", 
				objectUnderTest.UPDATE_ISSUE_FIELD_MODULE, postFunction.getArgs().get(objectUnderTest.MODULE_KEY));
		assertEquals("Post function resolution field name arg does not match expected", 
				objectUnderTest.RESOLUTION_FIELD_NAME, postFunction.getArgs().get(objectUnderTest.FIELD_NAME_KEY));
	}
	
	private void assertDoneResolutionPostFunction(ActionDescriptor action)
	{
		FunctionDescriptor firstPostFunction = (FunctionDescriptor)action.getUnconditionalResult().getPostFunctions().get(objectUnderTest.FRONT);
		
		assertResolutionPostFunction(firstPostFunction);
		
		assertEquals("First post function resolution value does not match expected", 
				objectUnderTest.DONE_RESOLUTION, firstPostFunction.getArgs().get(objectUnderTest.FIELD_VALUE_KEY));
	}

	private void assertClearResolutionPostFunction(ActionDescriptor action)
	{
		FunctionDescriptor firstPostFunction = (FunctionDescriptor)action.getUnconditionalResult().getPostFunctions().get(objectUnderTest.FRONT);
		
		assertResolutionPostFunction(firstPostFunction);
		
		assertEquals("First post function resolution value does not match expected", 
				objectUnderTest.UNRESOLVED, firstPostFunction.getArgs().get(objectUnderTest.FIELD_VALUE_KEY));
	}

	private void whenResolvedWFServiceIsCalled(WorkflowDescriptor workflow, Map<Integer, StatusCategory> stepColorMap) throws InvalidWorkflowDescriptorException
	{
		objectUnderTest.addResolutionPostFunctionsTo(workflow, stepColorMap);
		
	}

	private Map<Integer, StatusCategory> givenAStepMapWithASingleBlueStatus()
	{
		Map<Integer, StatusCategory> stepColorMap = new HashMap<Integer, StatusCategory>();
		stepColorMap.put(STEP_ID_ALPHA, BLUE);
		
		return stepColorMap;
	}

	private WorkflowDescriptor givenAWorkflowDescriptorWithASingleBlueGlobalAction()
	{
		WorkflowDescriptor workflowDescriptor = createWorkflowDescriptorWithInitAction();

		ActionDescriptor blueActionDescriptor = createActionLinkedToStep(ACTION_ID_ALPHA, STEP_ID_ALPHA);
		workflowDescriptor.addGlobalAction(blueActionDescriptor);
	
		return workflowDescriptor;
	}

	private WorkflowDescriptor createWorkflowDescriptorWithInitAction(DescriptorFactory descriptorFactory,
	        ActionDescriptor initialActionDescriptor)
	{
		WorkflowDescriptor workflowDescriptor = descriptorFactory.createWorkflowDescriptor();
		workflowDescriptor.addInitialAction(initialActionDescriptor);
		workflowDescriptor.addStep(ANY_STEP_DESCRIPTOR);
		return workflowDescriptor;
	}

	private static StepDescriptor createStepDescriptor(Integer stepId)
	{
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		StepDescriptor stepDescriptor = descriptorFactory.createStepDescriptor();
		stepDescriptor.setId(stepId);
		stepDescriptor.setName(ANY_STRING);
		return stepDescriptor;
	}
	
	private ResultDescriptor createResultLinkedToStep(Integer stepId)
	{
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		ResultDescriptor result = descriptorFactory.createResultDescriptor();
		result.setStep(stepId);
		result.setStatus(ANY_STRING);
		
		return result;
	}
	
	private ActionDescriptor createActionLinkedToStep(Integer actionId, Integer stepId)
	{
		DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
		
		ActionDescriptor actionDescriptor = descriptorFactory.createActionDescriptor();
		actionDescriptor.setUnconditionalResult(createResultLinkedToStep(stepId));
		actionDescriptor.setId(actionId);
		actionDescriptor.setName(ANY_STRING);
		
		return actionDescriptor;
	}
	
}
