use crate::config::Config;
use crate::models::{ErrorResponse, StoreResponse};
use crate::signing::verify_presigned;
use crate::validation::is_valid_hash;

use actix_web::{web, HttpResponse};
use std::collections::HashMap;
use std::path::PathBuf;
use tokio::fs;

const CHUNKS_DIR: &str = "./data/chunks";

pub async fn health() -> HttpResponse {
    HttpResponse::Ok().json(serde_json::json!({
        "status": "ok",
        "service": "storage-node"
    }))
}

pub async fn store_chunk(hash: web::Path<String>, body: web::Bytes) -> HttpResponse {
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

    let bytes_len = body.len() as u64;

    match fs::write(&path, &body).await {
        Ok(_) => HttpResponse::Ok().json(StoreResponse {
            hash: hash.into_inner(),
            bytes_written: bytes_len,
        }),
        Err(e) => {
            if e.raw_os_error() == Some(28) {
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

pub async fn get_chunk(hash: web::Path<String>) -> HttpResponse {
    if !is_valid_hash(&hash) {
        return HttpResponse::BadRequest().json(ErrorResponse {
            error: "Invalid hash: must be 64 hex characters".to_string(),
        });
    }

    let path = PathBuf::from(CHUNKS_DIR).join(hash.as_str());

    match fs::read(&path).await {
        Ok(bytes) => HttpResponse::Ok()
            .content_type("application/octet-stream")
            .body(bytes),
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => {
            HttpResponse::NotFound().json(ErrorResponse {
                error: format!("Chunk not found: {}", hash),
            })
        }
        Err(e) => HttpResponse::InternalServerError().json(ErrorResponse {
            error: format!("Failed to read chunk: {}", e),
        }),
    }
}

pub async fn store_chunk_verified(
    config: web::Data<Config>,
    hash: web::Path<String>,
    query: web::Query<HashMap<String, String>>,
    body: web::Bytes,
) -> HttpResponse {
    if !is_valid_hash(&hash) {
        return HttpResponse::BadRequest().json(ErrorResponse {
            error: "Invalid hash: must be 64 hex characters".to_string(),
        });
    }

    let expires: u64 = match query.get("expires").and_then(|e| e.parse().ok()) {
        Some(e) => e,
        None => {
            return HttpResponse::BadRequest().json(ErrorResponse {
                error: "Missing or invalid expires parameter".to_string(),
            })
        }
    };

    let sig = match query.get("sig") {
        Some(s) => s.clone(),
        None => {
            return HttpResponse::BadRequest().json(ErrorResponse {
                error: "Missing sig parameter".to_string(),
            })
        }
    };

    if let Err(reason) = verify_presigned(&hash, expires, &sig, &config.presign_secret) {
        return HttpResponse::Forbidden().json(ErrorResponse {
            error: reason.to_string(),
        });
    }

    let path = PathBuf::from(CHUNKS_DIR).join(hash.as_str());

    if let Err(e) = fs::create_dir_all(CHUNKS_DIR).await {
        return HttpResponse::InternalServerError().json(ErrorResponse {
            error: format!("Failed to create directory: {}", e),
        });
    }

    let bytes_len = body.len() as u64;

    match fs::write(&path, &body).await {
        Ok(_) => HttpResponse::Ok().json(StoreResponse {
            hash: hash.into_inner(),
            bytes_written: bytes_len,
        }),
        Err(e) => HttpResponse::InternalServerError().json(ErrorResponse {
            error: format!("Failed to write chunk: {}", e),
        }),
    }
}
