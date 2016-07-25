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


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.intrahealth.dhis.ScriptLibrary;
import org.intrahealth.dhis.ScriptLibraryJSON;
import org.springframework.core.io.ClassPathResource;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class ScriptLibraryJSONClassPathResource extends ScriptLibraryJSON {



    public ScriptLibraryJSONClassPathResource(String resource,String pathPrefix) {
	setLibrary(scanClassPath(resource,pathPrefix));
    }

    protected JsonObject scanClassPath(String resource, String pathPrefix) {
	resource = resource.toLowerCase();
	JsonObjectBuilder json = Json.createObjectBuilder();
	String lib_src = (new ClassPathResource("/script-library/fhir/" + pathPrefix  + "/" +resource + "_base_library.js")).getPath();
	String lib = null;
	try {
	    lib = new String(Files.readAllBytes(Paths.get(lib_src)) , StandardCharsets.UTF_8);
	} catch (IOException e) {}
	if (lib != null) {
	    json.add(resource + "_base_library", Json.createObjectBuilder()
		     .add("source",lib)
		);
	}
	String[] operations = {"read","vread","base","update","delete","history","create","search","conformance","batch","transaction"};
	for (int i=0; i < operations.length; i++) {
	    String operation = operations[i];
	    String script_src = (new ClassPathResource("/script-library/fhir/" + pathPrefix + "/"  + resource + "_" +  operation + ".js")).getPath();
	    String script;
	    try {
		script =  new String(Files.readAllBytes(Paths.get(script_src)) , StandardCharsets.UTF_8);
	    } catch (IOException e) {
		continue;
	    }
	    //we have a script
	    if (lib != null)  {
		json.add( resource + "_" + operation, Json.createObjectBuilder()
			  .add("source",script)
			 .add("deps", Json.createArrayBuilder()
			      .add( resource + "_base_library")
			     )
		    );
	    } else {
		json.add(resource + "_" + operation, Json.createObjectBuilder()
			 .add("source",script)
		    );      
	    }
	}
	return json.build();
    }

}
