#include "brain/engine.h"

#include <android/log.h>
#include <jni.h>

#define CATCH_CONFIG_MAIN

#include "test/catch.hpp"

#include "test/AndroidBuffer.h"

Engine* instance = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_startBrain( JNIEnv* /* env */,
                                                                      jobject /* this */,
                                                                      jint dimension ) {
    assert( instance == nullptr );
    instance = new Engine( static_cast<const uint32_t>(dimension));
    instance->StartLoop();
    assert( instance != nullptr );
}

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_stopBrain( JNIEnv* /* env */,
                                                                     jobject /*this*/ ) {
    assert( instance != nullptr );
    instance->AddCommandsToInputQueue( "end" );
    delete instance;
    instance = nullptr;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_readFromBrain( JNIEnv* env,
                                                                         jobject /* this */,
                                                                         jint timeoutMillis ) {
    assert( instance != nullptr );
    if( timeoutMillis == 0 && instance->IsEmptyOutputQueue()) {
        return env->NewStringUTF( "" );
    }

    const auto str = instance->ReadFromOutputQueue( timeoutMillis + 1 );
    if( !str.empty()) {
        __android_log_write( ANDROID_LOG_DEBUG, "JNI read", str.c_str());
    }
    return env->NewStringUTF( str.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_writeToBrain( JNIEnv* env,
                                                                        jobject /* this */,
                                                                        jstring command ) {
    const auto str = env->GetStringUTFChars( command, nullptr );
    __android_log_write( ANDROID_LOG_DEBUG, "JNI write", str );

    if( str != nullptr ) {
        assert( instance != nullptr );
        instance->AddCommandsToInputQueue( str );
        env->ReleaseStringUTFChars( command, str );
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_runCatch2Test( JNIEnv* env,
                                                                         jobject /* this */,
                                                                         jstring name ) {
    // Redirect std::cout to logcat
    AndroidBuffer buf;
    std::cout.rdbuf( &buf );

    const auto str = env->GetStringUTFChars( name, nullptr );

    // Prepare test run with fake executable name
    const char* arguments[] = { "runTest.exe" };
    const auto argc = 1;
    const auto argv = const_cast<char**>(arguments);

    static Catch::Session catchSession;

    // Start each session with new fresh configuration
    // without this there is a crash at 2nd session invocation
    Catch::ConfigData cfg;
    cfg.testsOrTags.emplace_back( str );
    catchSession.useConfigData( cfg );

    const int result = catchSession.run( argc, argv );

    env->ReleaseStringUTFChars( name, str );
    // reset buffer to prevent double free at destruction
    std::cout.rdbuf( nullptr );
    return result;
}