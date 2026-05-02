package org.sparcs.soap.Widgets

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCaseBackgroundProtocol
import org.sparcs.soap.Widgets.AraPortalWidget.AraPortalWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyTimetableWidget.TimetableWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UpComingWidgetSyncManager
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UpcomingWidget

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimetableWidget

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DDayWidget

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AraPortal

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun tokenStorage(): TokenStorageProtocol
    @UpcomingWidget
    fun upComingSyncManager(): UpComingWidgetSyncManager

    @TimetableWidget
    fun timetableSyncManager(): TimetableWidgetSyncManager

    @DDayWidget
    fun dDaySyncManager(): DDayWidgetSyncManager

    @AraPortal
    fun araPortalSyncManager(): AraPortalWidgetSyncManager

    fun timetableUseCase(): TimetableUseCaseBackgroundProtocol
    fun araBoardUseCase(): AraBoardUseCaseProtocol
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

    @DDayWidget
    @Provides
    fun provideDDaySyncManager(@ApplicationContext context: Context): DDayWidgetSyncManager {
        return DDayWidgetSyncManager(context)
    }

    @AraPortal
    @Provides
    fun provideAraPortalSyncManager(@ApplicationContext context: Context): AraPortalWidgetSyncManager {
        return AraPortalWidgetSyncManager(context)
    }
}
