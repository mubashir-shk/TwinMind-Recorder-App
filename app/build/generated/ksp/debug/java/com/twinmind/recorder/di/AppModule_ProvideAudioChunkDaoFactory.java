package com.twinmind.recorder.di;

import com.twinmind.recorder.data.local.AppDatabase;
import com.twinmind.recorder.data.local.dao.AudioChunkDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideAudioChunkDaoFactory implements Factory<AudioChunkDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAudioChunkDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AudioChunkDao get() {
    return provideAudioChunkDao(dbProvider.get());
  }

  public static AppModule_ProvideAudioChunkDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAudioChunkDaoFactory(dbProvider);
  }

  public static AudioChunkDao provideAudioChunkDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAudioChunkDao(db));
  }
}
