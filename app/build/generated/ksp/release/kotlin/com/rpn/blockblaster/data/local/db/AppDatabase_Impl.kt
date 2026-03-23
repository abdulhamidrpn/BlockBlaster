package com.rpn.blockblaster.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _scoreDao: Lazy<ScoreDao> = lazy {
    ScoreDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "576ddf73d637f6aa1131a37f03fb79b3", "f68be8db6f34faa1496c4af9d585dbe3") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `scores` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `score` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `linesBlasted` INTEGER NOT NULL, `crossBlasts` INTEGER NOT NULL, `bestCombo` REAL NOT NULL, `blocksPlaced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '576ddf73d637f6aa1131a37f03fb79b3')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `scores`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsScores: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsScores.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("score", TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("linesBlasted", TableInfo.Column("linesBlasted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("crossBlasts", TableInfo.Column("crossBlasts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("bestCombo", TableInfo.Column("bestCombo", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScores.put("blocksPlaced", TableInfo.Column("blocksPlaced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysScores: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesScores: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoScores: TableInfo = TableInfo("scores", _columnsScores, _foreignKeysScores, _indicesScores)
        val _existingScores: TableInfo = read(connection, "scores")
        if (!_infoScores.equals(_existingScores)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |scores(com.rpn.blockblaster.data.local.db.entity.ScoreEntity).
              | Expected:
              |""".trimMargin() + _infoScores + """
              |
              | Found:
              |""".trimMargin() + _existingScores)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "scores")
  }

  public override fun clearAllTables() {
    super.performClear(false, "scores")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ScoreDao::class, ScoreDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun scoreDao(): ScoreDao = _scoreDao.value
}
