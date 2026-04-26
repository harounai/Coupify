from __future__ import annotations

from datetime import datetime

from sqlalchemy.orm import Session

from app.models import Business, CouponTemplate, Streak, User
from app.services.security import hash_password


def seed_if_empty(db: Session) -> None:
    # Users
    if not db.query(User).first():
        users = [
            User(
                id="user_alex",
                email="alex@example.com",
                password_hash=hash_password("password"),
                display_name="Alex",
                interests_csv="coffee,food,events",
                exploration_preference=60,
                created_at=datetime.utcnow(),
                has_completed_onboarding=True,
            ),
            User(
                id="user_mina",
                email="mina@example.com",
                password_hash=hash_password("password"),
                display_name="Mina",
                interests_csv="nightlife,art,food",
                exploration_preference=75,
                created_at=datetime.utcnow(),
                has_completed_onboarding=True,
            ),
            User(
                id="user_sam",
                email="sam@example.com",
                password_hash=hash_password("password"),
                display_name="Sam",
                interests_csv="fitness,wellness,coffee",
                exploration_preference=40,
                created_at=datetime.utcnow(),
                has_completed_onboarding=True,
            ),
        ]
        db.add_all(users)
        for u in users:
            db.add(Streak(user_id=u.id, current_days=0, best_days=0, last_checkin_date=None))

    # Businesses (Munich-ish coordinates; offline)
    #
    # Important: we both seed-and-upsert here so that:
    # - changing curated seed images updates existing DB rows
    # - adding new businesses later inserts them into existing DBs (not just empty ones)
    #
    # For reliability we use `picsum.photos` seeded URLs for the *new* businesses
    # (always returns an image), and keep the existing curated Unsplash set.
    curated_businesses = [
        # Original 14
        Business(
            id="biz_coffee_1",
            name="Nebula Coffee Lab",
            category="coffee",
            lat=48.137154,
            lon=11.576124,
            image_url="https://images.unsplash.com/photo-1481833761820-0509d3217039?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_gym_1",
            name="Pulse Forge Gym",
            category="fitness",
            lat=48.1351,
            lon=11.5820,
            image_url="https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_restaurant_1",
            name="Urban Harvest Kitchen",
            category="food",
            lat=48.1333,
            lon=11.5666,
            image_url="https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_coffee_2",
            name="Roast Republic",
            category="coffee",
            lat=48.1400,
            lon=11.5700,
            image_url="https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_fitness_2",
            name="Skyline Mobility Studio",
            category="fitness",
            lat=48.1450,
            lon=11.5750,
            image_url="https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_food_2",
            name="Metro Bento House",
            category="food",
            lat=48.1380,
            lon=11.5900,
            image_url="https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_nightlife_1",
            name="Afterglow Lounge",
            category="nightlife",
            lat=48.1362,
            lon=11.5752,
            image_url="https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_art_1",
            name="Canvas District Gallery",
            category="art",
            lat=48.1392,
            lon=11.5735,
            image_url="https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_events_1",
            name="CitySpark Pop-up Hall",
            category="events",
            lat=48.1347,
            lon=11.5791,
            image_url="https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_shopping_1",
            name="Market Mile",
            category="shopping",
            lat=48.1416,
            lon=11.5812,
            image_url="https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_wellness_1",
            name="Sauna & Serenity",
            category="wellness",
            lat=48.1432,
            lon=11.5698,
            image_url="https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_food_3",
            name="Spice Street Kitchen",
            category="food",
            lat=48.1319,
            lon=11.5723,
            image_url="https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_coffee_3",
            name="Amber Bean Corner",
            category="coffee",
            lat=48.1427,
            lon=11.5861,
            image_url="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=80",
        ),
        Business(
            id="biz_fitness_3",
            name="HIIT Harbor",
            category="fitness",
            lat=48.1326,
            lon=11.5886,
            image_url="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=80",
        ),

        # +20 more
        Business(id="biz_coffee_4", name="Kaffee Kontor", category="coffee", lat=48.1388, lon=11.5719, image_url="https://picsum.photos/seed/biz_coffee_4/1200/800"),
        Business(id="biz_coffee_5", name="Steam & Bloom", category="coffee", lat=48.1308, lon=11.5739, image_url="https://picsum.photos/seed/biz_coffee_5/1200/800"),
        Business(id="biz_food_4", name="Green Fork Deli", category="food", lat=48.1379, lon=11.5634, image_url="https://picsum.photos/seed/biz_food_4/1200/800"),
        Business(id="biz_food_5", name="Ramen Radar", category="food", lat=48.1457, lon=11.5796, image_url="https://picsum.photos/seed/biz_food_5/1200/800"),
        Business(id="biz_food_6", name="Sourdough Social", category="food", lat=48.1412, lon=11.5692, image_url="https://picsum.photos/seed/biz_food_6/1200/800"),
        Business(id="biz_fitness_4", name="Iron Atlas", category="fitness", lat=48.1368, lon=11.5892, image_url="https://picsum.photos/seed/biz_fitness_4/1200/800"),
        Business(id="biz_fitness_5", name="Tempo Track Studio", category="fitness", lat=48.1339, lon=11.5613, image_url="https://picsum.photos/seed/biz_fitness_5/1200/800"),
        Business(id="biz_wellness_2", name="Bloom Wellness House", category="wellness", lat=48.1471, lon=11.5683, image_url="https://picsum.photos/seed/biz_wellness_2/1200/800"),
        Business(id="biz_wellness_3", name="Salt Room Serenity", category="wellness", lat=48.1299, lon=11.5807, image_url="https://picsum.photos/seed/biz_wellness_3/1200/800"),
        Business(id="biz_art_2", name="Neon Frame Studio", category="art", lat=48.1408, lon=11.5924, image_url="https://picsum.photos/seed/biz_art_2/1200/800"),
        Business(id="biz_art_3", name="Studio Palette", category="art", lat=48.1322, lon=11.5689, image_url="https://picsum.photos/seed/biz_art_3/1200/800"),
        Business(id="biz_events_2", name="Rooftop Sessions", category="events", lat=48.1399, lon=11.5853, image_url="https://picsum.photos/seed/biz_events_2/1200/800"),
        Business(id="biz_events_3", name="Night Market Yard", category="events", lat=48.1357, lon=11.5646, image_url="https://picsum.photos/seed/biz_events_3/1200/800"),
        Business(id="biz_shopping_2", name="Riverwalk Finds", category="shopping", lat=48.1441, lon=11.5726, image_url="https://picsum.photos/seed/biz_shopping_2/1200/800"),
        Business(id="biz_shopping_3", name="Vintage Circuit", category="shopping", lat=48.1311, lon=11.5868, image_url="https://picsum.photos/seed/biz_shopping_3/1200/800"),
        Business(id="biz_nightlife_2", name="Bassline Bar", category="nightlife", lat=48.1383, lon=11.5803, image_url="https://picsum.photos/seed/biz_nightlife_2/1200/800"),
        Business(id="biz_nightlife_3", name="Velvet Neon Club", category="nightlife", lat=48.1420, lon=11.5771, image_url="https://picsum.photos/seed/biz_nightlife_3/1200/800"),
        Business(id="biz_books_1", name="Paperback Passage", category="shopping", lat=48.1462, lon=11.5741, image_url="https://picsum.photos/seed/biz_books_1/1200/800"),
        Business(id="biz_bakery_1", name="Crumb & Clover Bakery", category="food", lat=48.1341, lon=11.5929, image_url="https://picsum.photos/seed/biz_bakery_1/1200/800"),
        Business(id="biz_cinema_1", name="Lumen Indie Cinema", category="events", lat=48.1302, lon=11.5638, image_url="https://picsum.photos/seed/biz_cinema_1/1200/800"),
    ]

    if not db.query(Business).first():
        db.add_all(curated_businesses)
    else:
        # Upsert existing businesses to the curated set.
        for c in curated_businesses:
            b = db.get(Business, c.id)
            if not b:
                db.add(
                    Business(
                        id=c.id,
                        name=c.name,
                        category=c.category,
                        lat=c.lat,
                        lon=c.lon,
                        image_url=c.image_url,
                    )
                )
                continue

            changed = False
            if b.image_url != c.image_url:
                b.image_url = c.image_url
                changed = True
            if b.name != c.name:
                b.name = c.name
                changed = True
            if b.category != c.category:
                b.category = c.category
                changed = True
            if b.lat != c.lat:
                b.lat = c.lat
                changed = True
            if b.lon != c.lon:
                b.lon = c.lon
                changed = True

            if not changed:
                # Avoid dirtying the session unnecessarily
                pass

    # Coupon templates
    if not db.query(CouponTemplate).first():
        coupons = [
            CouponTemplate(id="coupon_coffee_1", title="Coffee Power Hour", category="coffee", base_discount=15, duration_hours=3),
            CouponTemplate(id="coupon_fitness_1", title="Fitness Trial Sprint", category="fitness", base_discount=20, duration_hours=4),
            CouponTemplate(id="coupon_food_1", title="Lunch Break Deal", category="food", base_discount=18, duration_hours=2),
            CouponTemplate(id="coupon_coffee_2", title="Cold Brew Express", category="coffee", base_discount=22, duration_hours=2),
            CouponTemplate(id="coupon_fitness_2", title="After Work Burn", category="fitness", base_discount=16, duration_hours=5),
            CouponTemplate(id="coupon_food_2", title="Chef Spotlight Menu", category="food", base_discount=24, duration_hours=3),
            CouponTemplate(id="coupon_nightlife_1", title="Late Night Cover Boost", category="nightlife", base_discount=20, duration_hours=4),
            CouponTemplate(id="coupon_art_1", title="Gallery Pass Perk", category="art", base_discount=15, duration_hours=6),
            CouponTemplate(id="coupon_events_1", title="Pop-up Priority Entry", category="events", base_discount=18, duration_hours=5),
            CouponTemplate(id="coupon_shopping_1", title="Streetwear Flash Discount", category="shopping", base_discount=12, duration_hours=4),
            CouponTemplate(id="coupon_wellness_1", title="Recharge Ritual Deal", category="wellness", base_discount=20, duration_hours=6),
            CouponTemplate(id="coupon_food_3", title="Dinner Rush Bonus", category="food", base_discount=16, duration_hours=3),
            CouponTemplate(id="coupon_coffee_4", title="Barista’s Pick", category="coffee", base_discount=18, duration_hours=3),
            CouponTemplate(id="coupon_fitness_4", title="Mobility Class Drop-in", category="fitness", base_discount=14, duration_hours=6),
        ]
        db.add_all(coupons)

