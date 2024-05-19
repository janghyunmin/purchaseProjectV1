package run.piece.dev.data.refactoring.ui.notice

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.notice.api.NoticeApi
import run.piece.dev.data.refactoring.ui.notice.repository.NoticeRepositoryImpl
import run.piece.dev.data.refactoring.ui.notice.repository.local.NoticeLocalDataSource
import run.piece.dev.data.refactoring.ui.notice.repository.local.NoticeLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.notice.repository.remote.NoticeRemoteDataSource
import run.piece.dev.data.refactoring.ui.notice.repository.remote.NoticeRemoteDataSourceImpl
import run.piece.domain.refactoring.faq.usecase.FaqListGetUseCase
import run.piece.domain.refactoring.notice.repository.NoticeRepository
import run.piece.domain.refactoring.notice.usecase.NoticeDetailGetUseCase
import run.piece.domain.refactoring.notice.usecase.NoticeListGetUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object NoticeModule {
    @Singleton
    @Provides
    fun provideNoticeApi(retrofit: Retrofit) : NoticeApi = retrofit.create(NoticeApi::class.java)

    @Singleton
    @Provides
    fun provideNoticeDataSource(api: NoticeApi) : NoticeRemoteDataSource = NoticeRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideNoticeLocalDataSource() : NoticeLocalDataSource = NoticeLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideNoticeRepository(noticeRemoteDataSource: NoticeRemoteDataSource, noticeLocalDataSource: NoticeLocalDataSource) : NoticeRepository = NoticeRepositoryImpl(noticeRemoteDataSource, noticeLocalDataSource)

    @Singleton
    @Provides
    fun provideNoticeUseCase(noticeRepository: NoticeRepository): NoticeDetailGetUseCase = NoticeDetailGetUseCase(noticeRepository)

    @Singleton
    @Provides
    fun provideNoticeListGetUseCase(noticeRepository: NoticeRepository): NoticeListGetUseCase = NoticeListGetUseCase(noticeRepository)
}