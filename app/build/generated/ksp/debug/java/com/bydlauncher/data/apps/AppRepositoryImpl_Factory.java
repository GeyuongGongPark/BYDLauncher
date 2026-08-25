package com.bydlauncher.data.apps;

import android.content.Context;
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
public final class AppRepositoryImpl_Factory implements Factory<AppRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public AppRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static AppRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new AppRepositoryImpl_Factory(contextProvider);
  }

  public static AppRepositoryImpl newInstance(Context context) {
    return new AppRepositoryImpl(context);
  }
}
