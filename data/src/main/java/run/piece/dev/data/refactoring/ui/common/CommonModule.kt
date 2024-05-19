package run.piece.dev.data.refactoring.ui.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.common.api.CommonApi
import run.piece.dev.data.refactoring.ui.common.repository.CommonRepositoryImpl
import run.piece.dev.data.refactoring.ui.common.repository.local.CommonLocalDataSource
import run.piece.dev.data.refactoring.ui.common.repository.local.CommonLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.common.repository.remote.CommonRemoteDataSource
import run.piece.dev.data.refactoring.ui.common.repository.remote.CommonRemoteDataSourceImpl
import run.piece.domain.refactoring.common.repository.CommonRepository
import run.piece.domain.refactoring.common.usecase.CommonFaqTabGetUseCase
import run.piece.domain.refactoring.common.usecase.SearchAddressUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Singleton
    @Provides
    fun provideCommonApi(retrofit: Retrofit) : CommonApi = retrofit.create(CommonApi::class.java)

    @Singleton
    @Provides
    fun provideCommonLocalDataSource(): CommonLocalDataSource = CommonLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideCommonRemoteDataSource(api: CommonApi) : CommonRemoteDataSource = CommonRemoteDataSourceImpl(api = api)

    @Singleton
    @Provides
    fun provideCommonRepository(commonLocalDataSource: CommonLocalDataSource, commonRemoteDataSource: CommonRemoteDataSource) : CommonRepository = CommonRepositoryImpl(commonLocalDataSource, commonRemoteDataSource)

    @Singleton
    @Provides
    fun provideSearchAddressUseCase(commonRepository: CommonRepository) : SearchAddressUseCase = SearchAddressUseCase(repository = commonRepository)

    @Singleton
    @Provides
    fun provideCommonFaqTabGetUseCase(commonRepository: CommonRepository) : CommonFaqTabGetUseCase = CommonFaqTabGetUseCase(repository = commonRepository)
}