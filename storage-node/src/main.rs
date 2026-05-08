use actix_web::{web, App, HttpServer, HttpResponse};
use std::path::PathBuf;
use tokio::fs;
use serde::Serialize;

const CHUNKS_DIR: &str = "./data/chunks";


#[derive(Serialize)]
struct ErrorResponse {
    error: String,
}


#[derive(Serialize)]
struct StoreResponse {
    hash: String,
    bytes_written: u64,
}

fn is_valid_hash(hash: &str) -> bool {
    hash.len() == 64 && hash.chars().all(|c| c.is_ascii_hexdigit())
}

// POST /chunks/{hash}
async fn store_chunk(
    hash: web::Path<String>,      
    body: web::Bytes,             
) -> HttpResponse {

    // Validate the hash looks reasonable — prevent path traversal attacks
    // Example: reject hashes like "../../etc/passwd"
    if !is_valid_hash(&hash) {
        return HttpResponse::BadRequest().json(ErrorResponse {
            error: "Invalid hash: must be 64 hex characters".to_string(),
        });
    }

    let path = PathBuf::from(CHUNKS_DIR).join(hash.as_str());


    if let Err(e) = fs::create_dir_all(CHUNKS_DIR).await {
        return HttpResponse::InternalServerError().json(ErrorResponse {
            error: format!("Failed to create storage directory: {}", e),
        });
    }

    // Check for disk space — if write fails, return 507 Insufficient Storage
    let bytes_len = body.len() as u64;

    // Write bytes to disk
    match fs::write(&path, &body).await {
        Ok(_) => {
            HttpResponse::Ok().json(StoreResponse {
                hash: hash.into_inner(),
                bytes_written: bytes_len,
            })
        }
        Err(e) => {
            // Check if the error is a disk-full situation
            if e.raw_os_error() == Some(28) {  // 28 = ENOSPC on Linux
                HttpResponse::InsufficientStorage().json(ErrorResponse {
                    error: "Disk full".to_string(),
                })
            } else {
                HttpResponse::InternalServerError().json(ErrorResponse {
                    error: format!("Failed to write chunk: {}", e),
                })
            }
        }
    }
}

// GET /chunks/{hash}
async fn get_chunk(
    hash: web::Path<String>,
) -> HttpResponse {

    if !is_valid_hash(&hash) {
        return HttpResponse::BadRequest().json(ErrorResponse {
            error: "Invalid hash: must be 64 hex characters".to_string(),
        });
    }

    let path = PathBuf::from(CHUNKS_DIR).join(hash.as_str());

    match fs::read(&path).await {
        Ok(bytes) => {
            HttpResponse::Ok()
                .content_type("application/octet-stream")
                .body(bytes)
        }
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => {
            HttpResponse::NotFound().json(ErrorResponse {
                error: format!("Chunk not found: {}", hash),
            })
        }
        Err(e) => {
            HttpResponse::InternalServerError().json(ErrorResponse {
                error: format!("Failed to read chunk: {}", e),
            })
        }
    }
}

// GET /health
async fn health() -> HttpResponse {
    HttpResponse::Ok().json(serde_json::json!({
        "status": "ok",
        "service": "storage-node"
    }))
}


#[tokio::main]
async fn main() -> std::io::Result<()> {

    println!("Storage node starting on port 9001...");

    fs::create_dir_all(CHUNKS_DIR).await
        .expect("Failed to create chunks directory");

    HttpServer::new(|| {
        App::new()
            .route("/health",            web::get().to(health))
            .route("/chunks/{hash}",     web::post().to(store_chunk))
            .route("/chunks/{hash}",     web::get().to(get_chunk))
    })
    .bind("0.0.0.0:9001")?
    .run()
    .await
}