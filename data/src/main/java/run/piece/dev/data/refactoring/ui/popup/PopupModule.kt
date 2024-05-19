package run.piece.dev.data.refactoring.ui.popup

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.popup.api.PopupApi
import run.piece.dev.data.refactoring.ui.popup.repository.PopupRepositoryImpl
import run.piece.dev.data.refactoring.ui.popup.repository.local.PopupLocalDataSource
import run.piece.dev.data.refactoring.ui.popup.repository.local.PopupLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.popup.repository.remote.PopupRemoteDataSource
import run.piece.dev.data.refactoring.ui.popup.repository.remote.PopupRemoteDataSourceImpl
import run.piece.domain.refactoring.popup.repository.PopupRepository
import run.piece.domain.refactoring.popup.usecase.PopupUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object PopupModule {
    @Singleton
    @Provides
    fun providePopupApi(retrofit: Retrofit) : PopupApi = retrofit.create(PopupApi::class.java)

    @Singleton
    @Provides
    fun providePopupLocalDataSource(): PopupLocalDataSource = PopupLocalDataSourceImpl()

    @Singleton
    @Provides
    fun providePopupRemoteDataSource(api : PopupApi) : PopupRemoteDataSource = PopupRemoteDataSourceImpl(api = api)

    @Singleton
    @Provides
    fun providePopupRepository(
        popupLocalDataSource: PopupLocalDataSource,
        popupRemoteDataSource: PopupRemoteDataSource
    ) : PopupRepository = PopupRepositoryImpl(popupLocalDataSource = popupLocalDataSource,popupRemoteDataSource= popupRemoteDataSource)

    @Singleton
    @Provides
    fun providePopupUseCase(popupRepository: PopupRepository) : PopupUseCase = PopupUseCase(repository = popupRepository)

}