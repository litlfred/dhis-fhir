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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Stack;
import javax.script.Invocable;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.intrahealth.dhis.ScriptLibrary;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class Processor {

    protected ScriptLibrary sl;
    protected ScriptEngine engine;
    protected Invocable invocable;
    protected ScriptContext ctx;
    public HttpServletResponse http_response
    public HttpServletRequest http_request;
    public Object dhis_response;
    public Object dhis_request;
    public XXXXX dhis;  //DHIS instance

    public Processor(ScriptLibrary sl) {
	this.sl = sl;
	ScriptEngineManager engineManager = new ScriptEngineManager();
	initEngine();
    }


    protected initEngine() {
	initEngine(new String[0]);
    }

    protected initEngine(String[] jslibs) {
	dhis_response = null;
	http_response  =null;
	dhis = SOMETHING XXXX;
	ctx = new SimpleScriptContext();
	ctx.setErrorWriter(new StringWriter());
	engine = engineManager.getEngineByName("nashorn");
	bind('dhis_processor',this,ScriptContext.ENGINE_SCOPE);

	//load up any referenced libraries
	Stack deps = new Stack();
	deps.addAll(Arrays.asList(jslibs));
	
	ArrayList seen = new ArrayList();
	while(! deps.isEmpty()) {
	    String script = deps.pop();
	    if (! sl.containsScript(script)) {
		continue;
	    }
	    ArrayList  sdeps = new ArrayList();
	    sdeps.addAll(Arrays.asList(sl.getDependencies(script)));
	    for (String s: seen) {
		sdeps.remove(s);
	    }
	    if (sdeps.size() == 0) {
		//no dependencies that are unmet
		String lib = sl.getSource(jslibs[i]);
		engine.eval(lib);
	    } else {
		//we need to push this back on the stack and add all the dependencies
		deps.push(script);
		deps.addAll(sdeps);
	    }
	}
	
    }

    public Boolean hasScript(String script) {
	return sl.containsKey(script);
    }

    public Boolean  processScript(HttpServletRequest http_request, Object dhis_request,String script) {
	if (sl.containsKey(script)) {
	    String source = sl.getSource(script);
	    initEngine(sl.getDependencies(script));
	    return processRequest(http_request, dhis_request,source);
	} else{
	    return null;
	}
    }

    public Boolean processRequest(HttpServletRequest  http_request, Object dhis_request,Reader js) {
	this.http_request = http_request;
	this.dhis_request = dhis_request;
	return engine.eval(js);
    }
    public Boolean  processRequest(HttpServletRequest  http_quest, Object dhis_request,String js) {
	this.http_request = http_request;
	this.dhis_request = dhis_request;
	return engine.eval(js);
    }


}