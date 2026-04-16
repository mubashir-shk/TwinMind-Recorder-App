package com.twinmind.recorder.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SessionFinalizerWorker_AssistedFactory_Impl implements SessionFinalizerWorker_AssistedFactory {
  private final SessionFinalizerWorker_Factory delegateFactory;

  SessionFinalizerWorker_AssistedFactory_Impl(SessionFinalizerWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public SessionFinalizerWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<SessionFinalizerWorker_AssistedFactory> create(
      SessionFinalizerWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SessionFinalizerWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<SessionFinalizerWorker_AssistedFactory> createFactoryProvider(
      SessionFinalizerWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SessionFinalizerWorker_AssistedFactory_Impl(delegateFactory));
  }
}
