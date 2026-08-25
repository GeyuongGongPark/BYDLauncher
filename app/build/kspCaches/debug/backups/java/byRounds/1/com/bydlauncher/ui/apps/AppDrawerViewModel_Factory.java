package com.bydlauncher.ui.apps;

import com.bydlauncher.domain.apps.AppRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppDrawerViewModel_Factory implements Factory<AppDrawerViewModel> {
  private final Provider<AppRepository> appRepositoryProvider;

  public AppDrawerViewModel_Factory(Provider<AppRepository> appRepositoryProvider) {
    this.appRepositoryProvider = appRepositoryProvider;
  }

  @Override
  public AppDrawerViewModel get() {
    return newInstance(appRepositoryProvider.get());
  }

  public static AppDrawerViewModel_Factory create(Provider<AppRepository> appRepositoryProvider) {
    return new AppDrawerViewModel_Factory(appRepositoryProvider);
  }

  public static AppDrawerViewModel newInstance(AppRepository appRepository) {
    return new AppDrawerViewModel(appRepository);
  }
}
