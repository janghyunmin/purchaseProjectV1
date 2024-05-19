package run.piece.dev.data.refactoring.datastore

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import run.piece.dev.data.refactoring.datastore.repository.DataStoreRepositoryImpl
import run.piece.domain.refactoring.datastore.DataStoreRepository
import javax.inject.Singleton


// Migration 대상 Preferences 이름
private const val PREFERENCE_NAME = "PIECE"

// DataStoreModule
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

//    @Singleton
//    @Provides
//    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
//        return PreferenceDataStoreFactory.create(
//            corruptionHandler = ReplaceFileCorruptionHandler(
//                produceNewData = { emptyPreferences() }
//            ),
//            migrations = listOf(SharedPreferencesMigration(appContext,PREFERENCE_NAME)),
//            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
//            produceFile = { appContext.preferencesDataStoreFile(PREFERENCE_NAME) }
//        )
//    }


    @Singleton
    @Provides
    fun provideDataStoreRepository(
        @ApplicationContext app: Context
    ): DataStoreRepository = DataStoreRepositoryImpl(app)

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default)
    }

}