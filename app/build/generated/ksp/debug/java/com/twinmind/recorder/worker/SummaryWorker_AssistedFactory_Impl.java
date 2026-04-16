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
public final class SummaryWorker_AssistedFactory_Impl implements SummaryWorker_AssistedFactory {
  private final SummaryWorker_Factory delegateFactory;

  SummaryWorker_AssistedFactory_Impl(SummaryWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public SummaryWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<SummaryWorker_AssistedFactory> create(
      SummaryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SummaryWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<SummaryWorker_AssistedFactory> createFactoryProvider(
      SummaryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SummaryWorker_AssistedFactory_Impl(delegateFactory));
  }
}
