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
import static com.vanguard.jira.workflowcouncil.domain.ResolutionService.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class ResolutionServiceTest
{
	private static final WorkflowDescriptor ANY_WF_DESCRIPTOR = DescriptorFactory.getFactory().createWorkflowDescriptor();
	private static final Status A_GREEN_STATUS = mock(Status.class);
	private static final Status A_YELLOW_STATUS = mock(Status.class);
	private static final Status A_BLUE_STATUS = mock(Status.class);
	private static final ApplicationUser SOME_APP_USER = mock(ApplicationUser.class);
	private static final Long FIXED_TIME = System.currentTimeMillis();
	private static final RuntimeException SOME_RTE = new RuntimeException();
	private static final IllegalArgumentException SOME_ILLEGAL_ARG_EX = new IllegalArgumentException();
	private static final JiraWorkflow INACTIVE_BACKUP_WORKFLOW = mock(JiraWorkflow.class);
	private static final JiraWorkflow AN_ACTIVE_WF_COUNCIL_WORKFLOW = mock(JiraWorkflow.class);
	private static final JiraWorkflow AN_ACTIVE_WORKFLOW = mock(JiraWorkflow.class);
	private static final JiraWorkflow AN_INACTIVE_WORKFLOW = mock(JiraWorkflow.class);
	private static final JiraWorkflow INACTIVE_BACKUP_WORKFLOW_ALPHA = mock(JiraWorkflow.class);
	private static final JiraWorkflow INACTIVE_BACKUP_WORKFLOW_BETA = mock(JiraWorkflow.class);
	
	private static StepDescriptor blueStepDescriptor;
	private static StepDescriptor yellowStepDescriptor;
	private static StepDescriptor greenStepDescriptor;

	
	private static WorkflowManager mockWorkflowManager;
	private static UserManager mockUserManager;
	private static ResolvedWorkflowService mockResolvedWFService;
	private static Logger mockLogger; 
	
	private ResolutionService objectUnderTest;
	
	@BeforeClass
	public static void beforeAll()
	{
		//Needed because the status categories used in thenReturn() are themselves mocks, so they need to be setup first
		initStatusColors();
		when(A_BLUE_STATUS.getStatusCategory()).thenReturn(BLUE); 
		when(A_YELLOW_STATUS.getStatusCategory()).thenReturn(YELLOW);
		when(A_GREEN_STATUS.getStatusCategory()).thenReturn(GREEN); 

		when(INACTIVE_BACKUP_WORKFLOW.getDescription()).thenReturn(backupWorkflowDescriptionName(ANY_STRING));
		when(INACTIVE_BACKUP_WORKFLOW.isActive()).thenReturn(false);

		when(INACTIVE_BACKUP_WORKFLOW_ALPHA.getDescription()).thenReturn(backupWorkflowDescriptionName(randomString()));
		when(INACTIVE_BACKUP_WORKFLOW_ALPHA.isActive()).thenReturn(false);

		when(INACTIVE_BACKUP_WORKFLOW_BETA.getDescription()).thenReturn(backupWorkflowDescriptionName(randomString()));
		when(INACTIVE_BACKUP_WORKFLOW_BETA.isActive()).thenReturn(false);
		
		when(AN_ACTIVE_WF_COUNCIL_WORKFLOW.getDescription()).thenReturn(BACKUP_DESCRIPTION_SNIPPET);
		when(AN_ACTIVE_WF_COUNCIL_WORKFLOW.isActive()).thenReturn(true);
		
		when(AN_ACTIVE_WORKFLOW.getDescription()).thenReturn(ANY_STRING);
		when(AN_ACTIVE_WORKFLOW.isActive()).thenReturn(true);
		
		when(AN_INACTIVE_WORKFLOW.getDescription()).thenReturn(ANY_STRING);
		when(AN_INACTIVE_WORKFLOW.isActive()).thenReturn(false);
	}
	
	@Before
	public void setUp()
	{
		mockLogger = LoggerFactory.getLogger(ResolutionService.class);
	}
	
	@After
	public void tearDown()
	{
		objectUnderTest = null;
	}
	
	@Test
	public void testHappyPathWith1OfEachStatusColor() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerWithABackup();
		givenAMockResolvedWFService();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
		thenWorkflowManagerIsCalledForCopyAndCreateAndABackup(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenUserManagerCalledToGetUserNameAtLeastOnce();
	}
	
	@Test
	public void testHappyPathWith1OfEachStatusColorMultipleBackups() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerWithMultipleBackups();
		givenAMockResolvedWFService();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
		thenWorkflowManagerIsCalledForCopyAndCreateAndABackup(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenUserManagerCalledToGetUserNameAtLeastOnce();
	}
	
	private void givenAMockWorkflowManagerWithMultipleBackups()
	{
		setupMockWorkflowManagerWithMultipleBackups();
	}

	private void setupMockWorkflowManagerWithMultipleBackups()
	{
		mockWorkflowManager = mock(WorkflowManager.class);
		
		Collection<JiraWorkflow> listWithSingleBackup = listOfWorkflowsWithNoInactiveWFCouncilBackup();
		listWithSingleBackup.add(INACTIVE_BACKUP_WORKFLOW_ALPHA);
		listWithSingleBackup.add(INACTIVE_BACKUP_WORKFLOW_BETA);
		listWithSingleBackup.add(INACTIVE_BACKUP_WORKFLOW);
		
		when(mockWorkflowManager.getWorkflows()).thenReturn(listWithSingleBackup);
	}
	
	
	@Test
	public void testInvalidInput() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAnInvalidWorkflow();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAnInvalidInputErrorIsLogged();
	}
	
	@Test
	public void testWorkflowBackupFails() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowExpectingOnlyGetNameCall();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerThatCannotBackup(workflow);
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAGeneralErrorIsLogged();
		thenWorkflowIsCalledAsExpectedForGetNameCall(workflow);
		thenWorkflowManagerOnlyCopies(workflow);
		thenUserManagerCalledToGetUserNameAtLeastOnce();
	}
	
	@Test
	public void testUserManagerFails() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowExpectingOnlyGetNameCall();
		givenAMockUserMgrThrowingRTE();
		givenAMockWorkflowManagerWithNoBackup();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAGeneralErrorIsLogged();
		thenWorkflowIsCalledAsExpectedForGetNameCall(workflow);
		thenUserManagerCalledToGetUserNameAtLeastOnce();
		thenWorkflowManagerIsCalledForFindingBackupsOnly(workflow);
	}
	
	@Test
	public void testResolvedWFServiceFails() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerWithNoBackup();
		givenAMockResolvedWFServiceThrowingIllegalArg();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAnIllegalArgIsLogged();
		thenUserManagerCalledToGetUserNameAtLeastOnce();
		thenWorkflowManagerOnlyCopies(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
	}
	
	@Test
	public void testResolvedWFServiceFailsWithBackup() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerWithABackup();
		givenAMockResolvedWFServiceThrowingIllegalArg();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAnIllegalArgIsLogged();
		thenUserManagerCalledToGetUserNameAtLeastOnce();
		thenWorkflowManagerCopiesAndPurges(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
	}
	
	@Test
	public void testWorkflowSaveFails() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerThatCannotSave(workflow);
		givenAMockResolvedWFService();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAGeneralErrorIsLogged();
		thenUserManagerCalledToGetUserNameAtLeastOnce();
		thenWorkflowManagerIsCalledForCopyAndCreate(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
	}

	@Test
	public void testWorkflowSaveFailsWithBackup() throws InvalidWorkflowDescriptorException
	{
		JiraWorkflow workflow = givenAWorkflowWith1OfEachStatusColor();
		givenAMockUserMgrReturningMockUser();
		givenAMockWorkflowManagerThatCannotSaveWithBackup(workflow);
		givenAMockResolvedWFService();
		givenAMockLogger();
		
		whenResolutionServiceIsCalledOn(workflow);
		
		thenAGeneralErrorIsLogged();
		thenUserManagerCalledToGetUserNameAtLeastOnce();
		thenWorkflowManagerIsCalledForCopyAndCreateAndABackup(workflow);
		thenResolvedWorkflowServiceIsCalledOnce(stepColorMapWith1OfEachStatusColor());
		thenWorkflowIsCalledAsExpectedFor1OfEachStatus(workflow);
	}

	
	private void givenAMockResolvedWFServiceThrowingIllegalArg() throws InvalidWorkflowDescriptorException
	{
		mockResolvedWFService = mock(ResolvedWorkflowService.class);
		doThrow(SOME_ILLEGAL_ARG_EX).when(mockResolvedWFService).addResolutionPostFunctionsTo(ANY_WF_DESCRIPTOR, stepColorMapWith1OfEachStatusColor());
	}

	private void givenAMockUserMgrThrowingRTE()
	{
		mockUserManager = mock(UserManager.class);
		when(mockUserManager.getUserByName(WF_COUNCIL_USER)).thenThrow(SOME_RTE);
	}
	
	private void thenAGeneralErrorIsLogged()
	{
		assertErrorLogged(SOME_RTE);
	}

	private void thenAnIllegalArgIsLogged()
	{
		assertErrorLogged(SOME_ILLEGAL_ARG_EX);
	}
	
	private void assertErrorLogged(Exception ex)
	{
		verify(mockLogger, atLeastOnce()).error(stackTraceMessageStringFrom(ANY_STRING, ex));
	}
	
	private void thenWorkflowManagerOnlyCopies(JiraWorkflow workflow)
	{
		assertWorkflowManagerOnlyCopies(workflow);
		verify(mockWorkflowManager, times(1)).getWorkflows();
	}
	
	private void thenWorkflowManagerCopiesAndPurges(JiraWorkflow workflow)
	{
		assertWorkflowManagerOnlyCopies(workflow);
		assertWorkflowManagerPurgesCorrectly();
	}
	
	private void assertWorkflowManagerOnlyCopies(JiraWorkflow workflow)
	{
		verify(mockWorkflowManager, times(1)).copyWorkflow(SOME_APP_USER, 
				backupWorkflowName(ANY_STRING, FIXED_TIME), backupWorkflowDescription(ANY_STRING, FIXED_TIME), workflow);
	}
	
	private JiraWorkflow givenAWorkflowExpectingOnlyGetNameCall()
	{
		
		JiraWorkflow workflow = mock(JiraWorkflow.class);
		
		when(workflow.getName()).thenReturn(ANY_STRING);
		
		
		return workflow;
	}
	
	private void thenWorkflowIsCalledAsExpectedForGetNameCall(JiraWorkflow workflow)
	{
		verify(workflow, atLeastOnce()).getName();
	}
	
	private void givenAMockWorkflowManagerThatCannotBackup(JiraWorkflow workflow)
	{
		setUpAMockWorkflowManagerReturningNoCouncilBackups();
		
		when(mockWorkflowManager.copyWorkflow(SOME_APP_USER, 
				backupWorkflowName(ANY_STRING, FIXED_TIME), backupWorkflowDescription(ANY_STRING, FIXED_TIME), workflow)).thenThrow(SOME_RTE);
	}

	private Collection<JiraWorkflow> listOfWorkflowsWithNoInactiveWFCouncilBackup()
	{
		Collection<JiraWorkflow> listWithNoWFCouncilBackup = new ArrayList<JiraWorkflow>();
		listWithNoWFCouncilBackup.add(AN_ACTIVE_WORKFLOW);
		listWithNoWFCouncilBackup.add(AN_INACTIVE_WORKFLOW);
		listWithNoWFCouncilBackup.add(AN_ACTIVE_WF_COUNCIL_WORKFLOW);
		return listWithNoWFCouncilBackup;
	}

	private void givenAMockLogger()
	{
		mockLogger = mock(Logger.class);
		
	}

	private void thenAnInvalidInputErrorIsLogged()
	{
		verify(mockLogger, atLeastOnce()).error(INVALID_INPUT_MSG);
		
	}

	private JiraWorkflow givenAnInvalidWorkflow()
	{
		return null;
	}

	private void thenUserManagerCalledToGetUserNameAtLeastOnce()
	{
		verify(mockUserManager, atLeastOnce()).getUserByName(WF_COUNCIL_USER);
	}

	private void thenResolvedWorkflowServiceIsCalledOnce(Map<Integer, StatusCategory> stepColorMap)
	        throws InvalidWorkflowDescriptorException
	{
		verify(mockResolvedWFService, times(1)).addResolutionPostFunctionsTo(ANY_WF_DESCRIPTOR, stepColorMap);
	}

	private void thenWorkflowManagerIsCalledForCopyAndCreateAndABackup(JiraWorkflow workflow)
	{
		assertWorkflowManagerCalledForCopyCreateAndGetWorkflows(workflow);
		assertWorkflowManagerPurgesCorrectly();
	}

	private void assertWorkflowManagerPurgesCorrectly()
	{
		verify(mockWorkflowManager, times(1)).deleteWorkflow(INACTIVE_BACKUP_WORKFLOW);
	}

	private void thenWorkflowManagerIsCalledForCopyAndCreate(JiraWorkflow workflow)
	{
		assertWorkflowManagerCalledForCopyCreateAndGetWorkflows(workflow);
	}
	
	private void assertWorkflowManagerCalledForCopyCreateAndGetWorkflows(JiraWorkflow workflow)
	{
		assertWorkflowManagerOnlyCopies(workflow);
		verify(mockWorkflowManager, times(1)).createWorkflow(SOME_APP_USER, workflow);
		verify(mockWorkflowManager, times(1)).getWorkflows();
	}
	
	private void thenWorkflowManagerIsCalledForFindingBackupsOnly(JiraWorkflow workflow)
	{
		verify(mockWorkflowManager, times(1)).getWorkflows();
	}

	private void thenWorkflowIsCalledAsExpectedFor1OfEachStatus(JiraWorkflow workflow)
	{
		verify(workflow, atLeastOnce()).getName();
		verify(workflow, atLeastOnce()).getDescriptor();
		verify(workflow, atLeastOnce()).getLinkedStatusObjects();
		
		verify(workflow, atLeastOnce()).getLinkedStep(A_BLUE_STATUS);
		verify(workflow, atLeastOnce()).getLinkedStep(A_YELLOW_STATUS);
		verify(workflow, atLeastOnce()).getLinkedStep(A_BLUE_STATUS);
		
	}

	private void givenAMockResolvedWFService()
	{
		mockResolvedWFService = mock(ResolvedWorkflowService.class);
	}

	private void givenAMockWorkflowManagerWithABackup()
	{
		setupMockWorkflowManagerWithBackup();
	}

	private void setupMockWorkflowManagerWithBackup()
	{
		mockWorkflowManager = mock(WorkflowManager.class);
		
		Collection<JiraWorkflow> listWithSingleBackup = listOfWorkflowsWithNoInactiveWFCouncilBackup();
		listWithSingleBackup.add(INACTIVE_BACKUP_WORKFLOW);
		
		when(mockWorkflowManager.getWorkflows()).thenReturn(listWithSingleBackup);
	}
	
	private void givenAMockWorkflowManagerWithNoBackup()
	{
		setUpAMockWorkflowManagerReturningNoCouncilBackups();
	}

	
	private void givenAMockWorkflowManagerThatCannotSave(JiraWorkflow workflow)
	{
		setUpAMockWorkflowManagerReturningNoCouncilBackups();
		
		doThrow(SOME_RTE).when(mockWorkflowManager).createWorkflow(SOME_APP_USER, workflow);
	}
	
	private void givenAMockWorkflowManagerThatCannotSaveWithBackup(JiraWorkflow workflow)
	{
		setupMockWorkflowManagerWithBackup();
		
		doThrow(SOME_RTE).when(mockWorkflowManager).createWorkflow(SOME_APP_USER, workflow);
	}

	
	private void setUpAMockWorkflowManagerReturningNoCouncilBackups()
	{
		mockWorkflowManager = mock(WorkflowManager.class);
		when(mockWorkflowManager.getWorkflows()).thenReturn(listOfWorkflowsWithNoInactiveWFCouncilBackup());
	}

	
	private void givenAMockUserMgrReturningMockUser()
	{
		mockUserManager = mock(UserManager.class);
		when(mockUserManager.getUserByName(WF_COUNCIL_USER)).thenReturn(SOME_APP_USER);
	}

	private void whenResolutionServiceIsCalledOn(JiraWorkflow workflow)
	{
		objectUnderTest = new ResolutionService(mockWorkflowManager, mockUserManager, mockResolvedWFService, mockLogger, FIXED_TIME);
		objectUnderTest.addResolutionPostFunctionsTo(workflow);
	}

	private Map<Integer, StatusCategory> stepColorMapWith1OfEachStatusColor()
	{
		Map<Integer, StatusCategory> expectedStepColorMap = new HashMap<Integer, StatusCategory>();
		expectedStepColorMap.put(STEP_ID_ALPHA, BLUE);
		expectedStepColorMap.put(STEP_ID_BETA, YELLOW);
		expectedStepColorMap.put(STEP_ID_PHI, GREEN);
		return expectedStepColorMap;
	}

	private JiraWorkflow givenAWorkflowWith1OfEachStatusColor()
	{
		List<Status> statuses = new ArrayList<Status>();
		statuses.add(A_BLUE_STATUS);
		statuses.add(A_YELLOW_STATUS);
		statuses.add(A_GREEN_STATUS);

		
		
		
		blueStepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
		yellowStepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
		greenStepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
		
		blueStepDescriptor.setId(STEP_ID_ALPHA);
		yellowStepDescriptor.setId(STEP_ID_BETA);
		greenStepDescriptor.setId(STEP_ID_PHI);
		
		
		
		
		
		JiraWorkflow workflow = mock(JiraWorkflow.class);
		
		when(workflow.getName()).thenReturn(ANY_STRING);
		when(workflow.getDescriptor()).thenReturn(ANY_WF_DESCRIPTOR);
		when(workflow.getLinkedStatusObjects()).thenReturn(statuses);
		
		
		when(workflow.getLinkedStep(A_BLUE_STATUS)).thenReturn(blueStepDescriptor);
		when(workflow.getLinkedStep(A_YELLOW_STATUS)).thenReturn(yellowStepDescriptor);
		when(workflow.getLinkedStep(A_GREEN_STATUS)).thenReturn(greenStepDescriptor);
		
		return workflow;
	}
}
