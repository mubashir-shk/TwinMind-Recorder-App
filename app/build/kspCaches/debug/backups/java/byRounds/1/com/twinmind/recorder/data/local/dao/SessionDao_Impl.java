package com.twinmind.recorder.data.local.dao;

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
import com.twinmind.recorder.data.local.Converters;
import com.twinmind.recorder.data.local.entity.SessionEntity;
import com.twinmind.recorder.data.local.entity.SessionStatus;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<SessionEntity> __updateAdapterOfSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDuration;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTranscript;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSummary;

  private final SharedSQLiteStatement __preparedStmtOfUpdateError;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSession;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sessions` (`id`,`title`,`createdAt`,`durationMs`,`status`,`transcript`,`summary`,`summaryTitle`,`actionItems`,`keyPoints`,`errorMessage`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getDurationMs());
        final String _tmp = __converters.fromStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getTranscript() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTranscript());
        }
        if (entity.getSummary() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSummary());
        }
        if (entity.getSummaryTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSummaryTitle());
        }
        if (entity.getActionItems() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getActionItems());
        }
        if (entity.getKeyPoints() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getKeyPoints());
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getErrorMessage());
        }
      }
    };
    this.__updateAdapterOfSessionEntity = new EntityDeletionOrUpdateAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sessions` SET `id` = ?,`title` = ?,`createdAt` = ?,`durationMs` = ?,`status` = ?,`transcript` = ?,`summary` = ?,`summaryTitle` = ?,`actionItems` = ?,`keyPoints` = ?,`errorMessage` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getDurationMs());
        final String _tmp = __converters.fromStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getTranscript() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTranscript());
        }
        if (entity.getSummary() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSummary());
        }
        if (entity.getSummaryTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSummaryTitle());
        }
        if (entity.getActionItems() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getActionItems());
        }
        if (entity.getKeyPoints() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getKeyPoints());
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getErrorMessage());
        }
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateDuration = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET durationMs = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTranscript = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET transcript = ?, status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSummary = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET summary = ?, summaryTitle = ?, actionItems = ?, keyPoints = ?, status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateError = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET errorMessage = ?, status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final SessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final SessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final String id, final SessionStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromStatus(status);
        _stmt.bindString(_argIndex, _tmp);
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDuration(final String id, final long durationMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDuration.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, durationMs);
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
          __preparedStmtOfUpdateDuration.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTranscript(final String id, final String transcript,
      final SessionStatus status, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTranscript.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, transcript);
        _argIndex = 2;
        final String _tmp = __converters.fromStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 3;
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
          __preparedStmtOfUpdateTranscript.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSummary(final String id, final String summary, final String title,
      final String actionItems, final String keyPoints, final SessionStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSummary.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, summary);
        _argIndex = 2;
        _stmt.bindString(_argIndex, title);
        _argIndex = 3;
        _stmt.bindString(_argIndex, actionItems);
        _argIndex = 4;
        _stmt.bindString(_argIndex, keyPoints);
        _argIndex = 5;
        final String _tmp = __converters.fromStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 6;
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
          __preparedStmtOfUpdateSummary.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateError(final String id, final String error, final SessionStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateError.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, error);
        _argIndex = 2;
        final String _tmp = __converters.fromStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 3;
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
          __preparedStmtOfUpdateError.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSession.acquire();
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
          __preparedStmtOfDeleteSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM sessions ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sessions"}, new Callable<List<SessionEntity>>() {
      @Override
      @NonNull
      public List<SessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfSummaryTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryTitle");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final SessionStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatus(_tmp);
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final String _tmpSummaryTitle;
            if (_cursor.isNull(_cursorIndexOfSummaryTitle)) {
              _tmpSummaryTitle = null;
            } else {
              _tmpSummaryTitle = _cursor.getString(_cursorIndexOfSummaryTitle);
            }
            final String _tmpActionItems;
            if (_cursor.isNull(_cursorIndexOfActionItems)) {
              _tmpActionItems = null;
            } else {
              _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            }
            final String _tmpKeyPoints;
            if (_cursor.isNull(_cursorIndexOfKeyPoints)) {
              _tmpKeyPoints = null;
            } else {
              _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            _item = new SessionEntity(_tmpId,_tmpTitle,_tmpCreatedAt,_tmpDurationMs,_tmpStatus,_tmpTranscript,_tmpSummary,_tmpSummaryTitle,_tmpActionItems,_tmpKeyPoints,_tmpErrorMessage);
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
  public Flow<SessionEntity> getSessionById(final String id) {
    final String _sql = "SELECT * FROM sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sessions"}, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfSummaryTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryTitle");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final SessionStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatus(_tmp);
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final String _tmpSummaryTitle;
            if (_cursor.isNull(_cursorIndexOfSummaryTitle)) {
              _tmpSummaryTitle = null;
            } else {
              _tmpSummaryTitle = _cursor.getString(_cursorIndexOfSummaryTitle);
            }
            final String _tmpActionItems;
            if (_cursor.isNull(_cursorIndexOfActionItems)) {
              _tmpActionItems = null;
            } else {
              _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            }
            final String _tmpKeyPoints;
            if (_cursor.isNull(_cursorIndexOfKeyPoints)) {
              _tmpKeyPoints = null;
            } else {
              _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            _result = new SessionEntity(_tmpId,_tmpTitle,_tmpCreatedAt,_tmpDurationMs,_tmpStatus,_tmpTranscript,_tmpSummary,_tmpSummaryTitle,_tmpActionItems,_tmpKeyPoints,_tmpErrorMessage);
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
  public Object getSessionByIdOnce(final String id,
      final Continuation<? super SessionEntity> $completion) {
    final String _sql = "SELECT * FROM sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfSummaryTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryTitle");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final SessionStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatus(_tmp);
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final String _tmpSummaryTitle;
            if (_cursor.isNull(_cursorIndexOfSummaryTitle)) {
              _tmpSummaryTitle = null;
            } else {
              _tmpSummaryTitle = _cursor.getString(_cursorIndexOfSummaryTitle);
            }
            final String _tmpActionItems;
            if (_cursor.isNull(_cursorIndexOfActionItems)) {
              _tmpActionItems = null;
            } else {
              _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            }
            final String _tmpKeyPoints;
            if (_cursor.isNull(_cursorIndexOfKeyPoints)) {
              _tmpKeyPoints = null;
            } else {
              _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            _result = new SessionEntity(_tmpId,_tmpTitle,_tmpCreatedAt,_tmpDurationMs,_tmpStatus,_tmpTranscript,_tmpSummary,_tmpSummaryTitle,_tmpActionItems,_tmpKeyPoints,_tmpErrorMessage);
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
  public Object getSessionByStatus(final SessionStatus status,
      final Continuation<? super List<SessionEntity>> $completion) {
    final String _sql = "SELECT * FROM sessions WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromStatus(status);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionEntity>>() {
      @Override
      @NonNull
      public List<SessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfSummaryTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryTitle");
          final int _cursorIndexOfActionItems = CursorUtil.getColumnIndexOrThrow(_cursor, "actionItems");
          final int _cursorIndexOfKeyPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "keyPoints");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final SessionStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toStatus(_tmp_1);
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final String _tmpSummaryTitle;
            if (_cursor.isNull(_cursorIndexOfSummaryTitle)) {
              _tmpSummaryTitle = null;
            } else {
              _tmpSummaryTitle = _cursor.getString(_cursorIndexOfSummaryTitle);
            }
            final String _tmpActionItems;
            if (_cursor.isNull(_cursorIndexOfActionItems)) {
              _tmpActionItems = null;
            } else {
              _tmpActionItems = _cursor.getString(_cursorIndexOfActionItems);
            }
            final String _tmpKeyPoints;
            if (_cursor.isNull(_cursorIndexOfKeyPoints)) {
              _tmpKeyPoints = null;
            } else {
              _tmpKeyPoints = _cursor.getString(_cursorIndexOfKeyPoints);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            _item = new SessionEntity(_tmpId,_tmpTitle,_tmpCreatedAt,_tmpDurationMs,_tmpStatus,_tmpTranscript,_tmpSummary,_tmpSummaryTitle,_tmpActionItems,_tmpKeyPoints,_tmpErrorMessage);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
