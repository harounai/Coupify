"""
Local backend application package.

This file ensures `uvicorn app.main:app` resolves to this repo's `backend/app`
module (and not an unrelated installed `app` package).
"""

