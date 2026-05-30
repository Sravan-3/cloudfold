mod config;
mod handlers;
mod models;
mod signing;
mod validation;

use actix_web::{web, App, HttpServer};
use dotenvy::dotenv;
use tokio::fs;

const CHUNKS_DIR: &str = "./data/chunks";

#[tokio::main]
async fn main() -> std::io::Result<()> {

    dotenv().ok();

    println!("Storage node starting on port 9001...");

    fs::create_dir_all(CHUNKS_DIR)
        .await
        .expect("Failed to create chunks directory");

    let config = web::Data::new(config::Config::from_env());

    HttpServer::new(move || {
        App::new()
            .app_data(config.clone())
            .route("/health", web::get().to(handlers::health))
            .route("/chunks/{hash}", web::post().to(handlers::store_chunk))
            .route("/chunks/{hash}", web::get().to(handlers::get_chunk))
            .route(
                "/chunks/{hash}",
                web::put().to(handlers::store_chunk_verified),
            )
    })
    .bind("0.0.0.0:9001")?
    .run()
    .await
}
