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
public final class TranscriptionWorker_Factory {
  private final Provider<RecordingRepository> repositoryProvider;

  public TranscriptionWorker_Factory(Provider<RecordingRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public TranscriptionWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get());
  }

  public static TranscriptionWorker_Factory create(
      Provider<RecordingRepository> repositoryProvider) {
    return new TranscriptionWorker_Factory(repositoryProvider);
  }

  public static TranscriptionWorker newInstance(Context context, WorkerParameters params,
      RecordingRepository repository) {
    return new TranscriptionWorker(context, params, repository);
  }
}
