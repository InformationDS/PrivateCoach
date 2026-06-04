package com.privatecoach.app.di

import com.privatecoach.app.core.audio.AudioRecorder
import com.privatecoach.app.core.audio.AudioRecorderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    @Binds @Singleton
    abstract fun bindAudioRecorder(impl: AudioRecorderImpl): AudioRecorder
}
