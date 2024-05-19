package run.piece.dev.data.refactoring.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BiometricModule {

    @Provides
    fun provideBiometricAuthenticationManager(context: Context): BiometricAuthenticationManager {
        return BiometricAuthenticationManager(context)
    }
}