package com.bydlauncher.data.weather;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class WeatherRepositoryImpl_Factory implements Factory<WeatherRepositoryImpl> {
  private final Provider<OkHttpClient> clientProvider;

  public WeatherRepositoryImpl_Factory(Provider<OkHttpClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  @Override
  public WeatherRepositoryImpl get() {
    return newInstance(clientProvider.get());
  }

  public static WeatherRepositoryImpl_Factory create(Provider<OkHttpClient> clientProvider) {
    return new WeatherRepositoryImpl_Factory(clientProvider);
  }

  public static WeatherRepositoryImpl newInstance(OkHttpClient client) {
    return new WeatherRepositoryImpl(client);
  }
}
