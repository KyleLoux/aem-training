package com.slalom.dxm.core.components.impl;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.slalom.dxm.core.components.Card;
import com.day.cq.wcm.api.PageManager;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Card.class},
        resourceType = {CardImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CardImpl implements Card {

    protected static final String RESOURCE_TYPE = "dxm/components/content/card";
    
    @Self
    @Required
    private SlingHttpServletRequest request;
    
    
    @ValueMapValue
    private String title;
    
         
    /**
     * A global variable made available by HTL script
     */
    @ScriptVariable
    @Required
    private PageManager pageManager;

    
    @PostConstruct
    public void init() {
                 
    }

    @Override
    public String getTitle() {        

        return this.title;
    }

}
