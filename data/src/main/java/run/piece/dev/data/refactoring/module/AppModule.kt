package run.piece.dev.data.refactoring.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import run.piece.dev.data.utils.DisplayManager
import run.piece.dev.data.utils.NetworkConnection
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDisplayManager() : DisplayManager = DisplayManager()

    @Singleton
    @Provides
    fun provideNetworkConnection(@ApplicationContext context: Context): NetworkConnection = NetworkConnection(context)

    @Singleton
    @Provides
    fun provideResourcesProvider(@ApplicationContext context: Context): ResourcesProvider = ResourcesProvider(context)

    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

}