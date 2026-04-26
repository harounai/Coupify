from __future__ import annotations

from sqlalchemy import text
from sqlalchemy.engine import Engine


def _column_names(engine: Engine, table: str) -> set[str]:
    with engine.connect() as conn:
        rows = conn.execute(text(f"PRAGMA table_info({table})")).fetchall()
    # row: (cid, name, type, notnull, dflt_value, pk)
    return {str(r[1]) for r in rows}


def ensure_sqlite_schema(engine: Engine) -> None:
    """
    Tiny hackathon-friendly migrations for SQLite.
    We only ADD COLUMN when missing (safe + forward-only).
    """

    # merchant_rules: add new columns if the DB was created before we added them
    try:
        cols = _column_names(engine, "merchant_rules")
    except Exception:
        cols = set()

    alter_statements: list[str] = []
    if "coupons_per_day" not in cols:
        alter_statements.append("ALTER TABLE merchant_rules ADD COLUMN coupons_per_day INTEGER DEFAULT 50")
    if "coupons_total" not in cols:
        alter_statements.append("ALTER TABLE merchant_rules ADD COLUMN coupons_total INTEGER DEFAULT 1000")
    if "coupons_total_issued" not in cols:
        alter_statements.append("ALTER TABLE merchant_rules ADD COLUMN coupons_total_issued INTEGER DEFAULT 0")
    if "products_csv" not in cols:
        alter_statements.append("ALTER TABLE merchant_rules ADD COLUMN products_csv VARCHAR DEFAULT 'coffee,food'")

    if alter_statements:
        with engine.begin() as conn:
            for stmt in alter_statements:
                conn.execute(text(stmt))

    # users table role fields (hackathon migration)
    try:
        user_cols = _column_names(engine, "users")
    except Exception:
        return

    user_alters: list[str] = []
    if "role" not in user_cols:
        user_alters.append("ALTER TABLE users ADD COLUMN role VARCHAR DEFAULT 'USER'")
    if "company_business_id" not in user_cols:
        user_alters.append("ALTER TABLE users ADD COLUMN company_business_id VARCHAR")

    if user_alters:
        with engine.begin() as conn:
            for stmt in user_alters:
                conn.execute(text(stmt))

