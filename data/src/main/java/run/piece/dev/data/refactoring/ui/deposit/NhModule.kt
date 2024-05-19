package run.piece.dev.data.refactoring.ui.deposit

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.module.RoomModule
import run.piece.dev.data.refactoring.ui.deposit.api.NhApi
import run.piece.dev.data.refactoring.ui.deposit.repository.NhBankRepositoryImpl
import run.piece.dev.data.refactoring.ui.deposit.repository.local.NhBankLocalDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.local.NhBankLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.NhBankRemoteDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.NhBankRemoteDataSourceImpl
import run.piece.domain.refactoring.deposit.repository.NhBankRepository
import run.piece.domain.refactoring.deposit.usecase.NhBankHistoryUseCase
import run.piece.domain.refactoring.deposit.usecase.NhBankWithDrawUseCase
import run.piece.domain.refactoring.deposit.usecase.NhChangeAccountUseCase
import run.piece.domain.refactoring.deposit.usecase.NhCreateAccountUseCase
import javax.inject.Singleton


@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object NhModule {
    @Singleton
    @Provides
    fun provideNhApi(retrofit: Retrofit): NhApi = retrofit.create(NhApi::class.java)

    @Singleton
    @Provides
    fun provideNhBankRemoteDataSource(api: NhApi): NhBankRemoteDataSource =
        NhBankRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideNhBankLocalDataSource(): NhBankLocalDataSource =
        NhBankLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideNhBankRepository(
        nhBankRemoteDataSource: NhBankRemoteDataSource,
        nhBankLocalDataSource: NhBankLocalDataSource
    ):
            NhBankRepository =
        NhBankRepositoryImpl(nhBankRemoteDataSource, nhBankLocalDataSource)

    // 연동계좌 및 가상계좌 생성
    @Singleton
    @Provides
    fun provideNhCreateAccountUseCase(nhBankRepository: NhBankRepository): NhCreateAccountUseCase =
        NhCreateAccountUseCase(nhBankRepository)

    // 연동계좌 변경
    @Singleton
    @Provides
    fun provideNhChangeAccountUseCase(nhBankRepository: NhBankRepository) : NhChangeAccountUseCase =
        NhChangeAccountUseCase(nhBankRepository)


    // NH 입금 확인 내역 조회
    @Singleton
    @Provides
    fun provideNhHistoryUseCase(nhBankHistoryRepository: NhBankRepository): NhBankHistoryUseCase =
        NhBankHistoryUseCase(nhBankHistoryRepository)


    // NH 출금 신청
    @Singleton
    @Provides
    fun provideNhBankWithDrawUseCase(nhBankWithDrawRepository: NhBankRepository) : NhBankWithDrawUseCase =
        NhBankWithDrawUseCase(nhBankWithDrawRepository)

}