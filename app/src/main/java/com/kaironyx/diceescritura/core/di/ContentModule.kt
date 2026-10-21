package com.kaironyx.diceescritura.core.di

import com.kaironyx.diceescritura.data.bible.BibleTextSource
import com.kaironyx.diceescritura.data.bible.BibleTextSourceAssets
import com.kaironyx.diceescritura.data.content.FuenteContenido
import com.kaironyx.diceescritura.data.content.FuenteContenidoAssets
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
