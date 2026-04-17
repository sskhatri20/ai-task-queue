package com.taskqueue.aitaskqueue.controller;

import com.taskqueue.aitaskqueue.controller.dto.JobResponse;
import com.taskqueue.aitaskqueue.controller.dto.JobSubmitRequest;
import com.taskqueue.aitaskqueue.service.JobService;
import com.taskqueue.aitaskqueue.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final RateLimiterService rateLimiterService;

    @PostMapping
    public ResponseEntity<JobResponse> submit(@RequestBody JobSubmitRequest request, HttpServletRequest httpRequest) {
        String clientId = httpRequest.getRemoteAddr();
        if (!rateLimiterService.isAllowed(clientId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobService.submitJob(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String id) {
        return jobService.getJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
