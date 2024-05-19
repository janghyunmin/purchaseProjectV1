package run.piece.dev.data.refactoring.ui.deed

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.module.RoomModule
import run.piece.dev.data.refactoring.ui.deed.api.DeedApi
import run.piece.dev.data.refactoring.ui.deed.repository.DeedRepositoryImpl
import run.piece.dev.data.refactoring.ui.deed.repository.local.DeedLocalDataSource
import run.piece.dev.data.refactoring.ui.deed.repository.local.DeedLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.deed.repository.remote.DeedRemoteDataSource
import run.piece.dev.data.refactoring.ui.deed.repository.remote.DeedRemoteDataSourceImpl
import run.piece.domain.refactoring.deed.repository.DeedRepository
import run.piece.domain.refactoring.deed.usecase.SendEmailUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object DeedModule {
    @Singleton
    @Provides
    fun provideDeedApi(retrofit: Retrofit) : DeedApi = retrofit.create(DeedApi::class.java)

    @Singleton
    @Provides
    fun provideDeedDataSource(api: DeedApi) : DeedRemoteDataSource = DeedRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideDeedLocalDataSource() : DeedLocalDataSource = DeedLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideDeedRepository(deedRemoteDataSource: DeedRemoteDataSource, deedLocalDataSource: DeedLocalDataSource) : DeedRepository = DeedRepositoryImpl(deedRemoteDataSource, deedLocalDataSource)
    @Singleton
    @Provides
    fun provideSendEmailUseCase(deedRepository: DeedRepository) : SendEmailUseCase = SendEmailUseCase(deedRepository)
}