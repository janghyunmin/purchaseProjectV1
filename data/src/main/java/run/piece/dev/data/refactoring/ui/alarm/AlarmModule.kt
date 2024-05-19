package run.piece.dev.data.refactoring.ui.alarm

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.alarm.api.AlarmApi
import run.piece.dev.data.refactoring.ui.alarm.repository.AlarmRepositoryImpl
import run.piece.dev.data.refactoring.ui.alarm.repository.local.AlarmLocalDataSource
import run.piece.dev.data.refactoring.ui.alarm.repository.local.AlarmLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.alarm.repository.remote.AlarmRemoteDataSource
import run.piece.dev.data.refactoring.ui.alarm.repository.remote.AlarmRemoteDataSourceImpl
import run.piece.domain.refactoring.alarm.repository.AlarmRepository
import run.piece.domain.refactoring.alarm.usecase.AlarmListGetUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioDeleteUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioGetUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioSendUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPutUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object AlarmModule {
    @Singleton
    @Provides
    fun provideAlarmApi(retrofit: Retrofit) : AlarmApi = retrofit.create(AlarmApi::class.java)

    @Singleton
    @Provides
    fun provideAlarmDataSource(api: AlarmApi) : AlarmRemoteDataSource = AlarmRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideAlarmLocalDataSource() : AlarmLocalDataSource = AlarmLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideAlarmRepository(alarmRemoteDataSource: AlarmRemoteDataSource, alarmLocalDataSource: AlarmLocalDataSource) : AlarmRepository = AlarmRepositoryImpl(alarmRemoteDataSource, alarmLocalDataSource)

    @Singleton
    @Provides
    fun provideAlarmUseCase (alarmRepository: AlarmRepository): AlarmPutUseCase = AlarmPutUseCase(alarmRepository)

    @Singleton
    @Provides
    fun provideAlarmListGetUseCase (alarmRepository: AlarmRepository): AlarmListGetUseCase = AlarmListGetUseCase(alarmRepository)

    @Singleton
    @Provides
    fun provideAlarmPortfolioSendUseCase (alarmRepository: AlarmRepository): AlarmPortfolioSendUseCase = AlarmPortfolioSendUseCase(alarmRepository)

    @Singleton
    @Provides
    fun provideAlarmPortfolioDeleteUseCase (alarmRepository: AlarmRepository): AlarmPortfolioDeleteUseCase = AlarmPortfolioDeleteUseCase(alarmRepository)

    @Singleton
    @Provides
    fun provideAlarmPortfolioGetUseCase (alarmRepository: AlarmRepository): AlarmPortfolioGetUseCase = AlarmPortfolioGetUseCase(alarmRepository)


}