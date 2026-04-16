package com.twinmind.recorder.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.twinmind.recorder.data.repository.RecordingRepository;
import dagger.internal.DaggerGenerated;
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
public final class SessionFinalizerWorker_Factory {
  private final Provider<RecordingRepository> repositoryProvider;

  public SessionFinalizerWorker_Factory(Provider<RecordingRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public SessionFinalizerWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get());
  }

  public static SessionFinalizerWorker_Factory create(
      Provider<RecordingRepository> repositoryProvider) {
    return new SessionFinalizerWorker_Factory(repositoryProvider);
  }

  public static SessionFinalizerWorker newInstance(Context context, WorkerParameters params,
      RecordingRepository repository) {
    return new SessionFinalizerWorker(context, params, repository);
  }
}
