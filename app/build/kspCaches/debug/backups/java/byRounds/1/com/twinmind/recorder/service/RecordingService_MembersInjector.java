package com.twinmind.recorder.service;

import com.twinmind.recorder.data.repository.RecordingRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RecordingService_MembersInjector implements MembersInjector<RecordingService> {
  private final Provider<RecordingRepository> repositoryProvider;

  public RecordingService_MembersInjector(Provider<RecordingRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<RecordingService> create(
      Provider<RecordingRepository> repositoryProvider) {
    return new RecordingService_MembersInjector(repositoryProvider);
  }

  @Override
  public void injectMembers(RecordingService instance) {
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.twinmind.recorder.service.RecordingService.repository")
  public static void injectRepository(RecordingService instance, RecordingRepository repository) {
    instance.repository = repository;
  }
}
