package org.intrahealth.dhis;
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

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.datavalue.DefaultDataValueService;
import org.intrahealth.dhis.ScriptLibrary;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class Processor {

    protected static final Log log = LogFactory.getLog( DefaultDataValueService.class );
    protected ScriptLibrary sl;
    protected ScriptEngineManager engineManager;
    protected ScriptEngine engine;
    protected Invocable invocable;
    protected ScriptContext ctx;
    protected Writer w;
    public HttpServletResponse http_response;
    public HttpServletRequest http_request;
    public Object dhis_response;
    public Object dhis_request;

//    public XXXXX dhis;  //DHIS instance

    public Processor(ScriptLibrary sl) {
	this.sl = sl;
	engineManager = new ScriptEngineManager();
	initEngine();
    }


    protected void initEngine() {
	initEngine(new String[0]);
    }

    protected void  initEngine(String[] jslibs) {
	dhis_response = null;
	http_response  =null;
//	dhis = SOMETHING XXXX;
	ctx = new SimpleScriptContext();
	w =new StringWriter();
	ctx.setErrorWriter(w);
	engine = engineManager.getEngineByName("nashorn");
	engine.put("dhis_processor",this);

	//load up any referenced libraries
	Stack deps = new Stack();
	deps.addAll(Arrays.asList(jslibs));
	
	ArrayList seen = new ArrayList();
	while(! deps.isEmpty()) {
	    String script = deps.pop().toString();
	    if (! sl.containsScript(script)) {
		continue;
	    }
	    ArrayList  sdeps = new ArrayList();
	    sdeps.addAll(Arrays.asList(sl.retrieveDependencies(script)));
	    for (Object s: seen) {
		sdeps.remove(s);
	    }
	    if (sdeps.size() == 0) {
		//no dependencies that are unmet
		String lib = sl.retrieveSource(script);
		seen.add(script);
		try {
		    log.info("processing library");
		    eval(lib);
		} catch (ScriptException e) {
		    //should put a warning message
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

    public Object  processScript(HttpServletRequest http_request, Object dhis_request,String script) {
	if (sl.containsScript(script)) {
	    String source = sl.retrieveSource(script);
	    initEngine(sl.retrieveDependencies(script));
	    return processRequest(http_request, dhis_request,source);
	} else{
	    return null;
	}
    }

    public Object processRequest(HttpServletRequest  http_request, Object dhis_request,Reader js) {
	this.http_request = http_request;
	this.dhis_request = dhis_request;
	try {
	    return eval(js);
	} catch (ScriptException e) {
	    //should give error message
	    return null;
	}	
	    
    }
    public Object  processRequest(HttpServletRequest  http_quest, Object dhis_request,String js) {
	this.http_request = http_request;
	this.dhis_request = dhis_request;
	try {
	    log.info("processing library: " + js);
	    return eval(js);
	} catch (ScriptException e) {
	    return null;
	    //should give error message
	}
    }
    
    protected void clearErrors() {
	String e =  w.toString();
	if ( (e != null) && (e.length() > 0)) {
	    log.info(e);
	}
	w =new StringWriter();
	ctx.setErrorWriter(w);
    }

    protected void showErrors() {
	String e =  w.toString();
	if ( (e != null) && (e.length() > 0)) {
	    log.info(e);
	}
    }

    protected Object eval(String js) throws ScriptException {
	clearErrors();
	Object r  = engine.eval(js);
	showErrors();
	return r;
    }

   protected Object eval(Reader js) throws ScriptException {
	clearErrors();
	Object r  = engine.eval(js);
	showErrors();
	return r;
    }

}