package com.f5.authserver.Service.Communication;

import com.f5.authserver.DTO.UserDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
public class AccountCommunicationServiceImpl implements AccountCommunicationService {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public AccountCommunicationServiceImpl(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    @Override
    public void registerAccount(UserDetailDTO userDetailDTO) {
        List<ServiceInstance> instances = discoveryClient.getInstances("AccountServer");
        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException("No Account-Server instances available");
        }

        // URI 생성 부분 수정
        ServiceInstance accountService = instances.get(0);
        URI uri = URI.create(accountService.getUri() + "/api/account");

        // HTTP 헤더 및 본문 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // DTO를 HttpEntity에 포함
        HttpEntity<UserDetailDTO> httpEntity = new HttpEntity<>(userDetailDTO, headers);

        // REST 요청 보내기
        try {
            ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Request to update new user account information sent successfully.");
            } else {
                throw new IllegalStateException("Failed to send request to update new user account information.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send request to Account-Server", e);
        }
    }
}
