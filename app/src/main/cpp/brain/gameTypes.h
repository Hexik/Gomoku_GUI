#ifndef GAME_TYPES_H
#define GAME_TYPES_H

/**
 * @file gameTypes.h
 * @brief Common project constants, types, structs
 */

#include <algorithm>
#include <string>
#include <vector>

#include <android/log.h>

namespace Util {
    inline std::string Trim( const std::string& str ) {
        const auto WhiteSpace = " \t\v\r\n";
        auto       tmp        = str;

        tmp.erase( 0, tmp.find_first_not_of( WhiteSpace ));
        const auto idx = tmp.find_last_not_of( WhiteSpace );

        if( idx != std::string::npos ) {
            tmp.erase( idx + 1 );
        }
        return tmp;
    }

    inline std::vector<int64_t> ParseNumbers( const std::string& s, const std::string& delimiters ) {
        auto res     = std::vector<int64_t>{};
        auto numbers = std::vector<std::string>{}; // we'll put all of the tokens in here
        const auto& numbers_str = s;

        std::string token;

        __android_log_write( ANDROID_LOG_VERBOSE, "parse num token", s.c_str());

        size_t beg = 0;
        size_t pos = 0;
        while(( beg = numbers_str.find_first_not_of( delimiters, pos )) != std::string::npos ) {
            pos = numbers_str.find_first_of( delimiters, beg + 1 );
            numbers.push_back( numbers_str.substr( beg, pos - beg ));
        }

        for( const auto& t : numbers ) {
            auto       end   = static_cast<char*>( nullptr );
            const auto value = strtoll( t.c_str(), &end, 10 );
            if( end == t.c_str()) {
                __android_log_write( ANDROID_LOG_INFO, "Error in parsing token ", t.c_str());
                break;
            }
            res.push_back( static_cast<int64_t>( value ));
            __android_log_write( ANDROID_LOG_VERBOSE, "parse token ", std::to_string( res.back()).c_str());
        }

        return res;
    }

    // random number generator, srand() replacement
    inline uint32_t rand_xor128() {
        static uint32_t x = 123456789;
        static uint32_t y = 362436069;
        static uint32_t z = 521288629;
        static uint32_t w = 88675123;
        const uint32_t  t = x ^static_cast<uint32_t>( static_cast<uint64_t>( x ) << 11 );
        x        = y;
        y        = z;
        z        = w;
        return w = w ^ ( w >> 19 ) ^ ( t ^ ( t >> 8 ));
    }

/**
 * @brief Convert std::string to uppercase
 * @param strToConvert source string
 * @return UpperCase string
 */
    [[nodiscard]] inline std::string StringToUpper( std::string strToConvert ) {
        std::transform( strToConvert.begin(), strToConvert.end(), strToConvert.begin(), ::toupper );

        return strToConvert;
    }
}

using coord_t = unsigned int; /**< coord type */
using coords_t = unsigned int; /**< coords type */

constexpr uint32_t kWallSize = 4U; /**< uses 4/5 wall pieces on every side as sentinel */

constexpr uint32_t kBoardSize = 32U;                        /**< first power of 2 above kPlaySize */
constexpr uint32_t kMaxBoard  =
                           kBoardSize -
                           2 * kWallSize; /**< max board size is 24 for FreeStyle, 22 otherwise */
constexpr uint32_t kPlaySize  = 20U;                        /**< default board size is 20 */

static_assert( kWallSize == 4 || kWallSize == 5, "kWallSize" );
static_assert( kBoardSize >= kMaxBoard + 2 * kWallSize, "No room for wall pieces" );
static_assert( kMaxBoard >= kPlaySize, "Too big play size" );
static_assert( sizeof( coord_t ) >= 1, "At least 8 bits" );
static_assert( sizeof( coords_t ) >= 2, "At least 8+8 bits" );
static_assert( sizeof( coords_t ) >= sizeof( coord_t ), "coords_t is bigger than coord_t" );

namespace mf {

/**
 * @brief Absolute value quickest implementation
 * @param x input data
 */
    [[nodiscard]] inline constexpr int abs( int x ) {
        int s = x >> std::numeric_limits<decltype( x )>::digits;
        return ( x ^ s ) - s;
    }

    static_assert( abs( 0 ) == 0 );
    static_assert( abs( -1 ) == 1 );
    static_assert( abs( 1 ) == abs( -1 ));
    static_assert( abs( -std::numeric_limits<int>::max()) == abs( std::numeric_limits<int>::max()));

/**
 * @brief Integer signum quickest implementation
 * @param x input data
 */
    [[nodiscard]] inline constexpr int signum( int x ) {
        return static_cast<int>( x > 0 ) - static_cast<int>( x < 0 );
    }

