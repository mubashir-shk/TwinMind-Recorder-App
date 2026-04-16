package com.twinmind.recorder.data.repository;

import com.twinmind.recorder.data.local.dao.AudioChunkDao;
import com.twinmind.recorder.data.local.dao.SessionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class RecordingRepository_Factory implements Factory<RecordingRepository> {
  private final Provider<SessionDao> sessionDaoProvider;

  private final Provider<AudioChunkDao> audioChunkDaoProvider;

  public RecordingRepository_Factory(Provider<SessionDao> sessionDaoProvider,
      Provider<AudioChunkDao> audioChunkDaoProvider) {
    this.sessionDaoProvider = sessionDaoProvider;
    this.audioChunkDaoProvider = audioChunkDaoProvider;
  }

  @Override
  public RecordingRepository get() {
    return newInstance(sessionDaoProvider.get(), audioChunkDaoProvider.get());
  }

  public static RecordingRepository_Factory create(Provider<SessionDao> sessionDaoProvider,
      Provider<AudioChunkDao> audioChunkDaoProvider) {
    return new RecordingRepository_Factory(sessionDaoProvider, audioChunkDaoProvider);
  }

  public static RecordingRepository newInstance(SessionDao sessionDao,
      AudioChunkDao audioChunkDao) {
    return new RecordingRepository(sessionDao, audioChunkDao);
  }
}
