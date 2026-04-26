from base64 import b64encode
from functools import lru_cache
from pathlib import Path
from typing import Literal

from fastapi import Depends, FastAPI, Header, HTTPException, status
from pydantic import BaseModel, Field

app = FastAPI(title="Mock Voucher API", version="1.0.0")

IMG_DIR = Path(__file__).resolve().parent.parent / "img"


USERS = {
    "alice": {"password": "alice123", "token": "mock-token-alice"},
    "bob": {"password": "bob123", "token": "mock-token-bob"},
}


class LoginRequest(BaseModel):
    username: str = Field(min_length=1)
    password: str = Field(min_length=1)


class LoginResponse(BaseModel):
    access_token: str
    token_type: Literal["bearer"] = "bearer"
    expires_in_seconds: int = 3600


class DailyVoucherResponse(BaseModel):
    b64image: str
    headline: str
    text: str
    percent: int = Field(ge=0, le=100)


@lru_cache(maxsize=None)
def _image_file_to_b64(filename: str) -> str:
    file_path = IMG_DIR / filename
    if not file_path.exists():
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Missing image file: {filename}",
        )
    return b64encode(file_path.read_bytes()).decode("ascii")


def _require_bearer_token(authorization: str | None = Header(default=None)) -> str:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing or invalid bearer token",
        )

    token = authorization.split(" ", 1)[1].strip()
    for username, user_data in USERS.items():
        if token == user_data["token"]:
            return username

    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid token",
    )


VOUCHERS_BY_USER = {
    "alice": [
        {
            "image": "burger_01.webp",
            "headline": "Burger Night Deal",
            "text": "Get a juicy burger combo at a special limited-time discount.",
            "percent": 20,
        },
        {
            "image": "coffee_01.webp",
            "headline": "Morning Coffee Boost",
            "text": "Start your day with premium coffee for less.",
            "percent": 35,
        },
    ],
    "bob": [
        {
            "image": "drink_01.webp",
            "headline": "Chill Drink Special",
            "text": "Cool down with a refreshing drink at a daily deal price.",
            "percent": 15,
        },
        {
            "image": "food_01.webp",
            "headline": "Food Lover Voucher",
            "text": "Use this voucher to save on a favorite meal today.",
            "percent": 40,
        },
    ],
}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/auth/login", response_model=LoginResponse)
def login(payload: LoginRequest) -> LoginResponse:
    user = USERS.get(payload.username)
    if not user or payload.password != user["password"]:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password",
        )

    return LoginResponse(access_token=user["token"])


@app.get("/voucher/daily", response_model=list[DailyVoucherResponse])
def daily_voucher(username: str = Depends(_require_bearer_token)) -> list[DailyVoucherResponse]:
    user_vouchers = VOUCHERS_BY_USER.get(username, [])
    return [
        DailyVoucherResponse(
            b64image=_image_file_to_b64(voucher["image"]),
            headline=voucher["headline"],
            text=voucher["text"],
            percent=voucher["percent"],
        )
        for voucher in user_vouchers
    ]
