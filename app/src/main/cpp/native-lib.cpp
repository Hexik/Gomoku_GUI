#include <android/log.h>
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_helloStringFromJNI(JNIEnv *env,
                                                                             jobject /* this */,
                                                                             jstring s) {
    const auto str = env->GetStringUTFChars(s, nullptr);
    __android_log_write(ANDROID_LOG_DEBUG, "JNI write", str);

    const auto hello = std::string("Hello from Brain");
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_readStringFromJNI(JNIEnv *env,
                                                                            jobject /* this */) {
    __android_log_write(ANDROID_LOG_DEBUG, "JNI read", "str");

    const auto hello = std::string("Hello from Brain");
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_cz_fontan_gomoku_1gui_NativeInterface_00024Companion_writeStringToJNI(JNIEnv *env,
                                                                           jobject /* this */,
                                                                           jstring s) {
    const auto str = env->GetStringUTFChars(s, nullptr);
    __android_log_write(ANDROID_LOG_DEBUG, "JNI write", str);
}