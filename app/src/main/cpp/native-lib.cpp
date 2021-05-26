#include "brain/engine.h"

#include <android/log.h>
#include <jni.h>

Engine* instance = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_startBrain( JNIEnv* env, jobject thiz,
                                                                      jint dimension ) {
    assert( instance == nullptr );
    instance = new Engine( dimension );
    assert( instance != nullptr );
}

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_stopBrain( JNIEnv* env, jobject thiz ) {
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
    auto str = instance->ReadFromOutputQueue( timeoutMillis );
    __android_log_write( ANDROID_LOG_DEBUG, "JNI read", str.c_str());
    return env->NewStringUTF( str.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_writeToBrain( JNIEnv* env,
                                                                        jobject /* this */,
                                                                        jstring s ) {
    const auto str = env->GetStringUTFChars( s, nullptr );
    __android_log_write( ANDROID_LOG_DEBUG, "JNI write", str );

    if ( str != nullptr ) {
        assert( instance != nullptr );
        instance->AddCommandsToInputQueue( str );
        env->ReleaseStringUTFChars( s, str );
    }
}
