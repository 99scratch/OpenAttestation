/*
Copyright (c) 2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@author: wchen106 & lijuan
*/

package com.intel.openAttestation.AttestationService.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import gov.niarl.hisAppraiser.hibernate.domain.AttestRequest;
import gov.niarl.hisAppraiser.hibernate.domain.HOST_MLE;
import gov.niarl.hisAppraiser.hibernate.domain.MLE;
import gov.niarl.hisAppraiser.hibernate.domain.HOST;

import com.intel.openAttestation.AttestationService.resource.HOSTResource;
import com.intel.openAttestation.AttestationService.resource.AttestService;
import com.intel.openAttestation.AttestationService.bean.HostBean;
import com.intel.openAttestation.AttestationService.bean.RespSyncBean;
import com.intel.openAttestation.AttestationService.bean.ReqAttestationBean;
import com.intel.openAttestation.AttestationService.util.ActionConverter;
import com.intel.openAttestation.AttestationService.util.CommonUtil;
import com.intel.openAttestation.AttestationService.util.ActionDelay.Action;
import com.intel.openAttestation.AttestationService.util.ResultConverter;
import com.intel.openAttestation.AttestationService.util.ResultConverter.AttestResult;
import com.intel.openAttestation.AttestationService.util.AttestUtil;
import com.intel.openAttestation.AttestationService.bean.AttestationResponseFault;
import com.intel.openAttestation.AttestationService.bean.OpenAttestationResponseFault;
import com.intel.openAttestation.AttestationService.hibernate.dao.HOSTDAO;
import com.intel.openAttestation.AttestationService.hibernate.dao.MLEDAO;


/**
 * RESTful web service interface to work with HOST DB.
 * @author wchen106 & lijuan
 *
 */

@Path("/resources")
public class HOSTResource {
	private static Logger logger = Logger.getLogger("OpenAttestation");

