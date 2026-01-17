package org.sparcs.Widgets

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sparcs.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.App.Domain.Usecases.TimetableUseCaseBackgroundProtocol
import org.sparcs.Widgets.BuddyTimetableWidget.TimetableWidgetSyncManager
import org.sparcs.Widgets.BuddyUpcomingClassWidget.UpComingWidgetSyncManager
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UpcomingWidget

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimetableWidget

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun tokenStorage(): TokenStorageProtocol
    @UpcomingWidget
    fun upComingSyncManager(): UpComingWidgetSyncManager

    @TimetableWidget
    fun timetableSyncManager(): TimetableWidgetSyncManager
    fun timetableUseCase(): TimetableUseCaseBackgroundProtocol
}

@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {

    @UpcomingWidget
    @Provides
    fun provideUpcomingSyncManager(@ApplicationContext context: Context): UpComingWidgetSyncManager {
        return UpComingWidgetSyncManager(context)
    }

    @TimetableWidget
    @Provides
    fun provideTimetableSyncManager(@ApplicationContext context: Context): TimetableWidgetSyncManager {
        return TimetableWidgetSyncManager(context)
    }
}