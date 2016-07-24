package org.intrahealth.dhis.fhir.dstu2;
/*
 * Copyright (c) 2016, IntraHealth International
 * All rights reserved.
 * GPL v3
 * Carl Leitner <litlfred@gmail.com>
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

import java.io.IOException;
import java.nio.file.Files;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.intrahealth.dhis.fhir.dstu2.FHIRProcessor;
import org.intrahealth.dhis.ScriptLibrary;
import org.intrahealth.dhis.ScriptLibraryJSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;


/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
@Controller
public class PatientController {



    public class PatientProcessor extends FHIRProcessor {	    
	
	public PatientProcessor(ScriptLibrary sl) {
	    super(sl);
	}
	public static final String RESOURCE_NAME = "Patient";

	public String getResourceName() {
	    return RESOURCE_NAME;
	}
    }

    PatientProcessor fp;
    
    public PatientController() {
	ScriptLibrary sl = retrieveLibrary();
	fp = new PatientProcessor(sl);
    }

    protected ScriptLibrary retrieveLibrary() {	
	ScriptLibraryJSON sl = new ScriptLibraryJSON();
	sl.initizalize(FHIRProcessor.retrieveClassPathResourceLibrary(PatientProcessor.RESOURCE_NAME));
	return sl;
    }


    @RequestMapping( 
	value =  FHIRProcessor.RESOURCE_PATH + "/" + PatientProcessor.RESOURCE_NAME + "/{id}",
	method = RequestMethod.GET, 
	consumes =  FHIRProcessor.MIME_FHIR_JSON
	)
    public void operation_retreive_json( HttpServletResponse http_response, HttpServletRequest http_request, @PathVariable("id") String id ) throws IOException
    {
	JsonObject dhis_request = Json.createObjectBuilder()
	    .add("_id",id)
	    .build();	
	fp.process_read_json(http_response,http_request,dhis_request);
    }

    @RequestMapping( 
	value =  FHIRProcessor.RESOURCE_PATH + "/" + PatientProcessor.RESOURCE_NAME ,
	method = RequestMethod.GET, 
	consumes = FHIRProcessor.MIME_FHIR_JSON
	)
    public void operation_retreive_json_param( HttpServletResponse http_response, HttpServletRequest http_request, @RequestParam("_id") String id ) throws IOException
    {
	JsonObject dhis_request = Json.createObjectBuilder()
	    .add("_id",id)
	    .build();	
	fp.process_read_json(http_response,http_request,dhis_request);
    }

    @RequestMapping( 
	value =  FHIRProcessor.RESOURCE_PATH + "/" + PatientProcessor.RESOURCE_NAME + "/{id}",
	method = RequestMethod.GET, 
	consumes =  FHIRProcessor.MIME_FHIR_XML
	)
    public void operation_retreive_xml( HttpServletResponse http_response, HttpServletRequest http_request, @PathVariable("id") String id ) throws IOException
    {
	JsonObject dhis_request = Json.createObjectBuilder()
	    .add("_id",id)
	    .build();	
	fp.process_read_xml(http_response,http_request,dhis_request);
    }

    @RequestMapping( 
	value =  FHIRProcessor.RESOURCE_PATH + "/" + PatientProcessor.RESOURCE_NAME ,
	method = RequestMethod.GET, 
	consumes = FHIRProcessor.MIME_FHIR_XML
	)
    public void operation_retreive_xml_param( HttpServletResponse http_response, HttpServletRequest http_request, @RequestParam("_id") String id ) throws IOException
    {
	JsonObject dhis_request = Json.createObjectBuilder()
	    .add("_id",id)
	    .build();	
	fp.process_read_xml(http_response,http_request,dhis_request);
    }



}