    static_assert( signum( std::numeric_limits<int>::min()) == -1 );
    static_assert( signum( -5 ) == -1 );
    static_assert( signum( -1 ) == -1 );
    static_assert( signum( 0 ) == 0 );
    static_assert( signum( 1 ) == 1 );
    static_assert( signum( 5 ) == 1 );
    static_assert( signum( std::numeric_limits<int>::max()) == 1 );

} // namespace mf


/**
 * @enum eMove_t
 * @brief Enumerated Move types
 */
enum eMove_t {
    eEmpty,
    eXX,
    eOO,
    eBlock
};

/**  Move type, masks, constants */
struct Move {
    constexpr Move() : Move( 0, 0, eMove_t::eEmpty ) {}

    constexpr Move( coord_t x_, coord_t y_, eMove_t type_ ) : x( x_ ), y( y_ ), type( type_ ) {}

    coord_t x;
    coord_t y;
    eMove_t  type;
};

constexpr Move MOVE_NONE = Move();

/**@{
 * Create new move from coords and type
 */
template<eMove_t player>
[[nodiscard]] inline constexpr Move createMove( const coord_t x, const coord_t y ) {
    static_assert( player == eMove_t::eXX || player == eMove_t::eOO, "Bad player" );
    return Move( x, y, player );
}

/**@} */

/**
*@brief Check if Move can be played on board
*/
[[nodiscard]] inline constexpr bool IsOk( const Move m ) { return m.type != eMove_t::eEmpty; }

static_assert( !IsOk( MOVE_NONE ));

/**
*@brief Check if Move is eMove_t::eEmpty
*/
[[nodiscard]] inline constexpr bool IsEmpty( const Move m ) {
    return m.type == eMove_t::eEmpty;
}

static_assert( IsEmpty( MOVE_NONE ));

/**
*@brief Check if Move is player type
*/
template<eMove_t player>
[[nodiscard]] inline constexpr bool IsType( Move m ) { return m.type == player; }

/**
*@brief Check if Move is eMove_t::eXX
*/
[[nodiscard]] inline constexpr bool IsTypeXX( Move m ) { return IsType<eMove_t::eXX>( m ); }

/**
*@brief Check if Move is eMove_t::eOO
*/
[[nodiscard]] inline constexpr bool IsTypeOO( Move m ) { return IsType<eMove_t::eOO>( m ); }

/**
*@brief Get x-coordinate from Move
*/
[[nodiscard]] inline constexpr coord_t GetX( Move m ) {
    return static_cast<coord_t>(m.x);
}

/**
*@brief Get y-coordinate from Move
*/
[[nodiscard]] inline constexpr coord_t GetY( Move m ) {
    return static_cast<coord_t>(m.y);
}

/**
*@brief Get x,x-coordinates in form x * kBoardSize + y from Move
*/
[[nodiscard]] inline constexpr coords_t GetCoords( Move m ) { return m.x * kBoardSize + m.y; }

/**
*@brief Get move type
*/
[[nodiscard]] inline constexpr eMove_t GetType( Move m ) {
    return m.type;
}

/**
*@brief Get plain move = type + coords from Move
*/
[[nodiscard]] inline constexpr Move GetPlainMove( Move m ) {
    return m;
}


/**@{*/
/**
*@brief Set x, y-coordinates
*/
[[nodiscard]] inline constexpr Move SetCoords( coord_t x, coord_t y ) {
    return Move( x, y, eMove_t::eEmpty );
}
/**@}*/

/**
*@brief Set move type, the rest is unchanged
*/
[[nodiscard]] inline constexpr Move SetType( Move m, eMove_t type ) {
    return Move( m.x, m.y, type );
}

/**
*@brief Equal coordinates
*@param m Move to compare
*@param other Move to compare
*/
[[nodiscard]] inline constexpr bool HaveSameCoords( Move m, Move other ) {
    return m.x == other.x && m.y == other.y;
}

/**
*@brief Equality of moves
*@param m Move to compare
*@param other Move to compare
*/
[[nodiscard]] inline constexpr bool operator==( Move m, Move other ) {
    return HaveSameCoords( m, other ) && m.type == other.type;
}

/** How much should be bit index shifted, log2(digit count) */
constexpr auto kIndexShift = std::numeric_limits<unsigned int>::digits == 32U ? 5U
                                                                              :
                             std::numeric_limits<unsigned int>::digits == 64U ? 6U
                                                                              : 0U;
static_assert( kIndexShift == 5U || kIndexShift == 6U, "Wrong bit count" );

#endif // GAME_TYPES_H
