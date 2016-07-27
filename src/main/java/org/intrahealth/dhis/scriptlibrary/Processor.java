package org.intrahealth.dhis.scriptlibrary;
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
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.script.Invocable;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.datavalue.DefaultDataValueService;
import org.intrahealth.dhis.scriptlibrary.ScriptLibrary;
import org.intrahealth.dhis.scriptlibrary.ScriptExecutionException;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class Processor {
    
    /*
     * 1) All public classes/methods in the DHIS api are available through a script engine
     * 2) engine.put("script_processor",this) is set before the script is evaluated, but after
     *    any library dependencies are loaded.  this javascript has the following public 
     *    variables set:
     *      * http_request    - the HttpServletRequest
     *      * http_response   - the HttpServletResponse
     *      * script_out      - any output when evaluating the script
     *      * script_request  - any arbitrary java object.  intended for use by a specific Controller 
     *                          (e.g. FHIR controller)
     *      * script_response - intended for use by a specific Controller
     *                          Example, the base FHIR processor will automatically attempt to convert 
     *                          the into a javax.json.JsonObject, ready for the business logic
     *      * (streams)       - IO Streams for script execution are in SteamReader in, StreamWriter error & out
     *      * etc. -            Exposing new variables is simple.   
     *                          note: context is aleady exposed. add User? App?
     * 3) there are no requirements on the script being run.  however, if the script sets the
     *    variable script_processor.script_response, then the processor will automatically attempt
     *    to covert the into a javax.io.java object which can then be processed by a controller
     * 4) the script engine searches for avilable resources via a ScriptLibrary that provides 
     *    a dependency list of script libraries that need to be eval'd before the script is run
     * 5) currenlty has .js support with nashorn enabled,   others available javax.java.ScriptEngines
     *    can simply be turned on via a Map.   Possibilites include:
     *       * .R with Renjin
     *       * .ruby with jruby
     * 6) 
     */

    /*
     * Begin public class variables.  These are exposed to the script 
     */
    
    public HttpServletResponse http_response;
    public HttpServletRequest http_request;
    public Object script_response;
    public Object script_request;
    public Reader in;
    public Writer out;
    public Writer error;
    /*
     * End public class variables.  These are exposed to the script engine
     */

    protected static final Log log = LogFactory.getLog( DefaultDataValueService.class );
    protected ScriptLibrary sl;
    protected ScriptEngineManager engineManager;
    protected ScriptEngine engine;
    protected Invocable invocable;
    protected ScriptContext ctx;
    protected Map<String,String> engines = new HashMap<String,String>();
//    public XXXXX dhis;  //DHIS instance

    public Processor(ScriptLibrary sl) {
	this.sl = sl;
	engineManager = new ScriptEngineManager();
	engines.put("js","nashorn");
	//other interesting options include .R with Renjin,
    }




    protected void  initEngine(String ext, String[] jslibs,HttpServletRequest http_request, HttpServletResponse http_reponse) 
	throws ScriptException
    {
	script_response = null;
	http_response  =null;
	ctx = new SimpleScriptContext();
	out = new StringWriter();
	error = new StringWriter();
	try {
	    in = new InputStreamReader(http_request.getInputStream());
	} catch (Exception e) {
	    log.info("Could not capture input stream of request");
	}
	ctx.setErrorWriter(error);
	ctx.setWriter(out);
	String engine_name = null;
	try {
	    engine_name = engines.get(ext);
	    engine = engineManager.getEngineByName(ext); //e.g "nashorn"
	} catch (Exception e) {
	    throw new ScriptException("extension " + ext + ": cannot get " + engine_name);
	}
	engine.put("script_processor",this);
	//load up any referenced libraries
	Stack<String> deps = new Stack<String>();
	deps.addAll(Arrays.asList(jslibs));
	
	ArrayList seen = new ArrayList();
	while(! deps.isEmpty()) {
	    String script = deps.pop();
	    if ( FilenameUtils.getExtension(script) != ext
		 ||  ! sl.containsScript(script)) {
		continue;
	    }
	    ArrayList  sdeps = new ArrayList();
	    sdeps.addAll(Arrays.asList(sl.retrieveDependencies(script)));
	    for (Object s: seen) {
		sdeps.remove(s);
	    }
	    if (sdeps.size() == 0) {
		//no dependencies that are unmet
		try {
		    String lib = sl.retrieveSource(script);
		    seen.add(script);
		    log.info("processing library: " + script);
		    eval(lib);
		} catch (Exception e) {
		    log.info("Could not load library " + script);
		}
	    } else {
		//we need to push this back on the stack and add all the dependencies
		deps.push(script);
		deps.addAll(sdeps);
	    }
	}
	
    }

    public Boolean hasScript(String script) {
	return sl.containsScript(script);
    }

    public Object  processScript(String script, HttpServletRequest http_request, Object script_request) 
	throws ScriptException, ScriptNotFoundException
    {
	String source;
	try {
	    log.info("Retrieving script " + script);
	    source = sl.retrieveSource(script);
	} catch (IOException e) {
	    throw new ScriptNotFoundException("Could not retrieve script "  + script);
	}
	log.info("Retrieving dependencies");
	String ext = FilenameUtils.getExtension(script);
	initEngine(ext,sl.retrieveDependencies(script),http_request,http_response);
	log.info("processing script " +script);
	return processRequest(http_request, script_request,source);
    }

    public Object processRequest(HttpServletRequest  http_request, Object script_request,Reader js) 
	throws ScriptException
    {
	this.http_request = http_request;
	this.script_request = script_request;
	return eval(js);
    }
    public Object  processRequest(HttpServletRequest  http_quest, Object script_request,String js) 
	throws ScriptException
    {
	this.http_request = http_request;
	this.script_request = script_request;
	log.info("processing library: " + js);
	return eval(js);
    }
    
    protected void clearErrors() {
	error =new StringWriter();
	ctx.setErrorWriter(error);
    }
    protected void clearOut() {
	out =new StringWriter();
	ctx.setWriter(out);
    }


    protected void checkErrors() throws ScriptExecutionException {
	String e =  error.toString();
	if ( (e != null) && (e.length() > 0)) {
	    log.info("Errors:\n" +e);
	}
    }

    protected void checkOut() throws ScriptExecutionException {
	String e =  out.toString();
	if ( (e != null) && (e.length() > 0)) {
	    log.info("Out:\n" + e);
	}
    }


    protected Object eval(String js) throws ScriptException {
	clearErrors();
	clearOut();
	log.info("Running\n" + js);
	Object r  = engine.eval(js);
	if (r== null) {
	    log.info("Processing resulted in null");
	} else {
	    log.info("Processing resulted in (" + r.getClass()  + "):\n"  + r.toString());
	}
	checkErrors();
	checkOut();
	return r;
    }

    protected Object eval(Reader js) throws ScriptException {
	clearErrors();
	clearOut();
	Object r  = engine.eval(js);
	checkErrors();
	checkOut();
	return r;
    }

}