package com.rpn.blockblaster.`data`.local.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.rpn.blockblaster.`data`.local.db.entity.ScoreEntity
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ScoreDao_Impl(
  __db: RoomDatabase,
) : ScoreDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfScoreEntity: EntityInsertAdapter<ScoreEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfScoreEntity = object : EntityInsertAdapter<ScoreEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `scores` (`id`,`score`,`timestamp`,`linesBlasted`,`crossBlasts`,`bestCombo`,`blocksPlaced`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ScoreEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.score.toLong())
        statement.bindLong(3, entity.timestamp)
        statement.bindLong(4, entity.linesBlasted.toLong())
        statement.bindLong(5, entity.crossBlasts.toLong())
        statement.bindDouble(6, entity.bestCombo.toDouble())
        statement.bindLong(7, entity.blocksPlaced.toLong())
      }
    }
  }

  public override suspend fun insert(score: ScoreEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfScoreEntity.insert(_connection, score)
  }

  public override suspend fun getBestScore(): Int? {
    val _sql: String = "SELECT MAX(score) FROM scores"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopScores(): List<ScoreEntity> {
    val _sql: String = "SELECT * FROM scores ORDER BY score DESC LIMIT 10"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfLinesBlasted: Int = getColumnIndexOrThrow(_stmt, "linesBlasted")
        val _columnIndexOfCrossBlasts: Int = getColumnIndexOrThrow(_stmt, "crossBlasts")
        val _columnIndexOfBestCombo: Int = getColumnIndexOrThrow(_stmt, "bestCombo")
        val _columnIndexOfBlocksPlaced: Int = getColumnIndexOrThrow(_stmt, "blocksPlaced")
        val _result: MutableList<ScoreEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ScoreEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpLinesBlasted: Int
          _tmpLinesBlasted = _stmt.getLong(_columnIndexOfLinesBlasted).toInt()
          val _tmpCrossBlasts: Int
          _tmpCrossBlasts = _stmt.getLong(_columnIndexOfCrossBlasts).toInt()
          val _tmpBestCombo: Float
          _tmpBestCombo = _stmt.getDouble(_columnIndexOfBestCombo).toFloat()
          val _tmpBlocksPlaced: Int
          _tmpBlocksPlaced = _stmt.getLong(_columnIndexOfBlocksPlaced).toInt()
          _item = ScoreEntity(_tmpId,_tmpScore,_tmpTimestamp,_tmpLinesBlasted,_tmpCrossBlasts,_tmpBestCombo,_tmpBlocksPlaced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM scores"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
