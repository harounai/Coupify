from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="CITYSPARK_", env_file=".env", extra="ignore")

    api_v1_prefix: str = "/v1"
    # v2 schema (auth + onboarding + notifications). Use a new DB file to avoid
    # having to run migrations on the earlier hackathon schema.
    sqlite_path: str = "cityspark_v3.db"

    # Ollama local server, no API keys, offline-friendly.
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "mistral"

    # AI notification loop
    ai_notification_enabled: bool = True
    ai_notification_tick_seconds: int = 120  # 2 minutes

    # JWT (local/offline). For production you'd load from env.
    jwt_secret: str = "dev-only-change-me"
    jwt_algorithm: str = "HS256"
    jwt_access_token_minutes: int = 60 * 24 * 14  # 14 days


settings = Settings()

