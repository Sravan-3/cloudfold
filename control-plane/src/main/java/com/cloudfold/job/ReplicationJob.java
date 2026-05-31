package com.cloudfold.job;

import com.cloudfold.repository.ReplicationStatusRepository;
import com.cloudfold.service.ReplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplicationJob {

    private final ReplicationStatusRepository replicationRepo;
    private final ReplicationService replicationService;

    private volatile boolean running = false;

    public ReplicationJob(ReplicationStatusRepository replicationRepo,
                          ReplicationService replicationService) {
        this.replicationRepo = replicationRepo;
        this.replicationService = replicationService;
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void run() {

        if (running) {
            System.out.println("Replication job already running, skipping this tick");
            return;
        }

        running = true;

        try {
            System.out.println("Replication job started");


            List<String> underReplicated =
                    replicationRepo.findUnderReplicatedChunkHashes();

            if (underReplicated.isEmpty()) {
                System.out.println("All chunks fully replicated");
                return;
            }

            System.out.println(
                    "Found " + underReplicated.size() + " under-replicated chunks"
            );

            int batchLimit = 100;
            List<String> batch = underReplicated.stream()
                    .limit(batchLimit)
                    .toList();

            for (String chunkHash : batch) {
                replicationService.replicateChunk(chunkHash, "node-1");
            }

            System.out.println(
                    "Replication job queued " + batch.size() + " chunks for replication"
            );

        } finally {
            running = false;
        }
    }
}
