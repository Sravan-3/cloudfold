use std::env;

pub struct Config {
    pub presign_secret: String,
}

impl Config {

    pub fn from_env() -> Self {

        Self {
            presign_secret: env::var("PRESIGN_SECRET")
                .expect("PRESIGN_SECRET must be set"),
        }
    }
}