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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.vanguard.jira.workflowcouncil.domain.ResolvedWorkflowService.COMPLETE_STATUS_CATEGORY_NAME;

import java.util.UUID;

import com.atlassian.jira.issue.status.category.StatusCategory;

public class CommonTestUtils
{
	public static final String ANY_STRING = randomString();
	public static final Integer ANY_NUMBER = randomInteger();
	public static final Integer STEP_ID_ALPHA = randomInteger();
	public static final Integer STEP_ID_BETA = STEP_ID_ALPHA + STEP_ID_ALPHA;
	public static final Integer STEP_ID_THETA = STEP_ID_ALPHA + STEP_ID_BETA;
	public static final Integer STEP_ID_RHO = STEP_ID_ALPHA + STEP_ID_THETA;
	public static final Integer STEP_ID_PHI = STEP_ID_ALPHA + STEP_ID_RHO;
	public static final StatusCategory BLUE = mock(StatusCategory.class); 
	public static final StatusCategory YELLOW = mock(StatusCategory.class);
	public static final StatusCategory GREEN = mock(StatusCategory.class);

	
	public static int randomInteger()
	{
		return Integer.parseInt(UUID.randomUUID().toString().split("-")[1],16);
	}
	
	public static String randomString()
	{
		return UUID.randomUUID().toString().substring(0, 5);
	}
	
	public static void initStatusColors()
	{
		when(BLUE.getName()).thenReturn(randomString());
		when(YELLOW.getName()).thenReturn(randomString());
		when(GREEN.getName()).thenReturn(COMPLETE_STATUS_CATEGORY_NAME);
	}
}