	@POST
	@Path("/host")
	@Consumes("application/json")
	@Produces("application/json")
	public Response addHOST(@Context UriInfo uriInfo, HostBean hostFullObj, @Context javax.servlet.http.HttpServletRequest request){
        UriBuilder b = uriInfo.getBaseUriBuilder();
        b = b.path(HOSTResource.class);
		Response.Status status = Response.Status.OK;
		boolean isAnomaly = false;
		List<MLE> mles= new ArrayList<MLE>();
        try{
        	
			HOSTDAO dao = new HOSTDAO();
			MLEDAO mleDao = new MLEDAO();
			MLE mle = null;
			//Check if the HOST Name exists
			if (dao.isHOSTExisted(hostFullObj.getHostName())){
				status = Response.Status.BAD_REQUEST;
				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
				fault.setError_message("Data Error - HOST " + hostFullObj.getHostName() +" already exists in the database");
				return Response.status(status).header("Location", b.build()).entity(fault).build();
			}
			
			//Constraint check
			if (hostFullObj.getBIOSName() == null && hostFullObj.getVMMName() == null){
				isAnomaly = true; 
			} else if (hostFullObj.getBIOSName() != null && (hostFullObj.getBIOSOem() == null || hostFullObj.getBIOSVersion() == null)){
				isAnomaly = true;
			} else if (hostFullObj.getVMMName() != null && (hostFullObj.getVMMOSName() == null || hostFullObj.getVMMOSVersion() == null || hostFullObj.getVMMVersion() == null)){
				isAnomaly = true;
			}  
			
			if (isAnomaly){
				status = Response.Status.BAD_REQUEST;
				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
				fault.setError_message("Data Error - HOST " + "please check the input parameters and provide complete information");
				return Response.status(status).header("Location", b.build()).entity(fault).build();
			}
			
			if (hostFullObj.getBIOSName() != null) {
				//relation check for BIOS table
				mle =  mleDao.getMLE(hostFullObj, 0);
				if (mle == null){
                    status = Response.Status.BAD_REQUEST;
                    OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
                    fault.setError_message("Data Error - HOST " + "proper BIOS  or OEM  is not chosen");
                    return Response.status(status).header("Location", b.build()).entity(fault).build();
				}
				mles.add(mle);
				
			}  
			if (hostFullObj.getVMMName() != null) {
				//relation check for VMM MLE
				mle =  mleDao.getMLE(hostFullObj,1);
				if (mle == null){
                    status = Response.Status.BAD_REQUEST;
                    OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
                    fault.setError_message("Data Error - HOST " + "proper VMM  or OS  is not chosen");
                    return Response.status(status).header("Location", b.build()).entity(fault).build();
				}
				mles.add(mle);
			} 
			
			HOST host = new HOST();
			host.setAddOn_Connection_String(hostFullObj.getAddOn_Connection_String());
			host.setDescription(hostFullObj.getDescription());
			host.setEmail(hostFullObj.getEmail());
			host.setHostName(hostFullObj.getHostName());
			host.setIPAddress(hostFullObj.getIPAddress());
			host.setPort(hostFullObj.getPort());
			
			dao.addHOSTEntry(host);
			
			//insert an entry into HOST_MLE;
			for (int i=0; i<mles.size(); i++){
				HOST_MLE hostMle = new HOST_MLE();
				hostMle.setHost(host);
				hostMle.setMle(mles.get(i));
				dao.addHostMle(hostMle);
			}

	        return Response.status(status).header("Location", b.build()).type(MediaType.TEXT_PLAIN).entity("True").build();
	        
		}catch (Exception e){
			status = Response.Status.INTERNAL_SERVER_ERROR;
			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
					OpenAttestationResponseFault.FaultCode.FAULT_2001);
			fault.setError_message("Add HOST entry failed." + "Exception:" + e.getMessage());
			return Response.status(status).header("Location", b.build()).entity(fault).build();
		}
	}
	
	@PUT
	@Path("/host")
	@Consumes("application/json")
	@Produces("application/json")
	public Response updatehostEntry(@Context UriInfo uriInfo, HostBean hostFullObj, @Context javax.servlet.http.HttpServletRequest request){
        UriBuilder b = uriInfo.getBaseUriBuilder();
        b = b.path(HOSTResource.class);
		Response.Status status = Response.Status.ACCEPTED;
		boolean isAnomaly = false;

		try{
			HOSTDAO dao = new HOSTDAO();
			MLEDAO mleDao = new MLEDAO();
			MLE mle = null;
			if (!dao.isHOSTExisted(hostFullObj.getHostName())){
				status = Response.Status.NOT_FOUND;
				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(OpenAttestationResponseFault.FaultCode.FAULT_404);
				fault.setError_message("Update host entry failed.");
				fault.setDetail("host Index:" + hostFullObj.getHostName() + " doesn't exist in DB.");
				return Response.status(status).header("Location", b.build()).entity(fault).build();
			}
			
			//IP addresss and port are required
			if (hostFullObj.getIPAddress() == null || hostFullObj.getPort() == null){
				status = Response.Status.BAD_REQUEST;
				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(OpenAttestationResponseFault.FaultCode.FAULT_412);
				fault.setError_message("Update host entry failed.");
				fault.setDetail("hostNumber:" + hostFullObj.getHostName() +", IP addresss and port are required.");
				return Response.status(status).header("Location", b.build()).entity(fault).build();
			}
			
			if  (hostFullObj.getBIOSName() == null && hostFullObj.getVMMName() == null){
				isAnomaly = true; 
			} else if (hostFullObj.getBIOSName() != null && (hostFullObj.getBIOSOem() == null || hostFullObj.getBIOSVersion() == null)){
				isAnomaly = true;
			} else if (hostFullObj.getVMMName() != null && (hostFullObj.getVMMOSName() == null || hostFullObj.getVMMOSVersion() == null || hostFullObj.getVMMVersion() == null)){
				isAnomaly = true;
			} else if (hostFullObj.getBIOSName() != null) {
				//relation check for BIOS table
				mle = mleDao.getMLE(hostFullObj, 0);
				if (mle == null){
                    status = Response.Status.BAD_REQUEST;
                    OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
                    fault.setError_message("Data Error - HOST " + "proper BIOS  or OEM  is not chosen");
                    return Response.status(status).header("Location", b.build()).entity(fault).build();
				}
				
			} else if (hostFullObj.getVMMName() != null) {
				//relation check for VMM MLE 
				mle = mleDao.getMLE(hostFullObj, 1);
				if (mle == null){
                    status = Response.Status.BAD_REQUEST;
                    OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
                    fault.setError_message("Data Error - HOST " + "proper VMM  or OS  is not chosen");
                    return Response.status(status).header("Location", b.build()).entity(fault).build();
				}
			} 
					
			if (isAnomaly){
				status = Response.Status.BAD_REQUEST;
				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(2001);
				fault.setError_message("Data Error - HOST " + "please check the input parameters and provide complete information");
				return Response.status(status).header("Location", b.build()).entity(fault).build();
			}
			
			//update HOST table
			HOST host = new HOST();
			host.setAddOn_Connection_String(hostFullObj.getAddOn_Connection_String());
			host.setDescription(hostFullObj.getDescription());
			host.setEmail(hostFullObj.getEmail());
			host.setHostName(hostFullObj.getHostName());
			host.setIPAddress(hostFullObj.getIPAddress());
			host.setPort(hostFullObj.getPort());
			
			dao.updatehostEntry(host);
			return Response.status(status).header("Location", b.build()).type(MediaType.TEXT_PLAIN).entity("True").build();
		}catch (Exception e){
			e.printStackTrace();
			status = Response.Status.INTERNAL_SERVER_ERROR;
			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(OpenAttestationResponseFault.FaultCode.FAULT_500);
			fault.setError_message("Update host entry failed.");
			fault.setDetail("Exception:" + e.getMessage()); 
			return Response.status(status).entity(fault).build();
		}
	}	
	
	@DELETE
	@Path("/host")
	@Produces("application/json")
	public Response delhostEntry(@QueryParam("hostName") String Name, @Context UriInfo uriInfo){
        UriBuilder b = uriInfo.getBaseUriBuilder();
        b = b.path(HOSTResource.class);
		Response.Status status = Response.Status.OK;
        try{
			HOSTDAO dao = new HOSTDAO();
			System.out.println("Check if the HOST Name exists:" + Name);
			if (dao.isHOSTExisted(Name)){
				HOST host = dao.DeleteHOSTEntry(Name);
				dao.DeleteHostMle(host);
				return Response.status(status).type(MediaType.TEXT_PLAIN).entity("True").build();
			}
			status = Response.Status.BAD_REQUEST;
			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(OpenAttestationResponseFault.FaultCode.FAULT_2000);
			fault.setError_message("Host not found - Host - " + Name + "that is being deleted does not exist.");
			fault.setError_message("Data Error - HOST " + Name +" does not exist in the database");		
			return Response.status(status).entity(fault).build();
		}catch (Exception e){
			status = Response.Status.INTERNAL_SERVER_ERROR;
			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(OpenAttestationResponseFault.FaultCode.FAULT_500);
			fault.setError_message("Delete HOST entry failed." + "Exception:" + e.getMessage()); 
			return Response.status(status).entity(fault).build();
		}
	}
	
	
	/**
	 * synchronous attest model: client sends hosts and pcrmask to be attested, server attest these hosts and return specific PCR values.
	 * in this model, the client will always wait the response of server 
	 * @param Xauthuser
	 * @param Xauthpasswd
	 * @param reqAttestation
	 * @param uriInfo
	 * @return
	 */
	@PUT
	@Path("/PollHosts")
	@Consumes("application/json")
	@Produces("application/json")
	public Response pollHosts(@Context UriInfo uriInfo, ReqAttestationBean reqAttestation, @Context javax.servlet.http.HttpServletRequest request){
		UriBuilder b = uriInfo.getBaseUriBuilder();
        b = b.path(HOSTResource.class);
	    Response.Status status = Response.Status.OK;
	    String requestHost = request.getRemoteHost();
	    long timeThreshold = reqAttestation.getTimeThreshold() == null ? 0 :reqAttestation.getTimeThreshold();
	    long validateInterval = 0;
	    AttestUtil.loadProp();
	    try{
			HOSTDAO dao = new HOSTDAO();

    		String requestId = addRequests(reqAttestation, requestHost, true);
    		System.out.println("resource requestId:" +requestId);
    		List<AttestRequest> reqs= getRequestsByReqId(requestId);
    		if (reqs != null){
    	   		if (timeThreshold != 0 ){
        			logger.info("timeThreshold:" + timeThreshold);
        			for (AttestRequest req: reqs){
        				AttestRequest lastReq = dao.getLastAttestedRequest(req.getHostName());
        				long lastValidateTime = lastReq.getId()== null? 0: lastReq.getValidateTime().getTime();
        				validateInterval = System.currentTimeMillis() - lastValidateTime;
        				logger.info("validateInterval:" +validateInterval);
        				if (validateInterval < timeThreshold && lastValidateTime !=0 ){
        					System.out.println("obtain the trustworthiness of last record");
        					req.setAuditLog(lastReq.getAuditLog());
        					req.setResult(lastReq.getResult());
        					req.setValidateTime(lastReq.getValidateTime());
        				}
        				else{
        					req.setNextAction(CommonUtil.getIntFromAction(Action.SEND_REPORT));
        					req.setIsConsumedByPollingWS(false);//this flags must be set at the same time.
        					logger.debug("Next Action:" +req.getNextAction());
        				}
        				dao.updateRequest(req);
        			}
        			//start a thread to attest the pending request
        			if (!isAllAttested(requestId)){
        				logger.info("requestId:" +requestId +"is not attested.");
    		    		CheckAttestThread checkAttestThread = new CheckAttestThread(requestId);
    		     		checkAttestThread.start();
        			}	
        		}
        		else{// timeThreshold is null
    	    		do{  //loop until all hosts are finished
    	    			for (AttestRequest req: reqs){
    	    				//load the request again because its status may be changed for each loop
    	    				AttestRequest reqnew = AttestService.loadRequest(req.getId());
    	    				if (reqnew.getResult() == null){
    	    					long timeUsed = System.currentTimeMillis() - req.getRequestTime().getTime();
    	    					if (req.getMachineCert() == null ){
    	    						req.setResult(ResultConverter.getIntFromResult(AttestResult.UN_KNOWN));
    	    						req.setValidateTime(new Date());
    	    						dao.updateRequest(req);
    	    					}
    	    					else if (timeUsed > AttestUtil.getDefaultAttestTimeout()){
    	    						req.setResult(ResultConverter.getIntFromResult(AttestResult.TIME_OUT));
    	    						req.setValidateTime(new Date());
    	    						dao.updateRequest(req);
    	    					} 
    	    				}
    	    			}
    	    			Thread.sleep(10000/reqs.size()); //@TO DO: better calculation?
    	    		}while(!AttestService.isAllAttested(requestId));
    	    		logger.info("requestId:" +requestId +" has attested");
        		}
        		
        		RespSyncBean syncResult = AttestService.getRespSyncResult(requestId);
        		logger.info("requestId:" +requestId +" has returned the attested result");
    			return Response.status(status).header("Location", b.build()).entity(syncResult).build();    			
    		} else {
    			status = Response.Status.INTERNAL_SERVER_ERROR;
    			AttestationResponseFault fault = new AttestationResponseFault(AttestationResponseFault.FaultName.FAULT_ATTEST_ERROR);
    			fault.setMessage("cannot fetch an entry from the table of AttestRequest, please check it");
    			return Response.status(status).header("Location", b.build()).entity(fault).build();
    		}
	    }catch(Exception e){
	    	status = Response.Status.INTERNAL_SERVER_ERROR;
			AttestationResponseFault fault = new AttestationResponseFault(AttestationResponseFault.FaultName.FAULT_ATTEST_ERROR);
			fault.setMessage("poll hosts failed.");
			fault.setDetail("Exception:" + e.toString());
			logger.fatal(fault.getMessage(), e);
			return Response.status(status).header("Location", b.build()).entity(fault).build();
	    }
	}

	public static String addRequests(ReqAttestationBean reqAttestation, String requestHost, boolean isSync) {
		HOSTDAO dao = new HOSTDAO();
		String requestId;
		if (isSync)
			requestId = CommonUtil.generateRequestId("PollHostsRequestId");
		else
			requestId = CommonUtil.generateRequestId("PostHostsRequestId");
		Date requestTime = new Date();
		List<String> host = reqAttestation.getHosts();
		int hostNum = host.size();
		AttestRequest[] attestRequests = new AttestRequest[hostNum];
		
		for(int i=0; i<hostNum; i++){
			attestRequests[i] = new AttestRequest();
			attestRequests[i].setRequestId(requestId);
			attestRequests[i].setHostName(reqAttestation.getHosts().get(i));
			attestRequests[i].setRequestTime(requestTime);
			if (reqAttestation.getTimeThreshold() ==null)
				attestRequests[i].setNextAction(ActionConverter.getIntFromAction(Action.SEND_REPORT));
			else
				attestRequests[i].setNextAction(CommonUtil.getIntFromAction(Action.DO_NOTHING));
			attestRequests[i].setIsConsumedByPollingWS(false);
			
			attestRequests[i].setMachineCert(dao.getMachineCert(reqAttestation.getHosts().get(i)));
			attestRequests[i].setRequestHost(requestHost);
			attestRequests[i].setCount(new Long(hostNum));
			attestRequests[i].setPCRMask(reqAttestation.getPCRmask());
			attestRequests[i].setIsSync(isSync);
		}
		for(AttestRequest req: attestRequests)
			dao.saveRequest(req);
		return requestId;
	}
	
	/**
	 * get requests by specific requestId
	 * @param requestId
	 * @return
	 */
	public static List<AttestRequest> getRequestsByReqId(String requestId) {
		System.out.println("getRequestsByReqId requestId:"+requestId);
		HOSTDAO dao = new HOSTDAO();
		return dao.getRequestsByRequestId(requestId);
	}
	
	/**
	 * decide whether all hosts has attested for a given requestId. 
	 * @param requestId of interest
	 * @return true if all has attested, else false
	 */
	public static boolean isAllAttested(String requestId){
		HOSTDAO attestationDao = new HOSTDAO();
		List<AttestRequest> attestRequests = attestationDao.getRequestsByRequestId(requestId);
		for (AttestRequest attestRequest : attestRequests){
			if (attestRequest.getResult() == null)
				return false;
		}
		return true;
	}
	
	/**
	 * get newest request by id.
	 * @param id
	 * @return
	 */
	public static AttestRequest loadRequest(Long id) {
		HOSTDAO dao = new HOSTDAO();
        return dao.getRequestById(id);
	}
	
}
