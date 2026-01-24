package com.kdb.it.controller;

import com.kdb.it.dto.ApplicationDto;
import com.kdb.it.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<String> submit(@RequestBody ApplicationDto.CreateRequest request) {
        String apfMngNo = applicationService.submit(request);
        return ResponseEntity.created(URI.create("/api/applications/" + apfMngNo)).body(apfMngNo);
    }

    @PostMapping("/{apfMngNo}/approve")
    public ResponseEntity<Void> approve(@PathVariable("apfMngNo") String apfMngNo,
            @RequestBody ApplicationDto.ApproveRequest request) {
        applicationService.approve(apfMngNo, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{apfMngNo}")
    public ResponseEntity<ApplicationDto.Response> getApplication(@PathVariable("apfMngNo") String apfMngNo) {
        ApplicationDto.Response response = applicationService.getApplication(apfMngNo);
        return ResponseEntity.ok(response);
    }
}
