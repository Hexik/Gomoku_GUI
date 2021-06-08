/**
 * @file safecast.h
 * @brief simple templates for runtime integer conversion checking
 *        ideal case: replace all reported places from -Wconversion
 **/

#ifndef SAFECAST_H
#define SAFECAST_H

#include <cstdint>
#include <limits>
#include <type_traits>

#include <android/log.h>

/**
* @brief tests basic safe_cast conversions
*/
void safecast_check();

#define LOG_SAFECAST( f )   __android_log_print(ANDROID_LOG_FATAL, "Safecast", " 0x%llX", static_cast<long long>( f ))

/*
* define NO_SAFECAST if you want switch off the checking and logging from safe_cast
* or if your toolchain can not stomach advanced template features :-(
*/

#ifdef NO_SAFECAST

/** dummy template without overflow checks, cast only */
template <typename To, typename From> To safe_cast( From f ) { return static_cast<To>( f ); }

#else /*  NO_SAFECAST */

/* full template with overflow checks and logs */

/** @cond C1 */
/* usual arith. conversions for ints (pre-condition: A, B differ) */

/* helper template to find an underlying type in case of enum */
template<typename T, typename = typename std::is_enum<T>::type>
struct safe_underlying_type {
    using type = T;
};

template<typename T>
struct safe_underlying_type<T, std::true_type> {
    using type = std::underlying_type_t<T>;
};

template<int>
struct uac_at;
template<>
struct uac_at<1> {
    using type = int;
};
template<>
struct uac_at<2> {
    using type = unsigned int;
};
template<>
struct uac_at<3> {
    using type = long;
};
template<>
struct uac_at<4> {
    using type = unsigned long;
};
template<>
struct uac_at<5> {
    using type = long long;
};
template<>
struct uac_at<6> {
    using type = unsigned long long;
};

template<typename A, typename B>
struct uac_type {
    static char ( & f( int ))[1];
    static char ( & f( unsigned int ))[2];
    static char ( & f( long ))[3];
    static char ( & f( unsigned long ))[4];
    static char ( & f( long long ))[5];
    static char ( & f( unsigned long long ))[6];
    using type = typename uac_at<static_cast<int>( sizeof f(
            false ? typename safe_underlying_type<A>::type()
                  : typename safe_underlying_type<B>::type()))>::type;
};
/** @endcond C1 */

/**
 * @class do_conv
 * @brief workhorse template to make conversion check by specialization
 */
template<typename To, typename From, bool to_signed = std::is_signed<To>::value,
        bool from_signed = std::is_signed<From>::value,
        bool rank_fine = ( std::numeric_limits<To>::digits + std::is_signed<To>::value >=
                           std::numeric_limits<From>::digits + std::is_signed<From>::value )>
struct do_conv;

/** @cond C2 */
/** these conversions never overflow, like int -> int,
* or  int -> long. */
template<typename To, typename From, bool Sign>
struct do_conv<To, From, Sign, Sign, true> {
    static To callAction( From f ) { return static_cast<To>( f ); }
};

template<typename To, typename From>
struct do_conv<To, From, false, false, false> {
    static To callAction( From f ) {
        if( f > static_cast<To>( -1 )) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};

template<typename To, typename From>
struct do_conv<To, From, false, true, true> {
    static To callAction( From f ) {
        if( f < 0 ) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};

template<typename To, typename From>
struct do_conv<To, From, false, true, false> {
    using type = typename uac_type<To, From>::type;

    static To callAction( From f ) {
        if( f < 0 || static_cast<type>( f ) > static_cast<To>( -1 )) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};

template<typename To, typename From>
struct do_conv<To, From, true, false, false> {
    using type = typename uac_type<To, From>::type;

    static To callAction( From f ) {
        if( f > static_cast<type>( std::numeric_limits<To>::max())) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};

template<typename To, typename From, bool Rank>
struct do_conv<To, From, true, false, Rank> {
    using type = typename uac_type<To, From>::type;

    static To callAction( From f ) {
        if( static_cast<type>( f ) > static_cast<type>( std::numeric_limits<To>::max())) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};

template<typename To, typename From>
struct do_conv<To, From, true, true, false> {
    static To callAction( From f ) {
        if( f < std::numeric_limits<To>::min() || f > std::numeric_limits<To>::max()) {
            LOG_SAFECAST( f );
        }
        return static_cast<To>( f );
    }
};
/** @endcond C2 */

/**
 * @brief safe conversion between integers
 * @param f value to convert
 * @return converted value
 */
template<typename To, typename From>
To safe_cast( From f ) { return do_conv<To, From>::callAction( f ); }

#endif /*  NO_SAFECAST */
#endif /* SAFECAST_H */
