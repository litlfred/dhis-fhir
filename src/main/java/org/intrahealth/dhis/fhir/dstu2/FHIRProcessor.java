package org.intrahealth.dhis.fhir.dstu2;
/*
 * Copyright (c) 2016, IntraHealth International
 * All rights reserved.
 * GPL v3
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.parser.DataFormatException;
import java.io.IOException;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.intrahealth.dhis.fhir.BaseProcessor;
import org.intrahealth.dhis.ScriptLibrary;
import org.springframework.core.io.ClassPathResource;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
abstract public class FHIRProcessor extends BaseProcessor { 
    public static final String RESOURCE_PATH = "/dstu2";
    public static final String MIME_FHIR_JSON = "application/json+fhir";
    public static final String MIME_FHIR_XML = "application/xml+json";

    public FHIRProcessor(ScriptLibrary sl) {
	super(sl);
    }

    protected void setFhirContext() {
	fctx = FhirContext.forDstu2();
    }

     
    public void process_read_json(HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) 
    {
	try {
	    processResourceOperation("read",http_response,http_request,dhis_request);
	    http_response.setContentType( FHIRProcessor.MIME_FHIR_JSON);
	    http_response.setCharacterEncoding("UTF-8");
	    http_response.getWriter().write(toJSONString((BaseResource) dhis_response));
	    http_response.setStatus(HttpServletResponse.SC_OK);
	} catch (Exception e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response: " + ioe.toString());
	    }
	    return ;
	}
    }

    public void process_read_xml(HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) 
    {
	try {
	    processResourceOperation("read",http_response,http_request,dhis_request);
	    http_response.setContentType( FHIRProcessor.MIME_FHIR_XML);
	    http_response.setCharacterEncoding("UTF-8");
	    http_response.getWriter().write(toXMLString((BaseResource) dhis_response));
	    http_response.setStatus(HttpServletResponse.SC_OK);
	} catch (Exception e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response:" + ioe.toString());
	    }
	    return ;
	}
    }

    protected void processResourceOperation(String operation,HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException, DataFormatException
    {
	processOperation(operation,http_response,http_request,dhis_request);
	dhis_response = resourceFromJSON( (JsonObject) dhis_response);
    }

    protected void processBundleOperation(String operation,HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException, DataFormatException
    {
	processOperation(operation,http_response,http_request,dhis_request);
	dhis_response = bundleFromJSON( (JsonObject) dhis_response);
    }

    protected void processOperation(String operation,HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException, DataFormatException
    {
	if (!hasOperation(operation)) {
	    http_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED,"processing script not implemented for " + operation);
	    return ;
	}
	processOperation(http_request, dhis_request,operation);
	if (! (dhis_response instanceof JsonObject)) {
	    throw new DataFormatException("JSON object not found in dhis_response for operation " + operation);
	}
    }

    

}