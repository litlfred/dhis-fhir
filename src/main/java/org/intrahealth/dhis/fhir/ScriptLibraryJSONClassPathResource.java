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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.datavalue.DefaultDataValueService;
import org.intrahealth.dhis.ScriptLibrary;
import org.intrahealth.dhis.ScriptLibraryJSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class ScriptLibraryJSONClassPathResource extends ScriptLibraryJSON {
    protected static final Log log = LogFactory.getLog( DefaultDataValueService.class );    
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    public ScriptLibraryJSONClassPathResource(String[] resources,String pathPrefix, String[] operations) {
	log.info("Init library  under " + pathPrefix);
	setLibrary(scanClassPath(resources,pathPrefix,operations));
    }

    protected JsonObject scanClassPath(String[] resources, String pathPrefix,String[] operations) {
	/*  we are scanning the classpath for .js files.  may be better to access source code dynamically, 
	 *  but as this is pnly for proof of concept.. c'est la vie
	 */
	JsonObjectBuilder json = Json.createObjectBuilder();
	ArrayList<String> found_libs = new ArrayList<String>();
	String bl = "base_library";
	String bl_script_path = "/script-library/fhir/" + pathPrefix  + "/base_library.js";
	Resource bl_script_src = resourceLoader.getResource( bl_script_path);
	String bl_script = null;
	try {
	    if ( bl_script_src.exists() && bl_script_src.isReadable() ) {
		bl_script = IOUtils.toString(bl_script_src.getInputStream(), "UTF-8");
		json.add(bl, Json.createObjectBuilder()
			 .add("source",bl_script)
		    );
		log.info("found " +  bl_script_src.getURI());
	    }
	} catch (Exception e) {}

	for (String resource: resources) {
	    log.info("Adding library for " + resource);
	    resource = resource.toLowerCase();

	    for (int i=0; i < operations.length; i++) {
		String operation = operations[i];
		String script_path = "/script-library/fhir/" + pathPrefix + "/"  + resource + "_" +  operation + ".js";
		//log.info("checking " + script_path);
		Resource script_src = resourceLoader.getResource(script_path);
		String script = null;
		try {
		    if ( script_src.exists() && script_src.isReadable() ) {
			script =  IOUtils.toString(script_src.getInputStream(), "UTF-8");
			log.info("found " + script_src.getURI());
		    }
		} catch (Exception e) {
		    //log.info("not found " + script_path + "\n" + e.toString());
		    continue;
		}
		if (script == null) {
		    continue;
		}
		//we have a script

		String rbl = "base_library";
		String rbl_script_path = "/script-library/fhir/" + pathPrefix  + "/" + resource + "_base_library.js";
		Resource rbl_script_src = resourceLoader.getResource(   rbl_script_path);
		String rbl_script = null;
		try {
		    if ( rbl_script_src.exists() && rbl_script_src.isReadable() ) {
			rbl_script = IOUtils.toString(rbl_script_src.getInputStream(), "UTF-8");
			json.add(rbl, Json.createObjectBuilder()
				 .add("source",rbl_script)
			    );
			log.info("found " +  rbl_script_src.getURI());
		    }
		} catch (Exception e) {}


		JsonArrayBuilder deps = Json.createArrayBuilder();
		if ( bl_script != null) {
		    deps.add(bl);
		}
		if ( rbl_script != null) {
		    deps.add(rbl);
		}

		json.add( resource + "_" + operation, Json.createObjectBuilder()
			  .add("source",script)
			  .add("deps", deps)
		    );
	    }
	}
	return json.build();
    }



}
