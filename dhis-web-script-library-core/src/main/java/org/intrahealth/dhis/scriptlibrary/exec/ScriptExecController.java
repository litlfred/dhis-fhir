package org.intrahealth.dhis.scriptlibrary.exec;
/*
 * Copyright (c) 2016, IntraHealth International
 * All rights reserved.
 * Apache 2.0
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
import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hisp.dhis.appmanager.App;
import org.hisp.dhis.appmanager.AppManager;
import org.hisp.dhis.appmanager.AppStatus;
import org.hisp.dhis.appmanager.DefaultAppManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.DefaultCurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.webapi.service.ContextService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.intrahealth.dhis.scriptlibrary.AppScriptLibrary;
import org.intrahealth.dhis.scriptlibrary.Processor;
import org.intrahealth.dhis.scriptlibrary.ScriptLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;


/**                                                                                                                                                                                 
* @author Carl Leitner <litlfred@gmail.com>
 */
@Controller
@RequestMapping( 
    value =  "/{app}/" + ScriptExecController.PATH
    )
public class ScriptExecController {
    protected Map<String,Processor> processors = new HashMap<String,Processor>();
    @Autowired
    private  AppManager appManager; //not sure how this gets set!
    @Autowired
    private CurrentUserService currentUserService;
    protected Processor processor;
    public static final String PATH = "/exec";


    protected Processor getProcessor(String app,HttpServletRequest http_request, HttpServletResponse http_response) 
	throws IOException
    {
        String contextPath = ContextUtils.getContextPath( http_request );
	App a = appManager.getApp(app,contextPath);
	User user = currentUserService.getCurrentUser();
	Processor proc = null;
	if (! processors.containsKey(app)
	    && a != null
	    ) {
	    ScriptLibrary sl = new AppScriptLibrary(app,ScriptExecController.PATH);
	    proc = new Processor(sl);
	    processors.put(app,proc);
	} else {
	    proc = processors.get(app);
	}
	if (proc == null) {
	    http_response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"App " + app + " does not exist");
	    return null;
	}  else if ( !appManager.isAccessible( a,user)) {
	    http_response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Permision denied for " + app);
	    return null;
	} else {
	    return proc;
	}
    }

    @RequestMapping( 
	value =  "/{script}"
	)
    public void exec_script( HttpServletResponse http_response, HttpServletRequest http_request,
			     @PathVariable("app") String app, @PathVariable("script") String script)
    {	
	try {
	    Processor proc = getProcessor(app,http_request,http_response);
	    proc.processScript(script,http_request);
	} catch (Exception e1) {
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error\n" + e1.toString());
	    } catch (Exception e2) {}
	}
    }


}