#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" JNIEXPORT jstring JNICALL
Java_cz_fontan_gomoku_1gui_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */, jstring s) {
    const char *str = env->GetStringUTFChars(s, 0);
    __android_log_write(ANDROID_LOG_DEBUG, "JNI", str);

    std::string hello = "Hello from Brain";
    return env->NewStringUTF(hello.c_str());
}
