# dhis-web-fhir
DHIS2 FHIR API Bindings and Dynamic Processor

This is part of library to provide FHIR support to DHIS2.  Uses dynamically configurable FHIR support through the nashorn javascript engine.  

Makes use of generic base dynamic processing functionality that allows other API bindings and script engines to be used.


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