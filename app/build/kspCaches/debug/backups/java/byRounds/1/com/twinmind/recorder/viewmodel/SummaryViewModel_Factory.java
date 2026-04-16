package com.twinmind.recorder.viewmodel;

import android.content.Context;
import com.twinmind.recorder.data.repository.RecordingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SummaryViewModel_Factory implements Factory<SummaryViewModel> {
  private final Provider<RecordingRepository> repositoryProvider;

  private final Provider<Context> contextProvider;

  public SummaryViewModel_Factory(Provider<RecordingRepository> repositoryProvider,
      Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SummaryViewModel get() {
    return newInstance(repositoryProvider.get(), contextProvider.get());
  }

  public static SummaryViewModel_Factory create(Provider<RecordingRepository> repositoryProvider,
      Provider<Context> contextProvider) {
    return new SummaryViewModel_Factory(repositoryProvider, contextProvider);
  }

  public static SummaryViewModel newInstance(RecordingRepository repository, Context context) {
    return new SummaryViewModel(repository, context);
  }
}
