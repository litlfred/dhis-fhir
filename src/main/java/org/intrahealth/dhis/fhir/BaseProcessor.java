package org.intrahealth.dhis.fhir;
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

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import java.io.IOException;
import java.io.Reader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hisp.dhis.datavalue.DefaultDataValueService;
import org.intrahealth.dhis.Processor;
import org.intrahealth.dhis.ScriptLibrary;
import org.intrahealth.dhis.ScriptNotFoundException;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
abstract public class BaseProcessor extends Processor { 
    protected FhirContext fctx;
    protected IParser xmlParser;
    protected IParser jsonParser;
    
    public BaseProcessor(ScriptLibrary sl) {
	super(sl);
	setFhirContext();
        fctx.setParserErrorHandler(new StrictErrorHandler());
	xmlParser = fctx.newXmlParser();
	jsonParser = fctx.newJsonParser();
    }
    
    abstract protected void setFhirContext();
    
    
    public String getOperationKey(String resource, String operation) {
	return resource.toLowerCase() + "_" + operation;
    }

    public Boolean hasOperation(String resource, String operation) {
	return hasScript(getOperationKey(resource,operation));
    }


    protected void processJSONResourceOperation(String resource, String operation, HttpServletRequest http_request, JsonObject dhis_request ) 
	throws IOException, DataFormatException, ScriptNotFoundException, ScriptException
    {
	log.info("beginning resource processing of " + operation + " for " + resource);
	processJSONOperation(resource,operation,http_request,dhis_request);
	log.info("processed " + operation + " for" +resource);
	dhis_response = resourceFromJSON( (JsonObject) dhis_response);
    }

    protected void processJSONBundleOperation(String resource, String operation,HttpServletRequest http_request, JsonObject dhis_request ) 
	throws IOException, DataFormatException, ScriptNotFoundException, ScriptException
    {
	processJSONOperation(resource,operation,http_request,dhis_request);
	dhis_response = bundleFromJSON( (JsonObject) dhis_response);
    }


    public void processJSONOperation(String resource, String operation, HttpServletRequest http_request, Object dhis_request ) 
	throws IOException, DataFormatException, ScriptNotFoundException, ScriptException
    {
	log.info("beginning json processing of " + operation +  " for " + resource);
	processScript(getOperationKey(resource,operation), http_request, dhis_request);
	log.info("processed " + operation + " for " + resource);
	if (! (dhis_response instanceof JsonObject)) {
	    throw new DataFormatException("JSON object not found in dhis_response for operation " + operation + " for "  + resource);
	}
    }

    
    public String toJSONString(IResource r) throws DataFormatException {
	if (r == null) {
	    throw new DataFormatException("No resource to convert to JSON");
	}
	return jsonParser.encodeResourceToString(r);
    }
    public String toJSONString(Bundle b) throws DataFormatException {
	if (b == null) {
	    throw new DataFormatException("No bundle to convert to JSON");
	}
	return jsonParser.encodeBundleToString(b);
    }

    public String toXMLString(IResource r) throws DataFormatException {
	if (r == null) {
	    throw new DataFormatException("No resource to covvert to XML");
	}
	return xmlParser.encodeResourceToString(r);
    }
    public String toXMLString(Bundle b) throws DataFormatException {
	if (b == null) {
	    throw new DataFormatException("No bundle to covvert to XML");
	}
	return xmlParser.encodeBundleToString(b);
    }
    


    public Bundle bundleFromXML(String r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No XML stringr to process as bundle");
	}
	Object o = xmlParser.parseBundle(r);
	return (Bundle) o;
    }
    public Bundle bundleFromXML(Reader r) throws DataFormatException {
	if (r == null) {
	    throw new DataFormatException("No XML reader to process as bundle");
	}
	Object o = xmlParser.parseBundle(r);
	return (Bundle) o;
    }
    public Bundle bundleFromJSON(JsonObject o) throws DataFormatException {
	if (o == null) {
	    throw new DataFormatException("No JSON to process as bundle");
	}
	return bundleFromJSON(o.toString());
    }
    public Bundle bundleFromJSON(String r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No JSON string to process as bundle");
	}
	Object o = jsonParser.parseBundle(r);
	return (Bundle) o;
    }
    public Bundle bundleFromJSON(Reader r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No JSON reader to process as bundle");
	}
	Object o = jsonParser.parseBundle(r);
	return (Bundle) o;
    }

    public IResource resourceFromXML(String r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No XML string to process as resource");
	}
	Object o = xmlParser.parseResource(r);
	return (IResource) o;
    }
    public IResource resourceFromXML(Reader r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No XML reader to process as resource");
	}
	Object o = xmlParser.parseResource(r);
	return (IResource) o;
    }
    public IResource resourceFromJSON(String r) throws DataFormatException {
	if (r == null) {
	    throw new DataFormatException("No JSON string to process as resource");
	}
	Object o = jsonParser.parseResource(r);
	return (IResource) o;
    }
    public IResource resourceFromJSON(Reader r) throws DataFormatException  {
	if (r == null) {
	    throw new DataFormatException("No JSON reader to process as resource");
	}
	Object o = jsonParser.parseResource(r);
	return (IResource) o;
    }
    public IResource resourceFromJSON(JsonObject o) throws DataFormatException {
	if (o == null) {
	    throw new DataFormatException("No JSON to process as resource");
	}
	return resourceFromJSON(o.toString());
    }
    

}