package com.bydlauncher.ui.navi;

import android.content.Context;
import com.bydlauncher.domain.navi.NaviRepository;
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
public final class NaviViewModel_Factory implements Factory<NaviViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<NaviRepository> naviRepositoryProvider;

  public NaviViewModel_Factory(Provider<Context> contextProvider,
      Provider<NaviRepository> naviRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.naviRepositoryProvider = naviRepositoryProvider;
  }

  @Override
  public NaviViewModel get() {
    return newInstance(contextProvider.get(), naviRepositoryProvider.get());
  }

  public static NaviViewModel_Factory create(Provider<Context> contextProvider,
      Provider<NaviRepository> naviRepositoryProvider) {
    return new NaviViewModel_Factory(contextProvider, naviRepositoryProvider);
  }

  public static NaviViewModel newInstance(Context context, NaviRepository naviRepository) {
    return new NaviViewModel(context, naviRepository);
  }
}
