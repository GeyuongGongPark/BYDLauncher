package com.bydlauncher.di;

import android.content.Context;
import com.bydlauncher.camping.storage.CampingPrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CampingModule_ProvideCampingPrefsFactory implements Factory<CampingPrefs> {
  private final Provider<Context> contextProvider;

  public CampingModule_ProvideCampingPrefsFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CampingPrefs get() {
    return provideCampingPrefs(contextProvider.get());
  }

  public static CampingModule_ProvideCampingPrefsFactory create(Provider<Context> contextProvider) {
    return new CampingModule_ProvideCampingPrefsFactory(contextProvider);
  }

  public static CampingPrefs provideCampingPrefs(Context context) {
    return Preconditions.checkNotNullFromProvides(CampingModule.INSTANCE.provideCampingPrefs(context));
  }
}
