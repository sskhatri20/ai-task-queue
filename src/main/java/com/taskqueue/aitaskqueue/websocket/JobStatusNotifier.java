package com.taskqueue.aitaskqueue.websocket;

import com.taskqueue.aitaskqueue.model.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JobStatusNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyStatus(String jobId, JobStatus status) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("jobId", jobId);
        payload.put("status", status.name());
        messagingTemplate.convertAndSend("/topic/jobs/" + jobId, (Object) payload);
    }
}
