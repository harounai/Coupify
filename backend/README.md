# Mock FastAPI Voucher Service

Simple FastAPI project with:
- Mock authentication endpoint for two users
- Mock daily voucher endpoint returning two vouchers per authenticated user

## Endpoints

- `POST /auth/login`
  - Request body:
    ```json
    {
      "username": "alice",
      "password": "alice123"
    }
    ```
  - Mock users:
    - `alice` / `alice123`
    - `bob` / `bob123`
  - Response:
    ```json
    {
      "access_token": "mock-token-alice",
      "token_type": "bearer",
      "expires_in_seconds": 3600
    }
    ```

- `GET /voucher/daily`
  - Requires header: `Authorization: Bearer <user-token>`
  - Returns an array of 2 voucher records for the authenticated user
  - Response:
    - `b64image` (Base64 image string from files in `img/`)
    - `headline` (string)
    - `text` (string)
    - `percent` (integer 0..100)

## Run locally

```bash
python -m venv .venv
.venv\\Scripts\\activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open docs at: `http://127.0.0.1:8000/docs`
