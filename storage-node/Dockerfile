FROM rust:1.95-alpine AS builder

RUN apk add --no-cache musl-dev

WORKDIR /app

COPY Cargo.toml Cargo.lock ./
RUN mkdir src && echo 'fn main() {}' > src/main.rs
RUN cargo build --release 2>/dev/null; rm -f target/release/storage-node

COPY src src
RUN touch src/main.rs && cargo build --release

FROM alpine:3.19

RUN apk add --no-cache libgcc

WORKDIR /app
RUN mkdir -p /app/data/chunks

RUN addgroup -S storage && adduser -S storage -G storage
RUN chown -R storage:storage /app/data
USER storage

COPY --from=builder /app/target/release/storage-node .

EXPOSE 9001
CMD ["./storage-node"]
