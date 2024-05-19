package run.piece.dev.data.refactoring.ui.more

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.db.user.UserDao
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.member.MemberModule
import run.piece.dev.data.refactoring.ui.more.api.MoreApi
import run.piece.dev.data.refactoring.ui.more.repository.MoreRepositoryImpl
import run.piece.dev.data.refactoring.ui.more.repository.local.MoreLocalDataSource
import run.piece.dev.data.refactoring.ui.more.repository.local.MoreLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.more.repository.remote.MoreRemoteDataSource
import run.piece.dev.data.refactoring.ui.more.repository.remote.MoreRemoteDataSourceImpl
import run.piece.domain.refactoring.more.repository.MoreRepository
import java.lang.reflect.Member
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class, MemberModule::class])
@InstallIn(SingletonComponent::class)
object MoreModule {
    @Singleton
    @Provides
    fun provideMoreApi(retrofit: Retrofit) : MoreApi = retrofit.create(MoreApi::class.java)

    @Singleton
    @Provides
    fun provideMoreDataSource(moreApi: MoreApi) : MoreRemoteDataSource = MoreRemoteDataSourceImpl(moreApi)

    @Singleton
    @Provides
    fun provideMoreLocalDataSource(userDao: UserDao) : MoreLocalDataSource = MoreLocalDataSourceImpl(userDao)

    @Singleton
    @Provides
    fun provideMoreRepository(moreRemoteDataSource: MoreRemoteDataSource, moreLocalDataSource: MoreLocalDataSource) : MoreRepository = MoreRepositoryImpl(moreRemoteDataSource, moreLocalDataSource)
}