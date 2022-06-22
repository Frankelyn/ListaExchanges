package com.ejemplo.listaexchanges.di

import com.ejemplo.listaexchanges.ListExchangesRepository
import com.ejemplo.listaexchanges.ListaExchangesApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideCoinApi(moshi: Moshi): ListaExchangesApi {
        return Retrofit.Builder()
            .baseUrl("https://api.coinpaprika.com")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ListaExchangesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCoinRepository(listaExchangesApi: ListaExchangesApi): ListExchangesRepository {
        return ListExchangesRepository(listaExchangesApi)
    }
}