package com.bydlauncher.data.navi;

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
public final class NaviRepositoryImpl_Factory implements Factory<NaviRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public NaviRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NaviRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static NaviRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new NaviRepositoryImpl_Factory(contextProvider);
  }

  public static NaviRepositoryImpl newInstance(Context context) {
    return new NaviRepositoryImpl(context);
  }
}
