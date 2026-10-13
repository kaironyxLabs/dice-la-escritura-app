package com.example.lampara.core.di

import com.example.lampara.data.bible.BibleTextSource
import com.example.lampara.data.bible.BibleTextSourceAssets
import com.example.lampara.data.content.FuenteContenido
import com.example.lampara.data.content.FuenteContenidoAssets
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {
    @Binds
    abstract fun bindFuenteContenido(impl: FuenteContenidoAssets): FuenteContenido

    @Binds
    abstract fun bindBibleTextSource(impl: BibleTextSourceAssets): BibleTextSource
}
