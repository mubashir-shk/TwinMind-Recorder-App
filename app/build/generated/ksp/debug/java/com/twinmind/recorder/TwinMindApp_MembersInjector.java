package com.twinmind.recorder;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class TwinMindApp_MembersInjector implements MembersInjector<TwinMindApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public TwinMindApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<TwinMindApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new TwinMindApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(TwinMindApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.twinmind.recorder.TwinMindApp.workerFactory")
  public static void injectWorkerFactory(TwinMindApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
