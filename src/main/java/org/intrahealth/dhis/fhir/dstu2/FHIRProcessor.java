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
import ca.uhn.fhir.model.dstu2.resource.BaseResource
import ca.uhn.fhir.model.dstu2.resource.Bundle
import org.intrahealth.dhis.fhir.BaseProcessor;


/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
abstract public class FHIRProcessor extends BaseProcessor { 

    public static final String RESOURCE_PATH = "/fhir/dstu2";
    public static final String MIME_FHIR_JSON = "application/fhir+json";
    public static final String MIME_FHIR_XML = "application/xml+json";

    protected void setFhirContext() {
	fctx = FhirContext.forDstu2();
    }

    public static function retrieveClassPathResourceLibrary(String resource) {
	resource = resource.toLowerCase();
	JsonObjectBuilder json =n Json.createObjectBuilder();
	ClassPathResource lib_src = new ClassPathResource("/script-library/fhir/dstu2/" + resource + "_base_library.js");
	String lib = null;
	try {
	    lib = new String(Files.readAllBytes(lib_src) , StandardCharsets.UTF_8);
	} catch (IOExpcetion e) {}
	if (lib != null) {
	    json.add(resource + "_base_library", Json.createObjectBuilder()
		     .add('source',lib)
		);
	}
	String[] operations = {"read","vread","base","update","delete","history","create","search","conformance","batch","transaction"};
	for (int i=0; i < operations.length; i++) {
	    String operation = operations[i];
	    String script_src = new ClassPathResource("/script-library/fhir/dstu2/" + resource + "_" +  operation + ".js");
	    try {
		String script =  new String(Files.readAllBytes(script_src) , StandardCharsets.UTF_8);
	    } catch (IOException e) {
		continue;
	    }
	    //we have a script
	    if (lib != null)  {
		json.add( resource + "_" + operation, Json.createObjectBuilder()
			 .add('source',script)
			 .add('deps', Json.createArrayBuilder()
			      .add( new JSONString(resource + "_base_library"))
			     )
		    );
	    } else {
		json.add(resource + "_" + operation, Json.createObjectBuilder()
			 .add('source',script)
		    );      
	    }
	}
	return json.build();
    }
    
    public void process_read_json(HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException
    {
	process_read(http_response,http_request,dhis_request);
	if ( !dhis_response instanceof BaseResource) {
	    http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on resource");
	    return ;
	}
	http_response.setContentType( FHIRProcessor.MIME_FHIR_JSON);
	http_response.setCharacterEncoding("UTF-8");
	http_response.setStatus(HttpServletResponse.SC_OK);
	http_response.getWriter().write(toJSONString(dhis_response));

    }

    public void process_read_xml(HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException
    {
	process_read(http_response,http_request,dhis_request);
	if ( !dhis_response instanceof BaseResource) {
	    http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on resource");
	    return ;
	}
	http_response.setContentType( FHIRProcessor.MIME_FHIR_XML);
	http_response.setCharacterEncoding("UTF-8");
	http_response.setStatus(HttpServletResponse.SC_OK);
	http_response.getWriter().write(toXMLString(dhis_response));
    }

    public void process_read(HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) throws IOException
    {
	if (!hasOperation('read')) {
	    http_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED,"processing script not implemented");
	    return ;
	}
	processOperation(http_request, dhis_request,"read");
	if (! (dhis_response) instanceof JsonObject) {
	    http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json");
	    return ;
	}
	dhis_response = resourceFromJson(dhis_response);
    }

   

}