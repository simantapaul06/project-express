package com.projectexpress.application_service.client;

import com.projectexpress.application_service.exception.JobNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;


@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{id}")
    public void validateJobExists(@PathVariable("id") Long id, @RequestHeader("Authorization") String AuthorizationHeader);

}



