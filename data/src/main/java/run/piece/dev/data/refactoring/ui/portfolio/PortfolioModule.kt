package run.piece.dev.data.refactoring.ui.portfolio

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.module.RoomModule
import run.piece.dev.data.refactoring.ui.portfolio.api.PortfolioApi
import run.piece.dev.data.refactoring.ui.portfolio.repository.PortfolioRepositoryImpl
import run.piece.dev.data.refactoring.ui.portfolio.repository.local.PortfolioLocalDataSource
import run.piece.dev.data.refactoring.ui.portfolio.repository.local.PortfolioLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.portfolio.repository.remote.PortfolioRemoteDataSource
import run.piece.dev.data.refactoring.ui.portfolio.repository.remote.PortfolioRemoteDataSourceImpl
import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository
import run.piece.domain.refactoring.portfolio.usecase.PortfolioDetailGetUseCase
import run.piece.domain.refactoring.portfolio.usecase.PortfolioListGetUseCase
import run.piece.domain.refactoring.portfolio.usecase.PortfolioProductGetUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object PortfolioModule {
    @Singleton
    @Provides
    fun providePortfolioApi(retrofit: Retrofit) : PortfolioApi = retrofit.create(PortfolioApi::class.java)

    @Singleton
    @Provides
    fun providePortfolioDataSource(api: PortfolioApi) : PortfolioRemoteDataSource = PortfolioRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun providePortfolioLocalDataSource() : PortfolioLocalDataSource = PortfolioLocalDataSourceImpl()

    @Singleton
    @Provides
    fun providePortfolioRepository(portfolioRemoteDataSource: PortfolioRemoteDataSource, portfolioLocalDataSource: PortfolioLocalDataSource) : PortfolioRepository = PortfolioRepositoryImpl(portfolioRemoteDataSource, portfolioLocalDataSource)

    @Singleton
    @Provides
    fun providePortfolioListGetUseCase(portfolioRepository: PortfolioRepository): PortfolioListGetUseCase = PortfolioListGetUseCase(portfolioRepository)

    @Singleton
    @Provides
    fun providePortfolioDetailGetUseCase(portfolioRepository: PortfolioRepository): PortfolioDetailGetUseCase = PortfolioDetailGetUseCase(portfolioRepository)

    @Singleton
    @Provides
    fun providePortfolioProductGetUseCase(portfolioRepository: PortfolioRepository): PortfolioProductGetUseCase = PortfolioProductGetUseCase(portfolioRepository)
}