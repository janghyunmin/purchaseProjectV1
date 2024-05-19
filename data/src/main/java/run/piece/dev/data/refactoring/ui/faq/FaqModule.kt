package run.piece.dev.data.refactoring.ui.faq

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.faq.api.FaqApi
import run.piece.dev.data.refactoring.ui.faq.repository.FaqRepositoryImpl
import run.piece.dev.data.refactoring.ui.faq.repository.local.FaqLocalDataSource
import run.piece.dev.data.refactoring.ui.faq.repository.local.FaqLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.faq.repository.remote.FaqRemoteDataSource
import run.piece.dev.data.refactoring.ui.faq.repository.remote.FaqRemoteDataSourceImpl
import run.piece.dev.data.refactoring.ui.notice.api.NoticeApi
import run.piece.domain.refactoring.faq.repository.FaqRepository
import run.piece.domain.refactoring.faq.usecase.FaqListGetUseCase
import javax.inject.Singleton
@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object FaqModule {
    @Singleton
    @Provides
    fun provideNoticeApi(retrofit: Retrofit) : FaqApi = retrofit.create(FaqApi::class.java)

    @Singleton
    @Provides
    fun provideFaqRemoteDataSource(api: FaqApi) : FaqRemoteDataSource = FaqRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideFaqLocalDataSource() : FaqLocalDataSource = FaqLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideFaqRepository(faqRemoteDataSource: FaqRemoteDataSource, faqLocalDataSource: FaqLocalDataSource) : FaqRepository = FaqRepositoryImpl(faqRemoteDataSource, faqLocalDataSource)

    @Singleton
    @Provides
    fun provideFaqListGetUseCase(faqRepository: FaqRepository): FaqListGetUseCase = FaqListGetUseCase(faqRepository)
}