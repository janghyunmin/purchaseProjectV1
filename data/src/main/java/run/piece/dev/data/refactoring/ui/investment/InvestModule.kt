package run.piece.dev.data.refactoring.ui.investment

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.investment.api.InvestMentApi
import run.piece.dev.data.refactoring.ui.investment.repository.InvestMentRepositoryImpl
import run.piece.dev.data.refactoring.ui.investment.repository.local.InvestMentLocalDataSource
import run.piece.dev.data.refactoring.ui.investment.repository.local.InvestMentLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.investment.repository.remote.InvestMentRemoteDataSource
import run.piece.dev.data.refactoring.ui.investment.repository.remote.InvestMentRemoteDataSourceImpl
import run.piece.domain.refactoring.investment.repository.InvestMentRepository
import run.piece.domain.refactoring.investment.usecase.GetInvestMentUseCase
import run.piece.domain.refactoring.investment.usecase.PostInvestMentUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object InvestModule {
    @Singleton
    @Provides
    fun provideInvestMentApi(retrofit: Retrofit) : InvestMentApi = retrofit.create(InvestMentApi::class.java)

    @Singleton
    @Provides
    fun provideInvestMentLocalDataSource(): InvestMentLocalDataSource = InvestMentLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideInvestMentRemoteDataSource(api: InvestMentApi) : InvestMentRemoteDataSource = InvestMentRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideInvestMentRepository(investMentLocalDataSource: InvestMentLocalDataSource, investMentRemoteDataSource: InvestMentRemoteDataSource) : InvestMentRepository = InvestMentRepositoryImpl(investMentLocalDataSource,investMentRemoteDataSource)

    @Singleton
    @Provides
    fun provideInvestMentPostUseCase(investMentRepository: InvestMentRepository): PostInvestMentUseCase = PostInvestMentUseCase(investMentRepository)

    @Singleton
    @Provides
    fun provideGetInvestMentUseCase(investMentRepository: InvestMentRepository): GetInvestMentUseCase = GetInvestMentUseCase(investMentRepository)
}