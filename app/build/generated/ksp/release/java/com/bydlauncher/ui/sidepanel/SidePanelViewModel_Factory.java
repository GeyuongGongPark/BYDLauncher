package com.bydlauncher.ui.sidepanel;

import android.content.Context;
import com.bydlauncher.domain.calendar.CalendarRepository;
import com.bydlauncher.domain.weather.WeatherRepository;
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
public final class SidePanelViewModel_Factory implements Factory<SidePanelViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<WeatherRepository> weatherRepositoryProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  public SidePanelViewModel_Factory(Provider<Context> contextProvider,
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
  }

  @Override
  public SidePanelViewModel get() {
    return newInstance(contextProvider.get(), weatherRepositoryProvider.get(), calendarRepositoryProvider.get());
  }

  public static SidePanelViewModel_Factory create(Provider<Context> contextProvider,
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    return new SidePanelViewModel_Factory(contextProvider, weatherRepositoryProvider, calendarRepositoryProvider);
  }

  public static SidePanelViewModel newInstance(Context context, WeatherRepository weatherRepository,
      CalendarRepository calendarRepository) {
    return new SidePanelViewModel(context, weatherRepository, calendarRepository);
  }
}
