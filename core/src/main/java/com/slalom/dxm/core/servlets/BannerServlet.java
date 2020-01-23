/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.slalom.dxm.core.servlets;

import com.day.cq.dam.api.DamConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aemds.guide.utils.JcrResourceConstants;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

@Component(service=Servlet.class,
property={
        Constants.SERVICE_DESCRIPTION + "= Banner Servlet",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.extensions=" + "html",
        "sling.servlet.paths=" + "/bin/getBanners"
})

public class BannerServlet extends SlingSafeMethodsServlet {

private static final long serialVersionUID = 1L;

private final Logger logger = LoggerFactory.getLogger(BannerServlet.class);


@Override
protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
		throws ServletException, IOException {
		
		response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        final PrintWriter out = response.getWriter();
        final ResourceResolver resolver = request.getResourceResolver();
        
        // Get the resource for the given path
        final Resource resource = resolver.getResource(request.getParameter("path"));
        final Node node = resource.adaptTo(Node.class);
        
        // Get all children of the given path
        Iterable<Resource> children = resource.getChildren();

        // Create JsonObject
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();

    	int i = 0;
    	// Loop through all child folders
        for(Resource child: children) {
        	if(child.getResourceType().equals("sling:Folder")) {
        		
        		Iterable<Resource> grandchildren = child.getChildren();
        		JsonObject jsonObject2 = new JsonObject();
        		int j = 0;
        		
        		// Loop through all the dam:Assets of the child folders
        		for (Resource grandchild : grandchildren) {
        			if(grandchild.getResourceType().equals("dam:Asset")) {
        				
        				// Get all content fragment elements and populate json
        				ContentFragment contentFragment = grandchild.adaptTo(ContentFragment.class);
        				JsonObject elementsObject = new JsonObject();
        				Iterator<ContentElement> elements = contentFragment.getElements();
        				
        				for (Iterator iter = elements; iter.hasNext(); ) {
        					ContentElement element = (ContentElement) iter.next();
        					elementsObject.addProperty(element.getName(), element.getContent().toString());
        				}
        				jsonObject2.add(contentFragment.getTitle(), elementsObject);
						jsonObject2.add("test", elementsObject);
        			}
        		}
        		i++;
        		jsonObject.add(child.getPath(), jsonObject2);
        	}
        }
        
        gson.toJson(jsonObject);

        try {
            out.write(jsonObject.toString());
            response.setStatus(SlingHttpServletResponse.SC_OK);
        } catch (Exception e ) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
	}	
}
