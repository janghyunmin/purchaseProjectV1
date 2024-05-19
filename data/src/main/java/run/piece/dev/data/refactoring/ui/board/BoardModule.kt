package run.piece.dev.data.refactoring.ui.board

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.board.api.BoardApi
import run.piece.dev.data.refactoring.ui.board.repository.BoardRepositoryImpl
import run.piece.dev.data.refactoring.ui.board.repository.local.BoardLocalDataSource
import run.piece.dev.data.refactoring.ui.board.repository.local.BoardLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.board.repository.remote.BoardRemoteDataSource
import run.piece.dev.data.refactoring.ui.board.repository.remote.BoardRemoteDataSourceImpl
import run.piece.domain.refactoring.board.repository.BoardRepository
import run.piece.domain.refactoring.board.usecase.BoardListGetUseCase
import javax.inject.Singleton


@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object BoardModule {
    @Singleton
    @Provides
    fun provideBoardApi(retrofit: Retrofit) : BoardApi = retrofit.create(BoardApi::class.java)

    @Singleton
    @Provides
    fun provideBoardDataSource(api: BoardApi) : BoardRemoteDataSource = BoardRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideBoardLocalDataSource(): BoardLocalDataSource = BoardLocalDataSourceImpl()

    @Singleton
    @Provides
    fun provideBoardRepository(boardRemoteDataSource: BoardRemoteDataSource, boardLocalDataSource: BoardLocalDataSource) : BoardRepository = BoardRepositoryImpl(boardLocalDataSource, boardRemoteDataSource)

    @Singleton
    @Provides
    fun provideBoardUseCase(boardRepository: BoardRepository) : BoardListGetUseCase = BoardListGetUseCase(boardRepository)

}