# CloudFold – Distributed File Storage System

CloudFold is a distributed object storage system inspired by Amazon S3, designed to handle large-scale file uploads and downloads with fault tolerance and horizontal scalability.

The system is split into:
- Control Plane (Java, Spring Boot): metadata, coordination, APIs
- Data Plane (Rust): high-performance chunk storage and retrieval

Key concepts:
- Chunking + replication
- Consistent hashing for distribution
- Failure detection and recovery

Author: Sravan