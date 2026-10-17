package com.irv205.apimockinterceptor.di

import com.irv205.apimockinterceptor.data.repository.CharacterRepositoryImpl
import com.irv205.apimockinterceptor.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(
        impl: CharacterRepositoryImpl,
    ): CharacterRepository
}
