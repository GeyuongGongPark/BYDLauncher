package com.bydlauncher.ui.home;

import com.bydlauncher.domain.apps.AppRepository;
import com.bydlauncher.domain.favorites.FavoritesRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<FavoritesRepository> favoritesRepositoryProvider;

  private final Provider<AppRepository> appRepositoryProvider;

  public HomeViewModel_Factory(Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<AppRepository> appRepositoryProvider) {
    this.favoritesRepositoryProvider = favoritesRepositoryProvider;
    this.appRepositoryProvider = appRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(favoritesRepositoryProvider.get(), appRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<FavoritesRepository> favoritesRepositoryProvider,
      Provider<AppRepository> appRepositoryProvider) {
    return new HomeViewModel_Factory(favoritesRepositoryProvider, appRepositoryProvider);
  }

  public static HomeViewModel newInstance(FavoritesRepository favoritesRepository,
      AppRepository appRepository) {
    return new HomeViewModel(favoritesRepository, appRepository);
  }
}
