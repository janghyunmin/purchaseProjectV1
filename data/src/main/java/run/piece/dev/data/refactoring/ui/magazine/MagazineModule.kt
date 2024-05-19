package run.piece.dev.data.refactoring.ui.magazine

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.module.RoomModule
import run.piece.dev.data.refactoring.ui.magazine.api.MagazineApi
import run.piece.dev.data.refactoring.ui.magazine.repository.MagazineRepositoryImpl
import run.piece.dev.data.refactoring.ui.magazine.repository.local.MagazineLocalDataSource
import run.piece.dev.data.refactoring.ui.magazine.repository.local.MagazineLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.magazine.repository.remote.MagazineRemoteDataSource
import run.piece.dev.data.refactoring.ui.magazine.repository.remote.MagazineRemoteDataSourceImpl
import run.piece.domain.refactoring.magazine.repository.MagazineRepository
import run.piece.domain.refactoring.magazine.usecase.MagazineGetUseCase
import run.piece.domain.refactoring.magazine.usecase.MagazineTypeGetUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object MagazineModule {

    @Singleton
    @Provides
    fun provideMagazineApi(retrofit: Retrofit) : MagazineApi = retrofit.create(MagazineApi::class.java)

    @Singleton
    @Provides
    fun provideMagazineLocalDataSource() : MagazineLocalDataSource = MagazineLocalDataSourceImpl()


    @Singleton
    @Provides
    fun provideMagazineDataSource(api: MagazineApi) : MagazineRemoteDataSource = MagazineRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideMagazineRepository(magazineRemoteDataSource: MagazineRemoteDataSource, magazineLocalDataSource: MagazineLocalDataSource) : MagazineRepository
     = MagazineRepositoryImpl(magazineRemoteDataSource,magazineLocalDataSource)

    @Singleton
    @Provides
    fun provideMagazineGetUseCase(magazineRepository: MagazineRepository): MagazineGetUseCase = MagazineGetUseCase(magazineRepository)

    @Singleton
    @Provides
    fun provideMagazineTypeGetUseCase(magazineRepository: MagazineRepository): MagazineTypeGetUseCase = MagazineTypeGetUseCase(magazineRepository)
}