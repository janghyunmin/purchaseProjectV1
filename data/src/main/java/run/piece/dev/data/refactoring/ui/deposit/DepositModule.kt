package run.piece.dev.data.refactoring.ui.deposit

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.deposit.api.DepositApi
import run.piece.dev.data.refactoring.ui.deposit.repository.DepositRepositoryImpl
import run.piece.dev.data.refactoring.ui.deposit.repository.local.DepositLocalDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.local.DepositLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.DepositRemoteDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.DepositRemoteDataSourceImpl
import run.piece.domain.refactoring.deposit.repository.DepositRepository
import run.piece.domain.refactoring.deposit.usecase.DepositBalanceUseCase
import run.piece.domain.refactoring.deposit.usecase.DepositHistoryUseCase
import run.piece.domain.refactoring.deposit.usecase.DepositPurchaseUseCase
import run.piece.domain.refactoring.deposit.usecase.GetMemberAccountUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object DepositModule {
    @Singleton
    @Provides
    fun provideDepositApi(retrofit: Retrofit) : DepositApi = retrofit.create(DepositApi::class.java)

    @Singleton
    @Provides
    fun provideDepositLocalDataSource(): DepositLocalDataSource = DepositLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideDepositRemoteDataSource(api: DepositApi) : DepositRemoteDataSource = DepositRemoteDataSourceImpl(api = api)

    @Singleton
    @Provides
    fun provideDepositRepository(depositRemoteDataSource: DepositRemoteDataSource, depositLocalDataSource: DepositLocalDataSource): DepositRepository = DepositRepositoryImpl(depositLocalDataSource, depositRemoteDataSource)

    @Singleton
    @Provides
    fun provideGetMemberAccountUseCase(depositRepository: DepositRepository) : GetMemberAccountUseCase = GetMemberAccountUseCase(depositRepository)

    @Singleton
    @Provides
    fun provideDepositBalanceUseCase(depositRepository: DepositRepository) : DepositBalanceUseCase = DepositBalanceUseCase(depositRepository)

    @Singleton
    @Provides
    fun provideDepositHistoryUseCase(depositRepository: DepositRepository) : DepositHistoryUseCase = DepositHistoryUseCase(depositRepository)

    @Singleton
    @Provides
    fun provideDepositPurchaseUseCase(depositRepository: DepositRepository) : DepositPurchaseUseCase = DepositPurchaseUseCase(depositRepository)

}