package com.bydlauncher.ui.camping;

import android.content.Context;
import com.bydlauncher.camping.storage.CampingPrefs;
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
public final class CampingViewModel_Factory implements Factory<CampingViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<CampingPrefs> prefsProvider;

  public CampingViewModel_Factory(Provider<Context> contextProvider,
      Provider<CampingPrefs> prefsProvider) {
    this.contextProvider = contextProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public CampingViewModel get() {
    return newInstance(contextProvider.get(), prefsProvider.get());
  }

  public static CampingViewModel_Factory create(Provider<Context> contextProvider,
      Provider<CampingPrefs> prefsProvider) {
    return new CampingViewModel_Factory(contextProvider, prefsProvider);
  }

  public static CampingViewModel newInstance(Context context, CampingPrefs prefs) {
    return new CampingViewModel(context, prefs);
  }
}
