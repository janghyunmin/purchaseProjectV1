package run.piece.dev.data.refactoring.ui.event

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.event.api.EventApi
import run.piece.dev.data.refactoring.ui.event.repository.EventRepositoryImpl
import run.piece.dev.data.refactoring.ui.event.repository.local.EventLocalDataSource
import run.piece.dev.data.refactoring.ui.event.repository.local.EventLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.event.repository.remote.EventRemoteDataSource
import run.piece.dev.data.refactoring.ui.event.repository.remote.EventRemoteDataSourceImpl
import run.piece.domain.refactoring.event.repository.EventRepository
import run.piece.domain.refactoring.event.usecase.EventDetailGetUseCase
import run.piece.domain.refactoring.event.usecase.EventListGetUseCase
import javax.inject.Singleton
@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object EventModule {
    @Singleton
    @Provides
    fun provideEventApi(retrofit: Retrofit) : EventApi = retrofit.create(EventApi::class.java)

    @Singleton
    @Provides
    fun provideEventRemoteDataSource(api: EventApi) : EventRemoteDataSource = EventRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideEventLocalDataSource() : EventLocalDataSource = EventLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideEventRepository(eventRemoteDataSource: EventRemoteDataSource, eventLocalDataSource: EventLocalDataSource) : EventRepository = EventRepositoryImpl(eventRemoteDataSource, eventLocalDataSource)

    @Singleton
    @Provides
    fun provideEventListGetUseCase(eventRepository: EventRepository): EventListGetUseCase = EventListGetUseCase(eventRepository)

    @Singleton
    @Provides
    fun provideEventDetailGetUseCase(eventRepository: EventRepository): EventDetailGetUseCase = EventDetailGetUseCase(eventRepository)
}