package com.generativecity.wallet.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile WalletDao _walletDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `role` TEXT NOT NULL, `username` TEXT NOT NULL, `email` TEXT NOT NULL, `token` TEXT, `hasCompletedOnboarding` INTEGER NOT NULL, `interestsCsv` TEXT NOT NULL, `explorationPreference` INTEGER NOT NULL, `companyName` TEXT, `companyCategory` TEXT, `maxDiscountPercent` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `offers` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `title` TEXT NOT NULL, `discountPercent` INTEGER NOT NULL, `distanceKm` REAL NOT NULL, `createdEpochMillis` INTEGER NOT NULL, `expiryEpochMillis` INTEGER NOT NULL, `businessName` TEXT NOT NULL, `category` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reward_inventory` (`userId` TEXT NOT NULL, `coins` INTEGER NOT NULL, `boosts` INTEGER NOT NULL, `streakDays` INTEGER NOT NULL, `lastLoginEpochMillis` INTEGER NOT NULL, `streakFreezerCount` INTEGER NOT NULL, `doubleOrNothingCount` INTEGER NOT NULL, `freeCouponCount` INTEGER NOT NULL, `timeExtensionCount` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'da4aa631d00691f12ae28a289f638877')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `offers`");
        db.execSQL("DROP TABLE IF EXISTS `reward_inventory`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(11);
        _columnsUsers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("token", new TableInfo.Column("token", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("hasCompletedOnboarding", new TableInfo.Column("hasCompletedOnboarding", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("interestsCsv", new TableInfo.Column("interestsCsv", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("explorationPreference", new TableInfo.Column("explorationPreference", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("companyName", new TableInfo.Column("companyName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("companyCategory", new TableInfo.Column("companyCategory", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("maxDiscountPercent", new TableInfo.Column("maxDiscountPercent", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.generativecity.wallet.data.local.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsOffers = new HashMap<String, TableInfo.Column>(11);
        _columnsOffers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("discountPercent", new TableInfo.Column("discountPercent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("distanceKm", new TableInfo.Column("distanceKm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("createdEpochMillis", new TableInfo.Column("createdEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("expiryEpochMillis", new TableInfo.Column("expiryEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("businessName", new TableInfo.Column("businessName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOffers.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOffers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOffers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOffers = new TableInfo("offers", _columnsOffers, _foreignKeysOffers, _indicesOffers);
        final TableInfo _existingOffers = TableInfo.read(db, "offers");
        if (!_infoOffers.equals(_existingOffers)) {
          return new RoomOpenHelper.ValidationResult(false, "offers(com.generativecity.wallet.data.local.OfferEntity).\n"
                  + " Expected:\n" + _infoOffers + "\n"
                  + " Found:\n" + _existingOffers);
        }
        final HashMap<String, TableInfo.Column> _columnsRewardInventory = new HashMap<String, TableInfo.Column>(9);
        _columnsRewardInventory.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("coins", new TableInfo.Column("coins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("boosts", new TableInfo.Column("boosts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("streakDays", new TableInfo.Column("streakDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("lastLoginEpochMillis", new TableInfo.Column("lastLoginEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("streakFreezerCount", new TableInfo.Column("streakFreezerCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("doubleOrNothingCount", new TableInfo.Column("doubleOrNothingCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("freeCouponCount", new TableInfo.Column("freeCouponCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardInventory.put("timeExtensionCount", new TableInfo.Column("timeExtensionCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRewardInventory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRewardInventory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRewardInventory = new TableInfo("reward_inventory", _columnsRewardInventory, _foreignKeysRewardInventory, _indicesRewardInventory);
        final TableInfo _existingRewardInventory = TableInfo.read(db, "reward_inventory");
        if (!_infoRewardInventory.equals(_existingRewardInventory)) {
          return new RoomOpenHelper.ValidationResult(false, "reward_inventory(com.generativecity.wallet.data.local.RewardInventoryEntity).\n"
                  + " Expected:\n" + _infoRewardInventory + "\n"
                  + " Found:\n" + _existingRewardInventory);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "da4aa631d00691f12ae28a289f638877", "3019c2bd3f0d82a4cd17bfb755491dc4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","offers","reward_inventory");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `offers`");
      _db.execSQL("DELETE FROM `reward_inventory`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(WalletDao.class, WalletDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public WalletDao walletDao() {
    if (_walletDao != null) {
      return _walletDao;
    } else {
      synchronized(this) {
        if(_walletDao == null) {
          _walletDao = new WalletDao_Impl(this);
        }
        return _walletDao;
      }
    }
  }
}
