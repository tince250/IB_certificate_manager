package com.certificate_manager.certificate_manager.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.certificate_manager.certificate_manager.dtos.CertificateDTO;
import com.certificate_manager.certificate_manager.dtos.CertificateRequestReturnedDTO;
import com.certificate_manager.certificate_manager.entities.CertificateRequest;
import com.certificate_manager.certificate_manager.entities.User;
import com.certificate_manager.certificate_manager.enums.RequestStatus;
import com.certificate_manager.certificate_manager.enums.UserRole;
import com.certificate_manager.certificate_manager.exceptions.CertificateNotFoundException;
import com.certificate_manager.certificate_manager.exceptions.NotPendingRequestException;
import com.certificate_manager.certificate_manager.exceptions.NotTheIssuerException;
import com.certificate_manager.certificate_manager.repositories.CertificateRequestRepository;
import com.certificate_manager.certificate_manager.services.interfaces.ICertificateRequestService;
import com.certificate_manager.certificate_manager.services.interfaces.ICertificateService;
import com.certificate_manager.certificate_manager.services.interfaces.IUserService;

@Service
public class CertificateRequestServiceImpl implements ICertificateRequestService {
	
	@Autowired
	private CertificateRequestRepository allRequests;
	
	@Autowired
	private ICertificateService certificateService;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private CertificateGenerator certificateGenerator;

	@Override
	public List<CertificateRequestReturnedDTO> getAllForRequester() {
		User requester = userService.getCurrentUser();
		
		
		List<CertificateRequest> requests = new ArrayList<CertificateRequest>();
		if (requester.getRole() == UserRole.USER)
			requests = allRequests.findAllForRequester(requester.getId());
		else 
			requests = allRequests.findAll(); 
		
		List<CertificateRequestReturnedDTO> ret = new ArrayList<CertificateRequestReturnedDTO>();
		for (CertificateRequest req : requests) {
			CertificateDTO issuer = certificateService.getBySerialNumber(req.getIssuerSerialNumber());
			ret.add(new CertificateRequestReturnedDTO(req, issuer));
		}
		
		return ret;
	}

	@Override
	public void acceptRequest(long id) {
		CertificateRequest request = allRequests.findById(id).orElseThrow(() -> new CertificateNotFoundException());
		
		validateProcessingRequest(request);
		
		this.certificateGenerator.generateCertificate(request);
		
		request.setStatus(RequestStatus.ACCEPTED);
		allRequests.save(request);
		allRequests.flush();
	}

	@Override
	public void denyRequest(long id, String rejectionReason) {
		CertificateRequest request = allRequests.findById(id).orElseThrow(() -> new CertificateNotFoundException());
		
		validateProcessingRequest(request);
		
		request.setStatus(RequestStatus.DENIED);
		request.setRejectionReason(rejectionReason);
		allRequests.save(request);
		allRequests.flush();
		
	}
	
	private void validateProcessingRequest(CertificateRequest request) {
		User issuer = userService.getCurrentUser();
		
		CertificateDTO issuerCertificate = this.certificateService.getBySerialNumber(request.getIssuerSerialNumber());
		
		if (issuerCertificate.getIssuedTo().getId() != issuer.getId()) {
			throw new NotTheIssuerException();
		}
		
		if (request.getStatus() != RequestStatus.PENDING) {
			throw new NotPendingRequestException();
		}
	}

}
