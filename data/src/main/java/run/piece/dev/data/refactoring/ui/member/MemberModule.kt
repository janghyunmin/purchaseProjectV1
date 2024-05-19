package run.piece.dev.data.refactoring.ui.member

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.member.api.MemberApi
import run.piece.dev.data.refactoring.ui.member.repository.MemberRepositoryImpl
import run.piece.dev.data.refactoring.ui.member.repository.local.MemberLocalDataSource
import run.piece.dev.data.refactoring.ui.member.repository.local.MemberLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.member.repository.remote.MemberRemoteDataSource
import run.piece.dev.data.refactoring.ui.member.repository.remote.MemberRemoteDataSourceImpl
import run.piece.domain.refactoring.member.repository.MemberRepository
import run.piece.domain.refactoring.member.usecase.GetAccessTokenUseCase
import run.piece.domain.refactoring.member.usecase.GetAuthPinUseCase
import run.piece.domain.refactoring.member.usecase.GetSsnYnUseCase
import run.piece.domain.refactoring.member.usecase.JoinPostUseCase
import run.piece.domain.refactoring.member.usecase.MemberDeleteUseCase
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.MemberPostInvestUseCase
import run.piece.domain.refactoring.member.usecase.MemberPutNotificationUseCase
import run.piece.domain.refactoring.member.usecase.PostReSmsAuthUseCase
import run.piece.domain.refactoring.member.usecase.PostSmsAuthUseCase
import run.piece.domain.refactoring.member.usecase.PostSmsVerificationUseCase
import run.piece.domain.refactoring.member.usecase.PutAuthPinUseCase
import run.piece.domain.refactoring.member.usecase.PutMemberUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object MemberModule {
    @Singleton
    @Provides
    fun provideMemberApi(retrofit: Retrofit) : MemberApi = retrofit.create(MemberApi::class.java)

    @Singleton
    @Provides
    fun provideMemberRemoteDataSource(api: MemberApi) : MemberRemoteDataSource = MemberRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideMemberLocalDataSource() : MemberLocalDataSource = MemberLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideMemberRepository(remoteDataSource: MemberRemoteDataSource, localDataSource: MemberLocalDataSource) : MemberRepository = MemberRepositoryImpl(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideMemberInfoGetUseCase(repository: MemberRepository): MemberInfoGetUseCase = MemberInfoGetUseCase(repository)

    @Singleton
    @Provides
    fun provideJoinPostUseCase(repository: MemberRepository): JoinPostUseCase = JoinPostUseCase(repository)

    @Singleton
    @Provides
    fun provideGetAccessToken(repository: MemberRepository): GetAccessTokenUseCase = GetAccessTokenUseCase(repository)


    @Singleton
    @Provides
    fun provideGetAuthPinUseCase(repository: MemberRepository): GetAuthPinUseCase = GetAuthPinUseCase(repository)

    @Singleton
    @Provides
    fun providePutAuthPinUseCase(repository: MemberRepository): PutAuthPinUseCase = PutAuthPinUseCase(repository)

    @Singleton
    @Provides
    fun provideGetSsnYnUseCase(repository: MemberRepository): GetSsnYnUseCase = GetSsnYnUseCase(repository)

    @Singleton
    @Provides
    fun provideMemberDeviceCheckUseCase(repository: MemberRepository) : MemberDeviceCheckUseCase = MemberDeviceCheckUseCase(repository)

    @Singleton
    @Provides
    fun provideMemberPutNotificationUseCase(repository: MemberRepository): MemberPutNotificationUseCase = MemberPutNotificationUseCase(repository)

    @Singleton
    @Provides
    fun provideInvestAgreementUseCase(repository: MemberRepository) : MemberPostInvestUseCase = MemberPostInvestUseCase(repository)

    @Singleton
    @Provides
    fun providePostSmsAuthUseCase(repository: MemberRepository): PostSmsAuthUseCase = PostSmsAuthUseCase(repository)

    @Singleton
    @Provides
    fun providePostReSmsAuthUseCase(repository: MemberRepository): PostReSmsAuthUseCase = PostReSmsAuthUseCase(repository)

    @Singleton
    @Provides
    fun providePostSmsVerificationUseCase(repository: MemberRepository): PostSmsVerificationUseCase = PostSmsVerificationUseCase(repository)

    @Singleton
    @Provides
    fun providePutMemberUseCase(repository: MemberRepository): PutMemberUseCase = PutMemberUseCase(repository)

    @Singleton
    @Provides
    fun provideMemberDeleteUseCase(repository: MemberRepository): MemberDeleteUseCase = MemberDeleteUseCase(repository)


}