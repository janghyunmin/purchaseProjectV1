package run.piece.dev.data.refactoring.ui.purchase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import run.piece.dev.data.refactoring.module.AppModule
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.ui.purchase.api.PurchaseApi
import run.piece.dev.data.refactoring.ui.purchase.repository.PurchaseRepositoryImpl
import run.piece.dev.data.refactoring.ui.purchase.repository.local.PurchaseLocalDataSource
import run.piece.dev.data.refactoring.ui.purchase.repository.local.PurchaseLocalDataSourceImpl
import run.piece.dev.data.refactoring.ui.purchase.repository.remote.PurchaseRemoteDataSource
import run.piece.dev.data.refactoring.ui.purchase.repository.remote.PurchaseRemoteDataSourceImpl
import run.piece.domain.refactoring.purchase.repository.PurchaseRepository
import run.piece.domain.refactoring.purchase.usecase.PurchaseCancelUseCase
import run.piece.domain.refactoring.purchase.usecase.PurchaseInfoUseCase
import run.piece.domain.refactoring.purchase.usecase.PurchaseOfferUseCase
import javax.inject.Singleton

@Module(includes = [NetModule::class, AppModule::class])
@InstallIn(SingletonComponent::class)
object PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseApi(retrofit: Retrofit): PurchaseApi = retrofit.create(PurchaseApi::class.java)

    @Singleton
    @Provides
    fun providePurchaseLocalDataSource() : PurchaseLocalDataSource = PurchaseLocalDataSourceImpl()

    @Singleton
    @Provides
    fun providePurchaseRemoteDataSource(api: PurchaseApi) : PurchaseRemoteDataSource = PurchaseRemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun providePurchaseRepository(
        purchaseLocalDataSource: PurchaseLocalDataSource,
        purchaseRemoteDataSource: PurchaseRemoteDataSource
    ) : PurchaseRepository = PurchaseRepositoryImpl(purchaseLocalDataSource,purchaseRemoteDataSource)

    @Singleton
    @Provides
    fun providePurchaseOfferUseCase(purchaseRepository: PurchaseRepository) : PurchaseOfferUseCase =
        PurchaseOfferUseCase(purchaseRepository)

    @Singleton
    @Provides
    fun providePurchaseCancelUseCase(purchaseRepository: PurchaseRepository) : PurchaseCancelUseCase =
        PurchaseCancelUseCase(purchaseRepository)

    @Singleton
    @Provides
    fun providePurchaseInfoUseCase(purchaseRepository: PurchaseRepository) : PurchaseInfoUseCase =
        PurchaseInfoUseCase(purchaseRepository)
}