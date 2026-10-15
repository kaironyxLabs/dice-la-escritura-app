package com.example.lampara.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.data.content.LectorNiveles
import com.example.lampara.data.datastore.AjustesRepository
import com.example.lampara.data.db.CREAR_PERFIL_INICIAL
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.MIGRACION_1_2
import com.example.lampara.domain.usecase.GestorNiveles
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
    fun provideDatabase(@ApplicationContext context: Context): LamparaDatabase =
        Room.databaseBuilder(context, LamparaDatabase::class.java, LamparaDatabase.NOMBRE)
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
