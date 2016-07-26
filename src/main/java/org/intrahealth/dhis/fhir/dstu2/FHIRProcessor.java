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
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.parser.DataFormatException;
import java.io.IOException;
import javax.json.JsonObject;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.intrahealth.dhis.fhir.BaseProcessor;
import org.intrahealth.dhis.scriptlibrary.ScriptLibrary;
import org.intrahealth.dhis.scriptlibrary.ScriptNotFoundException;
import org.springframework.core.io.ClassPathResource;

/**                                                                                                                                                                                 
 * @author Carl Leitner <litlfred@gmail.com>
 */
public class FHIRProcessor extends BaseProcessor { 
    public static final String RESOURCE_PATH = "/fhir/dstu2";
    public static final String MIME_FHIR_JSON = "application/json+fhir";
    public static final String MIME_FHIR_XML = "application/xml+json";
    public static final String[] operations = {
	"read","vread","base","update","delete","history","create","search","conformance","batch","transaction"
    };
    public static final String[] resources = {
	"AllergyIntolerance","Appointment","AppointmentResponse","AuditEvent","Basic","Binary", 
	"BodySite","Bundle","CarePlan","Claim","ClaimResponse","ClinicalImpression","Communication", 
	"CommunicationRequest","Composition","ConceptMap","Condition (aka Problem)","Conformance","Contract", 
	"DetectedIssue","Coverage","DataElement","Device","DeviceComponent","DeviceMetric","DeviceUseRequest", 
	"DeviceUseStatement","DiagnosticOrder","DiagnosticReport","DocumentManifest","DocumentReference","EligibilityRequest", 
	"EligibilityResponse","Encounter","EnrollmentRequest","EnrollmentResponse","EpisodeOfCare","ExplanationOfBenefit", 
	"FamilyMemberHistory","Flag","Goal","Group","HealthcareService","ImagingObjectSelection","ImagingStudy", 
	"Immunization","ImmunizationRecommendation","ImplementationGuide","List","Location","Media","Medication", 
	"MedicationAdministration","MedicationDispense","MedicationOrder","MedicationStatement","MessageHeader", 
	"NamingSystem","NutritionOrder","Observation","OperationDefinition","OperationOutcome","Order","OrderResponse", 
	"Organization","Parameters","Patient","PaymentNotice","PaymentReconciliation","Person","Practitioner", 
	"Procedure","ProcessRequest","ProcessResponse","ProcedureRequest","Provenance","Questionnaire", 
	"QuestionnaireResponse","ReferralRequest","RelatedPerson","RiskAssessment","Schedule","SearchParameter", 
	"Slot","Specimen","StructureDefinition","Subscription","Substance","SupplyRequest","SupplyDelivery","TestScript", 
	"ValueSet","VisionPrescription"
    };
    

    public FHIRProcessor(ScriptLibrary sl) {
	super(sl);
    }

    protected void setFhirContext() {
	fctx = FhirContext.forDstu2();
    }

     
    public void process_read_json(String resource, HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) 
    {
	try {
	    processJSONResourceOperation(resource, "read",http_request,dhis_request);
	    http_response.setContentType( FHIRProcessor.MIME_FHIR_JSON);
	    http_response.setCharacterEncoding("UTF-8");
	    http_response.getWriter().write(toJSONString((BaseResource) dhis_response));
	    http_response.setStatus(HttpServletResponse.SC_OK);
	} catch (ScriptNotFoundException e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response: " + ioe.toString());
	    }
	    http_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED,e.toString());
	    return ;
	} catch (Exception e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response: " + ioe.toString());
	    }
	    return ;
	}
    }

    public void process_read_xml(String resource, HttpServletResponse http_response, HttpServletRequest http_request, JsonObject dhis_request ) 
    {
	try {
	    processJSONResourceOperation(resource, "read",http_request,dhis_request);
	    http_response.setContentType( FHIRProcessor.MIME_FHIR_XML);
	    http_response.setCharacterEncoding("UTF-8");
	    http_response.getWriter().write(toXMLString((BaseResource) dhis_response));
	    http_response.setStatus(HttpServletResponse.SC_OK);
	} catch (ScriptNotFoundException e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response: " + ioe.toString());
	    }
	    http_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED,e.toString());
	    return ;
	} catch (Exception e) {
	    e.printStackTrace();		
	    try {
		http_response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"internal script processing error on json for read.\n" + e.toString());
	    } catch (IOException ioe) {
		log.info("Could not send http response:" + ioe.toString());
	    }
	    return ;
	}
    }


    

}