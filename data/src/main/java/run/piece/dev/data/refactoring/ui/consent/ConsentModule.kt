package run.piece.dev.data.refactoring.ui.consent

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.consent.api.ConsentApi
import run.piece.dev.data.refactoring.ui.consent.repository.ConsentRepositoryImpl
import run.piece.dev.data.refactoring.ui.consent.repository.local.ConsentLocalDataSource
import run.piece.dev.data.refactoring.ui.consent.repository.local.ConsentLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.consent.repository.remote.ConsentRemoteDataSource
import run.piece.dev.data.refactoring.ui.consent.repository.remote.ConsentRemoteDataSourceImpl
import run.piece.domain.refactoring.consent.repository.ConsentRepository
import run.piece.domain.refactoring.consent.usecase.ConsentListGetUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentMemberListGetUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentSendUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentWebLinkUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object ConsentModule {
    @Singleton
    @Provides
    fun provideConsentApi(retrofit: Retrofit) : ConsentApi = retrofit.create(ConsentApi::class.java)

    @Singleton
    @Provides
    fun provideConsentRemoteDataSource(api: ConsentApi): ConsentRemoteDataSource = ConsentRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideConsentLocalDataSource() : ConsentLocalDataSource = ConsentLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideConsentRepository(remoteDataSource: ConsentRemoteDataSource, localDataSource: ConsentLocalDataSource) : ConsentRepository = ConsentRepositoryImpl(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideConsentGetUseCase(repository: ConsentRepository) : ConsentListGetUseCase = ConsentListGetUseCase(repository)

    @Singleton
    @Provides
    fun provideConsentWebLinkUseCase(repository: ConsentRepository) : ConsentWebLinkUseCase = ConsentWebLinkUseCase(repository)

    @Singleton
    @Provides
    fun provideConsentMemberListGetUseCase(repository: ConsentRepository) : ConsentMemberListGetUseCase = ConsentMemberListGetUseCase(repository)

    @Singleton
    @Provides
    fun provideConsentSendUseCase(repository: ConsentRepository) : ConsentSendUseCase = ConsentSendUseCase(repository)
}