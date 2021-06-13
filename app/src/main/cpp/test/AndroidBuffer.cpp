#include "AndroidBuffer.h"
#include <android/log.h>

AndroidBuffer::AndroidBuffer() : buffer() {
    setp( nullptr, nullptr );
}

int AndroidBuffer::overflow( int c ) {
    if( c == traits_type::eof() || c == '\n' || idx == kBufferSize - 1U ) {
        buffer[idx] = '\0';

        __android_log_print( ANDROID_LOG_INFO, "unitTest", "%s", buffer );

        idx = 0U;
    }

    if( c != traits_type::eof() && c != '\n' ) {
        buffer[idx++] = c;
    }

    return c;
}
