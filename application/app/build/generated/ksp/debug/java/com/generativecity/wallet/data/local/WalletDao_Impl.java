package com.generativecity.wallet.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.generativecity.wallet.data.model.OfferStatus;
import com.generativecity.wallet.data.model.UserRole;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WalletDao_Impl implements WalletDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserEntity> __insertionAdapterOfUserEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<OfferEntity> __insertionAdapterOfOfferEntity;

  private final EntityInsertionAdapter<RewardInventoryEntity> __insertionAdapterOfRewardInventoryEntity;

  private final EntityDeletionOrUpdateAdapter<OfferEntity> __updateAdapterOfOfferEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearUsers;

  private final SharedSQLiteStatement __preparedStmtOfClearOffers;

  private final SharedSQLiteStatement __preparedStmtOfClearInventory;

  public WalletDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserEntity = new EntityInsertionAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`id`,`role`,`username`,`interestsCsv`,`explorationPreference`,`companyName`,`companyCategory`,`maxDiscountPercent`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindLong(1, entity.getId());
        final String _tmp = __converters.fromUserRole(entity.getRole());
        statement.bindString(2, _tmp);
        statement.bindString(3, entity.getUsername());
        statement.bindString(4, entity.getInterestsCsv());
        statement.bindLong(5, entity.getExplorationPreference());
        if (entity.getCompanyName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCompanyName());
        }
        if (entity.getCompanyCategory() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCompanyCategory());
        }
        if (entity.getMaxDiscountPercent() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getMaxDiscountPercent());
        }
      }
    };
    this.__insertionAdapterOfOfferEntity = new EntityInsertionAdapter<OfferEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `offers` (`id`,`userId`,`title`,`discountPercent`,`distanceKm`,`createdEpochMillis`,`expiryEpochMillis`,`businessName`,`category`,`imageUrl`,`status`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OfferEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getTitle());
        statement.bindLong(4, entity.getDiscountPercent());
        statement.bindDouble(5, entity.getDistanceKm());
        statement.bindLong(6, entity.getCreatedEpochMillis());
        statement.bindLong(7, entity.getExpiryEpochMillis());
        statement.bindString(8, entity.getBusinessName());
        statement.bindString(9, entity.getCategory());
        statement.bindString(10, entity.getImageUrl());
        final String _tmp = __converters.fromOfferStatus(entity.getStatus());
        statement.bindString(11, _tmp);
      }
    };
    this.__insertionAdapterOfRewardInventoryEntity = new EntityInsertionAdapter<RewardInventoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reward_inventory` (`userId`,`coins`,`boosts`,`streakDays`,`lastLoginEpochMillis`,`streakFreezerCount`,`doubleOrNothingCount`,`freeCouponCount`,`timeExtensionCount`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RewardInventoryEntity entity) {
        statement.bindLong(1, entity.getUserId());
        statement.bindLong(2, entity.getCoins());
        statement.bindLong(3, entity.getBoosts());
        statement.bindLong(4, entity.getStreakDays());
        statement.bindLong(5, entity.getLastLoginEpochMillis());
        statement.bindLong(6, entity.getStreakFreezerCount());
        statement.bindLong(7, entity.getDoubleOrNothingCount());
        statement.bindLong(8, entity.getFreeCouponCount());
        statement.bindLong(9, entity.getTimeExtensionCount());
      }
    };
    this.__updateAdapterOfOfferEntity = new EntityDeletionOrUpdateAdapter<OfferEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `offers` SET `id` = ?,`userId` = ?,`title` = ?,`discountPercent` = ?,`distanceKm` = ?,`createdEpochMillis` = ?,`expiryEpochMillis` = ?,`businessName` = ?,`category` = ?,`imageUrl` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OfferEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getTitle());
        statement.bindLong(4, entity.getDiscountPercent());
        statement.bindDouble(5, entity.getDistanceKm());
        statement.bindLong(6, entity.getCreatedEpochMillis());
        statement.bindLong(7, entity.getExpiryEpochMillis());
        statement.bindString(8, entity.getBusinessName());
        statement.bindString(9, entity.getCategory());
        statement.bindString(10, entity.getImageUrl());
        final String _tmp = __converters.fromOfferStatus(entity.getStatus());
        statement.bindString(11, _tmp);
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfClearUsers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users";
        return _query;
      }
    };
    this.__preparedStmtOfClearOffers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM offers";
        return _query;
      }
    };
    this.__preparedStmtOfClearInventory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reward_inventory";
        return _query;
      }
    };
  }

  @Override
  public Object upsertUser(final UserEntity user, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfUserEntity.insertAndReturnId(user);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOffers(final List<OfferEntity> offers,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfOfferEntity.insert(offers);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertOffer(final OfferEntity offer, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfOfferEntity.insert(offer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertInventory(final RewardInventoryEntity inventory,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRewardInventoryEntity.insert(inventory);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateOffer(final OfferEntity offer, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfOfferEntity.handle(offer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearUsers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearUsers.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearUsers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearOffers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearOffers.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearOffers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearInventory(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearInventory.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearInventory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserEntity> observeLatestUser() {
    final String _sql = "SELECT * FROM users ORDER BY id DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"users"}, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfInterestsCsv = CursorUtil.getColumnIndexOrThrow(_cursor, "interestsCsv");
          final int _cursorIndexOfExplorationPreference = CursorUtil.getColumnIndexOrThrow(_cursor, "explorationPreference");
          final int _cursorIndexOfCompanyName = CursorUtil.getColumnIndexOrThrow(_cursor, "companyName");
          final int _cursorIndexOfCompanyCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "companyCategory");
          final int _cursorIndexOfMaxDiscountPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "maxDiscountPercent");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final UserRole _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.toUserRole(_tmp);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final String _tmpInterestsCsv;
            _tmpInterestsCsv = _cursor.getString(_cursorIndexOfInterestsCsv);
            final int _tmpExplorationPreference;
            _tmpExplorationPreference = _cursor.getInt(_cursorIndexOfExplorationPreference);
            final String _tmpCompanyName;
            if (_cursor.isNull(_cursorIndexOfCompanyName)) {
              _tmpCompanyName = null;
            } else {
              _tmpCompanyName = _cursor.getString(_cursorIndexOfCompanyName);
            }
            final String _tmpCompanyCategory;
            if (_cursor.isNull(_cursorIndexOfCompanyCategory)) {
              _tmpCompanyCategory = null;
            } else {
              _tmpCompanyCategory = _cursor.getString(_cursorIndexOfCompanyCategory);
            }
            final Integer _tmpMaxDiscountPercent;
            if (_cursor.isNull(_cursorIndexOfMaxDiscountPercent)) {
              _tmpMaxDiscountPercent = null;
            } else {
              _tmpMaxDiscountPercent = _cursor.getInt(_cursorIndexOfMaxDiscountPercent);
            }
            _result = new UserEntity(_tmpId,_tmpRole,_tmpUsername,_tmpInterestsCsv,_tmpExplorationPreference,_tmpCompanyName,_tmpCompanyCategory,_tmpMaxDiscountPercent);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<OfferEntity>> observeOffersForUser(final int userId) {
    final String _sql = "SELECT * FROM offers WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"offers"}, new Callable<List<OfferEntity>>() {
      @Override
      @NonNull
      public List<OfferEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDiscountPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "discountPercent");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfCreatedEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdEpochMillis");
          final int _cursorIndexOfExpiryEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryEpochMillis");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<OfferEntity> _result = new ArrayList<OfferEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OfferEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpDiscountPercent;
            _tmpDiscountPercent = _cursor.getInt(_cursorIndexOfDiscountPercent);
            final double _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            final long _tmpCreatedEpochMillis;
            _tmpCreatedEpochMillis = _cursor.getLong(_cursorIndexOfCreatedEpochMillis);
            final long _tmpExpiryEpochMillis;
            _tmpExpiryEpochMillis = _cursor.getLong(_cursorIndexOfExpiryEpochMillis);
            final String _tmpBusinessName;
            _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final OfferStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toOfferStatus(_tmp);
            _item = new OfferEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDiscountPercent,_tmpDistanceKm,_tmpCreatedEpochMillis,_tmpExpiryEpochMillis,_tmpBusinessName,_tmpCategory,_tmpImageUrl,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getOfferById(final String offerId,
      final Continuation<? super OfferEntity> $completion) {
    final String _sql = "SELECT * FROM offers WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, offerId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<OfferEntity>() {
      @Override
      @Nullable
      public OfferEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDiscountPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "discountPercent");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfCreatedEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "createdEpochMillis");
          final int _cursorIndexOfExpiryEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryEpochMillis");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final OfferEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpDiscountPercent;
            _tmpDiscountPercent = _cursor.getInt(_cursorIndexOfDiscountPercent);
            final double _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            final long _tmpCreatedEpochMillis;
            _tmpCreatedEpochMillis = _cursor.getLong(_cursorIndexOfCreatedEpochMillis);
            final long _tmpExpiryEpochMillis;
            _tmpExpiryEpochMillis = _cursor.getLong(_cursorIndexOfExpiryEpochMillis);
            final String _tmpBusinessName;
            _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final OfferStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toOfferStatus(_tmp);
            _result = new OfferEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDiscountPercent,_tmpDistanceKm,_tmpCreatedEpochMillis,_tmpExpiryEpochMillis,_tmpBusinessName,_tmpCategory,_tmpImageUrl,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<RewardInventoryEntity> observeInventory(final int userId) {
    final String _sql = "SELECT * FROM reward_inventory WHERE userId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reward_inventory"}, new Callable<RewardInventoryEntity>() {
      @Override
      @Nullable
      public RewardInventoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "coins");
          final int _cursorIndexOfBoosts = CursorUtil.getColumnIndexOrThrow(_cursor, "boosts");
          final int _cursorIndexOfStreakDays = CursorUtil.getColumnIndexOrThrow(_cursor, "streakDays");
          final int _cursorIndexOfLastLoginEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginEpochMillis");
          final int _cursorIndexOfStreakFreezerCount = CursorUtil.getColumnIndexOrThrow(_cursor, "streakFreezerCount");
          final int _cursorIndexOfDoubleOrNothingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "doubleOrNothingCount");
          final int _cursorIndexOfFreeCouponCount = CursorUtil.getColumnIndexOrThrow(_cursor, "freeCouponCount");
          final int _cursorIndexOfTimeExtensionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "timeExtensionCount");
          final RewardInventoryEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final int _tmpCoins;
            _tmpCoins = _cursor.getInt(_cursorIndexOfCoins);
            final int _tmpBoosts;
            _tmpBoosts = _cursor.getInt(_cursorIndexOfBoosts);
            final int _tmpStreakDays;
            _tmpStreakDays = _cursor.getInt(_cursorIndexOfStreakDays);
            final long _tmpLastLoginEpochMillis;
            _tmpLastLoginEpochMillis = _cursor.getLong(_cursorIndexOfLastLoginEpochMillis);
            final int _tmpStreakFreezerCount;
            _tmpStreakFreezerCount = _cursor.getInt(_cursorIndexOfStreakFreezerCount);
            final int _tmpDoubleOrNothingCount;
            _tmpDoubleOrNothingCount = _cursor.getInt(_cursorIndexOfDoubleOrNothingCount);
            final int _tmpFreeCouponCount;
            _tmpFreeCouponCount = _cursor.getInt(_cursorIndexOfFreeCouponCount);
            final int _tmpTimeExtensionCount;
            _tmpTimeExtensionCount = _cursor.getInt(_cursorIndexOfTimeExtensionCount);
            _result = new RewardInventoryEntity(_tmpUserId,_tmpCoins,_tmpBoosts,_tmpStreakDays,_tmpLastLoginEpochMillis,_tmpStreakFreezerCount,_tmpDoubleOrNothingCount,_tmpFreeCouponCount,_tmpTimeExtensionCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getInventoryByUserId(final int userId,
      final Continuation<? super RewardInventoryEntity> $completion) {
    final String _sql = "SELECT * FROM reward_inventory WHERE userId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RewardInventoryEntity>() {
      @Override
      @Nullable
      public RewardInventoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "coins");
          final int _cursorIndexOfBoosts = CursorUtil.getColumnIndexOrThrow(_cursor, "boosts");
          final int _cursorIndexOfStreakDays = CursorUtil.getColumnIndexOrThrow(_cursor, "streakDays");
          final int _cursorIndexOfLastLoginEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginEpochMillis");
          final int _cursorIndexOfStreakFreezerCount = CursorUtil.getColumnIndexOrThrow(_cursor, "streakFreezerCount");
          final int _cursorIndexOfDoubleOrNothingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "doubleOrNothingCount");
          final int _cursorIndexOfFreeCouponCount = CursorUtil.getColumnIndexOrThrow(_cursor, "freeCouponCount");
          final int _cursorIndexOfTimeExtensionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "timeExtensionCount");
          final RewardInventoryEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final int _tmpCoins;
            _tmpCoins = _cursor.getInt(_cursorIndexOfCoins);
            final int _tmpBoosts;
            _tmpBoosts = _cursor.getInt(_cursorIndexOfBoosts);
            final int _tmpStreakDays;
            _tmpStreakDays = _cursor.getInt(_cursorIndexOfStreakDays);
            final long _tmpLastLoginEpochMillis;
            _tmpLastLoginEpochMillis = _cursor.getLong(_cursorIndexOfLastLoginEpochMillis);
            final int _tmpStreakFreezerCount;
            _tmpStreakFreezerCount = _cursor.getInt(_cursorIndexOfStreakFreezerCount);
            final int _tmpDoubleOrNothingCount;
            _tmpDoubleOrNothingCount = _cursor.getInt(_cursorIndexOfDoubleOrNothingCount);
            final int _tmpFreeCouponCount;
            _tmpFreeCouponCount = _cursor.getInt(_cursorIndexOfFreeCouponCount);
            final int _tmpTimeExtensionCount;
            _tmpTimeExtensionCount = _cursor.getInt(_cursorIndexOfTimeExtensionCount);
            _result = new RewardInventoryEntity(_tmpUserId,_tmpCoins,_tmpBoosts,_tmpStreakDays,_tmpLastLoginEpochMillis,_tmpStreakFreezerCount,_tmpDoubleOrNothingCount,_tmpFreeCouponCount,_tmpTimeExtensionCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
