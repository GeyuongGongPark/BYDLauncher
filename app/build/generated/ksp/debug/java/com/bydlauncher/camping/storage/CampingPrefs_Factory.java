package com.bydlauncher.camping.storage;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CampingPrefs_Factory implements Factory<CampingPrefs> {
  private final Provider<Context> contextProvider;

  public CampingPrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CampingPrefs get() {
    return newInstance(contextProvider.get());
  }

  public static CampingPrefs_Factory create(Provider<Context> contextProvider) {
    return new CampingPrefs_Factory(contextProvider);
  }

  public static CampingPrefs newInstance(Context context) {
    return new CampingPrefs(context);
  }
}
