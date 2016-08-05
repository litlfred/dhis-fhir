Further development has been moved to:
   https://code.launchpad.net/~litlfred/dhis2/script-library


#dhis-web-script-libray
This project adds support for DHIS2 Server-Side Apps with dynamic scripting .js scripting support leveraging the existing DHIS2 Web App.
* scriptlibrary package: https://github.com/litlfred/dhis-web-script-library/tree/master/src/main/java/org/intrahealth/dhis/scriptlibrary

This project uses the Script Library service to provide FHIR support to DHIS2 with <a href="http://hl7api.sourceforge.net/">HAPI</a>.  
* fhir support package: https://github.com/litlfred/dhis-web-script-library/tree/master/src/main/java/org/intrahealth/dhis/fhir

Makes use of generic base dynamic processing functionality that allows other API bindings and script engines to be used.
*  Example Test Patient app: https://github.com/litlfred/dhis-web-script-library/tree/master/apps/test_patient

# Features
*  All public classes/methods in the DHIS api are available through a script engine                                     
*  engine.put("script_processor",this) is set before the script is evaluated, but after   any library dependencies are loaded.  this javascript has the following public variables set:                                                               
  *  http_request    - the HttpServletRequest  
  *  http_response   - the HttpServletResponse    
  *  script_out      - any output when evaluating the script                                                             
  *  script_request  - any arbitrary java object.  intended for use by a specific Controller (e.g. a FHIR controller)  
  *  script_response - intended for use by a specific Controller.  
  *  (streams)       - IO Streams for script execution are in SteamReader in, StreamWriter error & out
  *  etc. -            Exposing new variables is simple.  note: context is aleady exposed. add User? App?    
*  there are no requirements on the script being run.  however, if the script sets the     variable script_processor.script_response, then the processor will automatically attempt to convert the into a javax.io.java object which can then be processed by a controller                                          
*  the script engine searches for avilable resources via a ScriptLibrary that provides             a dependency list of script libraries that need to be eval'd before the script is run
*  currenlty has .js support with nashorn enabled,   others available javax.java.ScriptEngines can simply be turned on via a Map.   Possibilites include:
  *  .R with Renjin
  *  .ruby with jruby                                                                                                          


# Controllers
Two controllers are provided.  
*  scriptlibrary.exec package has ScriptExecController which will take a URL such as http://localhost:8080/script-library/{app}/exec/{script} and do a simple eval of the indicated script.  The script should live the DHIS2 Web App under the directory script-library/exec
*  fhir.dstu2 package has ResourceContoller which will take a FHIR URLs such as http://localhost:8080/script-library/{app}/fhir/dstu2/{resource}/{operation} and execute the script {resource}_{operation}.js. The ReosurceController uses  FHIR processor will automatically attempt to convert between XML and JSON representations of FHIR resources and convert script_response  into a javax.json.JsonObject, ready for further business logic.  The script should live the DHIS2 Web App under the directory script-library/fhir/dstu2

# Building
In order to build, this should be dropped into dhis2 source at dhis-2/dhis2-web-api.  You then need to edit a couple of files:
```
$ bzr diff
=== modified file 'dhis-2/dhis-web/dhis-web-portal/pom.xml'
--- dhis-2/dhis-web/dhis-web-portal/pom.xml     2016-04-23 11:21:33 +0000
+++ dhis-2/dhis-web/dhis-web-portal/pom.xml     2016-07-27 04:49:09 +0000
@@ -62,6 +62,12 @@
       <version>${project.version}</version>
       <type>war</type>
     </dependency>
+    <dependency>
+      <groupId>org.intrahealth</groupId>
+      <artifactId>dhis-web-script-library</artifactId>
+      <version>${project.version}</version>
+      <type>war</type>
+    </dependency>



=== modified file 'dhis-2/dhis-web/pom.xml'
--- dhis-2/dhis-web/pom.xml     2016-05-02 05:52:44 +0000
+++ dhis-2/dhis-web/pom.xml     2016-07-27 20:42:07 +0000
@@ -29,6 +29,7 @@
     <module>dhis-web-mobile</module>
     <module>dhis-web-sms</module>
     <module>dhis-web-apps</module>
+    <module>dhis-web-script-library</module>
     <module>dhis-web-portal</module>
   </modules>


=== modified file 'dhis-2/dhis-web/dhis-web-portal/src/main/webapp/WEB-INF/web.xml'
--- dhis-2/dhis-web/dhis-web-portal/src/main/webapp/WEB-INF/web.xml     2016-05-01 07:45:30 +0000
+++ dhis-2/dhis-web/dhis-web-portal/src/main/webapp/WEB-INF/web.xml     2016-07-27 04:45:06 +0000
@@ -172,6 +172,17 @@
   </servlet>
 
   <servlet>
+    <servlet-name>scriptLibraryServlet</servlet-name>
+    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
+    <init-param>
+      <param-name>contextConfigLocation</param-name>
+      <param-value>classpath*:/META-INF/scriptLibraryServlet.xml</param-value>
+    </init-param>
+    <load-on-startup>1</load-on-startup>
+    <async-supported>true</async-supported>
+  </servlet>
+
+  <servlet>
     <servlet-name>uaaServlet</servlet-name>
     <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
     <init-param>
@@ -207,6 +218,14 @@
     <url-pattern>/ohie/*</url-pattern>
   </servlet-mapping>
   <servlet-mapping>
+    <servlet-name>scriptLibraryServlet</servlet-name>
+    <url-pattern>/script-library</url-pattern>
+  </servlet-mapping>
+  <servlet-mapping>
+    <servlet-name>scriptLibraryServlet</servlet-name>
+    <url-pattern>/script-library/*</url-pattern>
+  </servlet-mapping>
+  <servlet-mapping>
     <servlet-name>uaaServlet</servlet-name>
     <url-pattern>/uaa</url-pattern>
   </servlet-mapping>


```
