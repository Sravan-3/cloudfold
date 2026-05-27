use serde::Serialize;

#[derive(Serialize)]
pub struct ErrorResponse {
    pub error: String,
}

#[derive(Serialize)]
pub struct StoreResponse {
    pub hash: String,
    pub bytes_written: u64,
}