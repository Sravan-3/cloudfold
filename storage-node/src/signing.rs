use hmac::{Hmac, Mac};
use sha2::Sha256;
use std::time::{SystemTime, UNIX_EPOCH};

pub fn verify_presigned(
    hash: &str,
    expires: u64,
    sig: &str,
    secret: &str,
) -> Result<(), &'static str> {
    let now = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_secs();

    if now > expires {
        return Err("URL has expired");
    }

    type HmacSha256 = Hmac<Sha256>;
    let data_to_sign = format!("{}:{}", hash, expires);

    let mut mac = HmacSha256::new_from_slice(secret.as_bytes()).expect("HMAC accepts any key size");

    mac.update(data_to_sign.as_bytes());
    let expected = hex::encode(mac.finalize().into_bytes());

    if expected != sig {
        return Err("Invalid signature");
    }

    Ok(())
}
