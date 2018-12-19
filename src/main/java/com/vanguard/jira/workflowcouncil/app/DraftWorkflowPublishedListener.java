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


package com.vanguard.jira.workflowcouncil.app;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.vanguard.jira.workflowcouncil.domain.ResolutionService;

@Component
public class DraftWorkflowPublishedListener implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(DraftWorkflowPublishedListener.class);

    @JiraImport
    private final EventPublisher eventPublisher;
    
	@Inject
	private ResolutionService resService;

	@Inject
    public DraftWorkflowPublishedListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Enabling plugin");
        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        log.info("Disabling plugin");
        eventPublisher.unregister(this);
    }
    
    @EventListener
    public void onDraftWorkflowPublished(DraftWorkflowPublishedEvent publishEvent)
    {
    	log.info("Just published " + publishEvent.getWorkflow().getName());
    	log.info("Now adding post-functions to set our clear resolutions, as needed");
    	
    	this.resService.addResolutionPostFunctionsTo(publishEvent.getWorkflow());
    	
    }

}