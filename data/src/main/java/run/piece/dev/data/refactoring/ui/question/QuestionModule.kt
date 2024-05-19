package run.piece.dev.data.refactoring.ui.question

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.question.api.QuestionApi
import run.piece.dev.data.refactoring.ui.question.repository.QuestionRepositoryImpl
import run.piece.dev.data.refactoring.ui.question.repository.local.QuestionLocalDataSource
import run.piece.dev.data.refactoring.ui.question.repository.local.QuestionLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.question.repository.remote.QuestionRemoteDataSource
import run.piece.dev.data.refactoring.ui.question.repository.remote.QuestionRemoteDataSourceImpl
import run.piece.domain.refactoring.question.repository.QuestionRepository
import run.piece.domain.refactoring.question.usecase.QuestionListGetUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object QuestionModule {
    @Singleton
    @Provides
    fun provideQuestionApi(retrofit: Retrofit) : QuestionApi = retrofit.create(QuestionApi::class.java)

    @Singleton
    @Provides
    fun provideQuestionRemoteDataSource(api: QuestionApi) : QuestionRemoteDataSource = QuestionRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideQuestionLocalDataSource() : QuestionLocalDataSource = QuestionLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideQuestionRepository(questionRemoteDataSource: QuestionRemoteDataSource, questionLocalDataSource: QuestionLocalDataSource) : QuestionRepository = QuestionRepositoryImpl(questionRemoteDataSource, questionLocalDataSource)

    @Singleton
    @Provides
    fun provideQuestionListGetUseCase(questionRepository: QuestionRepository): QuestionListGetUseCase = QuestionListGetUseCase(questionRepository)
}