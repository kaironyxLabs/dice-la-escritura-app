package com.kaironyx.diceescritura.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.kaironyx.diceescritura.core.flags.FeatureFlags
import com.kaironyx.diceescritura.data.content.LectorNiveles
import com.kaironyx.diceescritura.data.datastore.AjustesRepository
import com.kaironyx.diceescritura.data.db.CREAR_PERFIL_INICIAL
import com.kaironyx.diceescritura.data.db.DiceEscrituraDatabase
import com.kaironyx.diceescritura.data.db.MIGRACION_1_2
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DiceEscrituraDatabase =
        Room.databaseBuilder(context, DiceEscrituraDatabase::class.java, DiceEscrituraDatabase.NOMBRE)
            .addMigrations(MIGRACION_1_2)
            .addCallback(CREAR_PERFIL_INICIAL)
            .build()

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("ajustes") }

    @Provides
    @Singleton
    fun provideGestorNiveles(@ApplicationContext context: Context): GestorNiveles {
        val texto = context.assets.open("content/${LectorNiveles.ARCHIVO}").bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        return GestorNiveles(LectorNiveles.leer(texto))
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FlagsModule {
    @Binds
    abstract fun bindFeatureFlags(impl: AjustesRepository): FeatureFlags
}
