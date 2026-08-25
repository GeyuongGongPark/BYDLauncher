package com.bydlauncher.data.favorites;

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
public final class FavoritesRepositoryImpl_Factory implements Factory<FavoritesRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public FavoritesRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FavoritesRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static FavoritesRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new FavoritesRepositoryImpl_Factory(contextProvider);
  }

  public static FavoritesRepositoryImpl newInstance(Context context) {
    return new FavoritesRepositoryImpl(context);
  }
}
