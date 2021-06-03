#include "safecast.h"

#if 1

void safecast_check() {
    /* unsigned -> signed, overflow */
    safe_cast<int8_t>(UINT8_MAX);
    /* unsigned -> signed, no overflow */
    safe_cast<int16_t>(UINT8_MAX);
    safe_cast<int32_t>(UINT8_MAX);
    safe_cast<int64_t>(UINT8_MAX);
    safe_cast<size_t>(UINT8_MAX);

    /* unsigned -> signed, overflow */
    safe_cast<int8_t>(UINT32_MAX);
    safe_cast<int16_t>(UINT32_MAX);
    safe_cast<int32_t>(UINT32_MAX);
    /* unsigned -> signed, no overflow */
    safe_cast<int64_t>(UINT32_MAX);

    /* unsigned -> unsigned, no overflow */
    safe_cast<uint8_t>(UINT8_MAX);
    safe_cast<uint16_t>(UINT8_MAX);
    safe_cast<uint32_t>(UINT8_MAX);
    safe_cast<uint64_t>(UINT8_MAX);
    safe_cast<size_t>(UINT8_MAX);

    /* unsigned -> unsigned, overflow */
    safe_cast<uint8_t>(UINT64_MAX);
    safe_cast<uint16_t>(UINT64_MAX);
    safe_cast<uint32_t>(UINT64_MAX);
    /* unsigned -> unsigned, no overflow on 64bits */
    safe_cast<size_t>(UINT64_MAX);
    /* unsigned -> unsigned, no overflow */
    safe_cast<uint64_t>(UINT64_MAX);

    /* signed -> unsigned, overflow */
    safe_cast<uint8_t>(( -1 ));
    safe_cast<uint16_t>(( -1 ));
    safe_cast<uint32_t>(( -1 ));
    safe_cast<uint64_t>(( -1 ));
    safe_cast<size_t>(( -1 ));

    /* signed -> signed, overflow */
    safe_cast<int8_t>(INT32_MIN);
    safe_cast<int16_t>(INT32_MIN);
    /* signed -> signed, no overflow */
    safe_cast<int32_t>(INT32_MIN);
    safe_cast<int64_t>(INT32_MIN);

    /* always works (no check done) */
    safe_cast<int64_t>(INT32_MIN);
    safe_cast<int32_t>(INT16_MIN);
    safe_cast<int16_t>(INT8_MIN);
    safe_cast<int8_t>(INT8_MIN);
    safe_cast<size_t>(INT8_MAX);

    [[maybe_unused]] int32_t i32 = INT16_MAX;
    /* signed -> signed, no overflow */
    [[maybe_unused]] auto    i16 = safe_cast<int16_t>( i32 );
    __android_log_print( ANDROID_LOG_INFO, "Safecast", "i16 %d", i16 );
    i32++;
    /* signed -> signed, overflow */
    i16 = safe_cast<int16_t>( i32 );
    __android_log_print( ANDROID_LOG_INFO, "Safecast", "i16 %d", i16 );

    int64_t i64 = INT32_MAX;
    /* signed -> signed, overflow */
    i16 = safe_cast<int16_t>( i64 );
    __android_log_print( ANDROID_LOG_INFO, "Safecast", "i16 %d", i16 );
    i64++;
    /* signed -> signed, overflow */
    i32 = safe_cast<int32_t>( i64 );
    __android_log_print( ANDROID_LOG_INFO, "Safecast", "i32 %d", i32 );
}

#endif
