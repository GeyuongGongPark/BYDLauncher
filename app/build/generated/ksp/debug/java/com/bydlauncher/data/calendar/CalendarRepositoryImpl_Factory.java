package com.bydlauncher.data.calendar;

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
public final class CalendarRepositoryImpl_Factory implements Factory<CalendarRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public CalendarRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CalendarRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static CalendarRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new CalendarRepositoryImpl_Factory(contextProvider);
  }

  public static CalendarRepositoryImpl newInstance(Context context) {
    return new CalendarRepositoryImpl(context);
  }
}
