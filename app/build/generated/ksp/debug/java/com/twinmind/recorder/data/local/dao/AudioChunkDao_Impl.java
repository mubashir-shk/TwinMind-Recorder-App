package com.twinmind.recorder.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.twinmind.recorder.data.local.entity.AudioChunkEntity;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AudioChunkDao_Impl implements AudioChunkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AudioChunkEntity> __insertionAdapterOfAudioChunkEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkTranscribed;

  private final SharedSQLiteStatement __preparedStmtOfIncrementRetry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteChunksForSession;

  public AudioChunkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAudioChunkEntity = new EntityInsertionAdapter<AudioChunkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `audio_chunks` (`id`,`sessionId`,`filePath`,`chunkIndex`,`createdAt`,`durationMs`,`isTranscribed`,`transcript`,`retryCount`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AudioChunkEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getFilePath());
        statement.bindLong(4, entity.getChunkIndex());
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindLong(6, entity.getDurationMs());
        final int _tmp = entity.isTranscribed() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getTranscript() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getTranscript());
        }
        statement.bindLong(9, entity.getRetryCount());
      }
    };
    this.__preparedStmtOfMarkTranscribed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE audio_chunks SET isTranscribed = 1, transcript = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementRetry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE audio_chunks SET retryCount = retryCount + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteChunksForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM audio_chunks WHERE sessionId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertChunk(final AudioChunkEntity chunk,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAudioChunkEntity.insert(chunk);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markTranscribed(final String id, final String transcript,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkTranscribed.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, transcript);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfMarkTranscribed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementRetry(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementRetry.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfIncrementRetry.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChunksForSession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteChunksForSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
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
          __preparedStmtOfDeleteChunksForSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getChunksForSession(final String sessionId,
      final Continuation<? super List<AudioChunkEntity>> $completion) {
    final String _sql = "SELECT * FROM audio_chunks WHERE sessionId = ? ORDER BY chunkIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AudioChunkEntity>>() {
      @Override
      @NonNull
      public List<AudioChunkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfChunkIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "chunkIndex");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfIsTranscribed = CursorUtil.getColumnIndexOrThrow(_cursor, "isTranscribed");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<AudioChunkEntity> _result = new ArrayList<AudioChunkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AudioChunkEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final int _tmpChunkIndex;
            _tmpChunkIndex = _cursor.getInt(_cursorIndexOfChunkIndex);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final boolean _tmpIsTranscribed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTranscribed);
            _tmpIsTranscribed = _tmp != 0;
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new AudioChunkEntity(_tmpId,_tmpSessionId,_tmpFilePath,_tmpChunkIndex,_tmpCreatedAt,_tmpDurationMs,_tmpIsTranscribed,_tmpTranscript,_tmpRetryCount);
            _result.add(_item);
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
  public Object getUntranscribedChunks(final String sessionId,
      final Continuation<? super List<AudioChunkEntity>> $completion) {
    final String _sql = "SELECT * FROM audio_chunks WHERE sessionId = ? AND isTranscribed = 0 ORDER BY chunkIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AudioChunkEntity>>() {
      @Override
      @NonNull
      public List<AudioChunkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfChunkIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "chunkIndex");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfIsTranscribed = CursorUtil.getColumnIndexOrThrow(_cursor, "isTranscribed");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final List<AudioChunkEntity> _result = new ArrayList<AudioChunkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AudioChunkEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final int _tmpChunkIndex;
            _tmpChunkIndex = _cursor.getInt(_cursorIndexOfChunkIndex);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final boolean _tmpIsTranscribed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTranscribed);
            _tmpIsTranscribed = _tmp != 0;
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _item = new AudioChunkEntity(_tmpId,_tmpSessionId,_tmpFilePath,_tmpChunkIndex,_tmpCreatedAt,_tmpDurationMs,_tmpIsTranscribed,_tmpTranscript,_tmpRetryCount);
            _result.add(_item);
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
  public Object getById(final String id, final Continuation<? super AudioChunkEntity> $completion) {
    final String _sql = "SELECT * FROM audio_chunks WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AudioChunkEntity>() {
      @Override
      @Nullable
      public AudioChunkEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfChunkIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "chunkIndex");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfIsTranscribed = CursorUtil.getColumnIndexOrThrow(_cursor, "isTranscribed");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final AudioChunkEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final int _tmpChunkIndex;
            _tmpChunkIndex = _cursor.getInt(_cursorIndexOfChunkIndex);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final boolean _tmpIsTranscribed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTranscribed);
            _tmpIsTranscribed = _tmp != 0;
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            _result = new AudioChunkEntity(_tmpId,_tmpSessionId,_tmpFilePath,_tmpChunkIndex,_tmpCreatedAt,_tmpDurationMs,_tmpIsTranscribed,_tmpTranscript,_tmpRetryCount);
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
