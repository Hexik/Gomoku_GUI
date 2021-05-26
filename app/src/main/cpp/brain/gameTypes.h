#ifndef GAME_TYPES_H
#define GAME_TYPES_H

/**
 * @file gameTypes.h
 * @brief Common project constants, types, structs
 */

#include <algorithm>
#include <cassert>
#include <cstdint>
#include <limits>
#include <string>
#include <vector>

#include <android/log.h>

namespace Misc {
    inline std::string Trim( const std::string& str ) {
        const auto WhiteSpace = " \t\v\r\n";
        auto       tmp        = str;

        tmp.erase( 0, tmp.find_first_not_of( WhiteSpace ));
        const auto idx = tmp.find_last_not_of( WhiteSpace );

        if ( idx != std::string::npos ) {
            tmp.erase( idx + 1 );
        }
        return tmp;
    }

    inline std::vector<int64_t> ParseNumbers( const std::string& s, const std::string& delims ) {
        auto      res           = std::vector<int64_t>{};
        auto      numbers       = std::vector<std::string>{}; // we'll put all of the tokens in here
        const auto& numbers_str = s;

        std::string token;

        __android_log_write( ANDROID_LOG_VERBOSE, "parse num token", s.c_str());

        size_t beg = 0;
        size_t pos = 0;
        while (( beg = numbers_str.find_first_not_of( delims, pos )) != std::string::npos ) {
            pos = numbers_str.find_first_of( delims, beg + 1 );
            numbers.push_back( numbers_str.substr( beg, pos - beg ));
        }

        for ( const auto& t : numbers ) {
            auto       end   = static_cast<char*>( nullptr );
            const auto value = strtoll( t.c_str(), &end, 10 );
            if ( end == t.c_str()) {
                __android_log_write( ANDROID_LOG_INFO, "Error in parsing token ", t.c_str());
                break;
            }
            res.push_back( static_cast<int64_t>( value ));
            __android_log_write( ANDROID_LOG_VERBOSE, "parse token ",
                                 std::to_string( res.back()).c_str());
        }

        return res;
    }

} // namespace Misc


namespace System {
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

/** 4 pieces .. 2 bit per piece, uint64_t can hold 32 pieces
 * | wall
 * x me
 * o you
 * _ empty
 * if you need more, switch to uint128
 */

using pieces_t = uint64_t;     /**< pieces on ray type */
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
static_assert( sizeof( pieces_t ) == 8, "At least 64 bits" );
static_assert( sizeof( pieces_t ) * 4 == kBoardSize, "4 pieces per byte" );
static_assert( sizeof( coord_t ) >= 1, "At least 8 bits" );
static_assert( sizeof( coords_t ) >= 2, "At least 8+8 bits" );
static_assert( sizeof( coords_t ) >= sizeof( coord_t ), "coords_t is bigger than coord_t" );

/**
*@enum eGuiMode
*@brief Gui or manager mode
*/
enum class eGuiMode {
    eGomocup, eYixin
};

/**
 * @enum Bound
 * @brief Enumerated score boundary types
 */
enum Bound {
    BOUND_NONE, BOUND_UPPER, BOUND_LOWER, BOUND_EXACT = BOUND_UPPER | BOUND_LOWER
};

constexpr int          MAX_PLY   = 128;                   /**< maximum search depth */
constexpr unsigned int MAX_MOVES = kPlaySize * kPlaySize; /**< maximum generated moves */

/**  Value type and limits */
enum Value : int {
    VALUE_ZERO      = 0,
    VALUE_DRAW      = 0,
    VALUE_UNIT      = 100,
    VALUE_KNOWN_WIN = 10000,
    VALUE_WIN       = 32000,
    VALUE_INFINITE  = 32001,
    VALUE_NONE      = 32002,

    VALUE_WIN_IN_MAX_PLY  = VALUE_WIN - 2 * MAX_PLY,
    VALUE_LOSS_IN_MAX_PLY = -VALUE_WIN + 2 * MAX_PLY,
    VALUE_WIN_THRESHOLD   = VALUE_WIN_IN_MAX_PLY
};
static_assert( sizeof( Value ) == 4, "Size of Value is not 4" );

/**  ply depth type */
enum Depth : int {
    ONE_PLY = 1, DEPTH_ZERO = 0 * ONE_PLY, DEPTH_NONE = -6 * ONE_PLY, DEPTH_MAX = MAX_PLY * ONE_PLY
};
static_assert(( ONE_PLY & ( ONE_PLY - 1 )) == 0, "ONE_PLY is not a power of 2" );

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

/** @brief Macro for common arithmetic operations */
#define ENABLE_FULL_OPERATORS_ON( T )                                                                 \
    [[nodiscard]] inline T constexpr   operator+( T d1, T d2 ) { return T( int( d1 ) + int( d2 ) ); } \
    [[nodiscard]] inline T constexpr   operator-( T d1, T d2 ) { return T( int( d1 ) - int( d2 ) ); } \
    [[nodiscard]] inline T constexpr   operator*( int i, T d ) { return T( i * int( d ) ); }          \
    [[nodiscard]] inline T constexpr   operator*( T d, int i ) { return T( int( d ) * i ); }          \
    [[nodiscard]] inline T constexpr   operator-( T d ) { return T( -int( d ) ); }                    \
    [[nodiscard]] inline T constexpr   operator/( T d, int i ) { return T( int( d ) / i ); }          \
    [[nodiscard]] inline int constexpr operator/( T d1, T d2 ) { return int( d1 ) / int( d2 ); }      \
    inline T&                          operator+=( T& d1, T d2 ) { return d1 = d1 + d2; }             \
    inline T&                          operator-=( T& d1, T d2 ) { return d1 = d1 - d2; }             \
    inline T&                          operator*=( T& d, int i ) { return d = T( int( d ) * i ); }    \
    inline T&                          operator/=( T& d, int i ) { return d = T( int( d ) / i ); }    \
    inline T&                          operator++( T& d ) { return d = T( int( d ) + 1 ); }           \
    inline T&                          operator--( T& d ) { return d = T( int( d ) - 1 ); }

ENABLE_FULL_OPERATORS_ON( Value )

ENABLE_FULL_OPERATORS_ON( Depth )

#undef ENABLE_FULL_OPERATORS_ON

/**@{ Additional operators to add integers to a Value */
[[nodiscard]] inline constexpr Value operator+( Value v, int i ) { return Value( int( v ) + i ); }

[[nodiscard]] inline constexpr Value operator-( Value v, int i ) { return Value( int( v ) - i ); }

inline constexpr Value& operator+=( Value& v, int i ) { return v = v + i; }

inline constexpr Value& operator-=( Value& v, int i ) { return v = v - i; }
/**@}*/

static_assert( VALUE_KNOWN_WIN == VALUE_KNOWN_WIN + VALUE_ZERO );

/**
 * Move - bits mapping
 *  0 ..  4  y coord
 *  5 ..  9  x coord
 * 10 .. 11  eMove_t player type
 * 12 .. 13  eDir_t threat direction
 * 14 .. 24  cost squares bitmap, shifted by kCostOffset
 * 25 .. 26  distance to win
 * 27 .. 30  Square::eThreat
 * 31 .. 31  forced, used for extensions
 */

constexpr int32_t kCostOffset = 5; /**< bits offset of type data*/

constexpr uint32_t kBitsOffsetType   = 10U; /**< bits offset of type data*/
constexpr uint32_t kBitsOffsetThreat = 12U; /**< bits offset of threat data*/
constexpr uint32_t kBitsOffsetCost   = 14U; /**< bits offset of cost bitmap data*/
constexpr uint32_t kBitsOffsetDist   = 25U; /**< bits offset of distance to win data*/
constexpr uint32_t kBitsOffsetComb   = 27U; /**< bits offset of eThreat data*/
constexpr uint32_t kBitsOffsetForced = 31U; /**< bits offset of forced flag data*/

constexpr uint32_t kBitsSizeThreat =
                           kBitsOffsetComb - kBitsOffsetThreat; /**< bits size of all threat data*/
constexpr uint32_t kBitsSizeCoord  =
                           kBitsOffsetType / 2;                 /**< bits size of one coord data*/
constexpr uint32_t kBitsSizeType   =
                           kBitsOffsetThreat - kBitsOffsetType; /**< bits size of type data*/
constexpr uint32_t kBitsSizeDir    =
                           kBitsOffsetCost - kBitsOffsetThreat; /**< bits size of threat dir data*/
constexpr uint32_t kBitsSizeCost   =
                           kBitsOffsetDist - kBitsOffsetCost;   /**< bits size of cost bitmap data*/
constexpr uint32_t kBitsSizeDist   =
                           kBitsOffsetComb - kBitsOffsetDist;   /**< bits size of distance data*/
constexpr uint32_t kBitsSizeComb   =
                           kBitsOffsetForced - kBitsOffsetComb; /**< bits size of eThreatComb data*/

/**
 * @class generateMask
 * @brief Recurrent mask generation
 */
template<unsigned int N>
[[nodiscard]] constexpr pieces_t generateMask() {
    if constexpr( N > 0 ) {
        static_assert( N <= std::numeric_limits<unsigned int>::digits );
        return static_cast<pieces_t>(( generateMask<N - 1>() << 1 ) |
                                     1ULL ); /**< internal template calculated result*/

    } else {
        return 0ULL;
    }
}

static_assert( generateMask<0>() == 0b0U );
static_assert( generateMask<1>() == 0b01U );
static_assert( generateMask<2>() == 0b11U );
static_assert( generateMask<9>() == 0b111111111U );
static_assert( generateMask<32>() == 0xFFFFFFFFU );

/**  Move type, masks, consts */
enum Move : unsigned int {
    kMaskCoords     = generateMask<kBitsOffsetType>(),                      /**< coords mask*/
    kMaskType       = generateMask<kBitsSizeType>() << kBitsOffsetType,     /**< type mask*/
    kMaskThreat     = generateMask<kBitsSizeThreat>() << kBitsOffsetThreat, /**< threat mask*/
    kMaskCombThreat =
    generateMask<kBitsSizeComb>() << kBitsOffsetComb,     /**< combined threat mask*/
    kMaskForced     = generateMask<1>() << kBitsOffsetForced,               /**< forced move mask*/

    MOVE_NONE = 0U,
    MOVE_NULL = kMaskCoords
};

static_assert( sizeof( Move ) == 4, "Size of Move is not 4" );

/**
 * @enum eDir_t
 * @brief Enumerated direction types
 */
enum eDir_t : unsigned int {
    eRow = ( 0U << kBitsOffsetThreat ),
    eCol = ( 1U << kBitsOffsetThreat ),
    eUp  = ( 2U << kBitsOffsetThreat ),
    eDn  = ( 3U << kBitsOffsetThreat )
};

static_assert( sizeof( eDir_t ) == 4, "Size of eDir_t is not 4" );

constexpr uint32_t kDirections = 4U; /**< Pattern, Rays directions */

/**
 * @brief Converts enum type variable to underlying integral type
 * @tparam E enumeration type
 * @param e value to convert
 * @return converted value
 */
template<typename E>
[[nodiscard]] constexpr auto to_underlying( E e ) noexcept {
    return static_cast<std::underlying_type_t<E>>( e );
}

/**
 * @enum eMove_t
 * @brief Enumerated Move types
 */
enum eMove_t : unsigned int {
    eEmpty = ( 0U << kBitsOffsetType ),
    eXX    = ( 1U << kBitsOffsetType ),
    eOO    = ( 2U << kBitsOffsetType ),
    eBlock = ( 3U << kBitsOffsetType )
};

static_assert( sizeof( eMove_t ) == 4, "Size of eMove_t is not 4" );

static_assert( static_cast<unsigned int>( kMaskType ) == eMove_t::eBlock,
               "kMaskType differs from eMove_t::eBlock" );
static_assert(( kMaskCoords | kMaskType | kMaskThreat | kMaskCombThreat | kMaskForced ) ==
              generateMask<32>(),
              "kMask does not cover first 32bits" );
static_assert(( kMaskCoords ^ kMaskType ^ kMaskThreat ^ kMaskCombThreat ^ kMaskForced ) ==
              generateMask<32>(),
              "kMask components overlaps in first 32bits" );

/**@{
 * Create new move from coords and type
 */
template<eMove_t player>
[[nodiscard]] inline constexpr Move createMove( const coord_t x, const coord_t y ) {
    static_assert( player == eMove_t::eXX || player == eMove_t::eOO, "Bad player" );
    return Move( player | ( x << kBitsSizeCoord ) | y );
}

template<eMove_t player>
[[nodiscard]] inline constexpr Move createMove( const coords_t co ) {
    static_assert( player == eMove_t::eXX || player == eMove_t::eOO, "Bad player" );
    return Move( player | co );
}
/**@} */

/**
*@brief Check if Move can be played on board
*/
[[nodiscard]] inline constexpr bool IsOk( const Move m ) { return ( m & ( ~kMaskCoords )) != 0; }

static_assert( !IsOk( MOVE_NONE ));

/**
*@brief Check if Move is eMove_t::eEmpty
*/
[[nodiscard]] inline constexpr bool IsEmpty( const Move m ) {
    return ( m & kMaskType ) == eMove_t::eEmpty;
}

static_assert( IsEmpty( MOVE_NONE ));

/**
*@brief Check if Move is player type
*/
template<eMove_t player>
[[nodiscard]] inline constexpr bool IsType( Move m ) { return ( m & kMaskType ) == player; }

/**
*@brief Check if Move is eMove_t::eXX
*/
[[nodiscard]] inline constexpr bool IsTypeXX( Move m ) { return IsType<eMove_t::eXX>( m ); }

/**
*@brief Check if Move is eMove_t::eOO
*/
[[nodiscard]] inline constexpr bool IsTypeOO( Move m ) { return IsType<eMove_t::eOO>( m ); }

/**
*@brief Check if Move is eMove_t::eBlock
*/
[[nodiscard]] inline constexpr bool IsTypeBlock( Move m ) { return IsType<eMove_t::eBlock>( m ); }

/**
*@brief Check if Move has forced flag
*/
[[nodiscard]] inline constexpr bool IsForced( Move m ) { return ( m & kMaskForced ) != 0; }

/**
*@brief Get x-coordinate from Move
*/
[[nodiscard]] inline constexpr coord_t GetX( Move m ) {
    return static_cast<coord_t>(( m >> kBitsSizeCoord ) & generateMask<kBitsSizeCoord>());
}

/**
*@brief Get y-coordinate from Move
*/
[[nodiscard]] inline constexpr coord_t GetY( Move m ) {
    return static_cast<coord_t>( m & generateMask<kBitsSizeCoord>());
}

/**
*@brief Get x,x-coordinates in form x * kBoardSize + y from Move
*/
[[nodiscard]] inline constexpr coords_t GetCoords( Move m ) { return m & kMaskCoords; }

/**
*@brief Get move type
*/
[[nodiscard]] inline constexpr eMove_t GetType( Move m ) {
    return static_cast<eMove_t>( m & kMaskType );
}

/**
*@brief Get threat win direction from Move
*/
[[nodiscard]] inline constexpr eDir_t GetThreatDir( Move m ) {
    return static_cast<eDir_t>( m & ( generateMask<kBitsSizeDir>() << kBitsOffsetThreat ));
}

/**
*@brief Get threat cost squares from Move
*/
[[nodiscard]] inline constexpr uint32_t GetThreatCostBitmap( Move m ) {
    return static_cast<uint32_t>(( m >> kBitsOffsetCost ) & generateMask<kBitsSizeCost>());
}

/**
*@brief Get threat win distance from Move
*/
[[nodiscard]] inline constexpr coord_t GetWinDistance( Move m ) {
    return static_cast<coord_t>(( m >> kBitsOffsetDist ) & generateMask<kBitsSizeDist>());
}

/**
*@brief Move has winDistance LE
*@tparam winDistance dtw
*/
template<size_t winDistance>
[[nodiscard]] inline constexpr bool IsWinDistance_LE( const Move m ) {
    return ( m & ( generateMask<kBitsSizeDist>() << kBitsOffsetDist )) <=
           ( winDistance << kBitsOffsetDist );
}

/**
*@brief Move has winDistance EQ
*@tparam winDistance dtw
*/
template<size_t winDistance>
[[nodiscard]] inline constexpr bool IsWinDistance_EQ( const Move m ) {
    return ( m & ( generateMask<kBitsSizeDist>() << kBitsOffsetDist )) ==
           ( winDistance << kBitsOffsetDist );
}

/**
*@brief Get index = eXX x eOO + coords from Move
*/
[[nodiscard]] inline constexpr Move GetHashIndex( Move m ) {
    return Move( m & ( to_underlying( eMove_t::eXX ) | kMaskCoords ));
}

/**
*@brief Get plain move = type + coords from Move
*/
[[nodiscard]] inline constexpr Move GetPlainMove( Move m ) {
    return Move( m & ( kMaskType | kMaskCoords ));
}

/**
*@brief Get threat coordinates = Coord + type + Direction from Move
*/
[[nodiscard]] inline constexpr Move GetThreatCoord( Move m ) {
    return Move( m & generateMask<kBitsOffsetCost>());
}

/**
*@brief Compare Moves threat coordinates = Coord + type + Direction
*/
[[nodiscard]] inline constexpr bool AreThreatCoordsSame( Move m, Move other ) {
    return (( m ^ other ) & generateMask<kBitsOffsetCost>()) == 0;
}

/**
*@brief Get combined threat Id from Move
*/
[[nodiscard]] inline constexpr unsigned int GetCombThreat( Move m ) {
    return static_cast<unsigned int>(( m >> kBitsOffsetComb ) & generateMask<kBitsSizeComb>());
}

/**
*@brief Test specific combined threat Id in Move
*/
template<unsigned int comb>
[[nodiscard]] inline constexpr bool IsCombThreat( Move m ) {
    return ( m & ( generateMask<kBitsSizeComb>() << kBitsOffsetComb )) ==
           ( comb << kBitsOffsetComb );
}

/**
 * @brief checks if specific combined threat Id in Move and win distance is dist
 * @tparam comb threat to find
 * @tparam dist distance to win
 * @param m Move
 * @return true if condition is met, false otherwise
 */
template<unsigned int comb, unsigned int dist>
[[nodiscard]] inline constexpr bool IsCombThreatWithDist( Move m ) {
    return ( m & ( kMaskCombThreat | generateMask<kBitsSizeDist>() << kBitsOffsetDist )) ==
           (( comb << kBitsOffsetComb ) | ( dist << kBitsOffsetDist ));
}

/**@{*/
/**
*@brief Set x, y-coordinates
*/
[[nodiscard]] inline constexpr Move SetCoords( Move m, coord_t x, coord_t y ) {
    return Move(( m & ( ~kMaskCoords )) | ( x << kBitsSizeCoord ) | y );
}

[[nodiscard]] inline constexpr Move SetCoords( Move m, coords_t c ) {
    return Move(( m & ( ~kMaskCoords )) | c );
}

[[nodiscard]] inline constexpr Move SetCoords( coord_t x, coord_t y ) {
    return Move(( x << kBitsSizeCoord ) | y );
}
/**@}*/

/**
*@brief Set move type, the rest is unchanged
*/
[[nodiscard]] inline constexpr Move SetType( Move m, eMove_t type ) {
    return Move(( m & ( ~kMaskType )) | type );
}

/**
*@brief Set threat into Move, the rest is unchanged
*/
[[nodiscard]] inline constexpr Move SetCombThreat( Move m, unsigned int comb ) {
    return Move(( m & ~kMaskCombThreat ) | ( comb << kBitsOffsetComb ));
}

/**
*@brief Equal coordinates
*@param m Move to compare
*@param other Move to compare
*/
[[nodiscard]] inline constexpr bool HaveSameCoords( Move m, Move other ) {
    return (( m ^ other ) & kMaskCoords ) == 0;
}

/**
*@brief Equality of moves
*@param m Move to compare
*@param other Move to compare
*/
[[nodiscard]] inline constexpr bool operator==( Move m, Move other ) {
    return (( m ^ other ) & ( kMaskType | kMaskCoords )) == 0;
}

/**
 * @brief Dump Move info
 * @param m Move to dump
 */
void Dump( Move m );

/**
 * @brief Check presence
 * @tparam Container data type
 * @param container to be searched for element
 * @param element look for it
 * @return boolean result
 */
template<class Container>
[[nodiscard]] bool
InContainer( const Container& container, const typename Container::value_type& element ) {
    return std::find( container.begin(), container.end(), element ) != container.end();
}

/**
 * @brief Diagnostic container dump
 * @param v container to dump, T must implement Dump() method
 */
template<typename Container>
void DumpContainer( [[maybe_unused]] Container const& v ) {
    DumpContainer( std::begin( v ), std::end( v ));
}

/**
 * @brief Diagnostic container dump
 * @param first container to dump, *Iterator must implement Dump() method
 * @param last container to dump, *Iterator must implement Dump() method
 */
template<typename Iterator>
void DumpContainer( [[maybe_unused]] Iterator first, [[maybe_unused]] Iterator last ) {
    std::for_each( first, last, []( const auto& m ) { Dump( m ); } );
}

/** How much should be bit index shifted, log2(digit count) */
constexpr auto kIndexShift = std::numeric_limits<unsigned int>::digits == 32U ? 5U
                                                                              :
                             std::numeric_limits<unsigned int>::digits == 64U ? 6U
                                                                              : 0U;
static_assert( kIndexShift == 5U || kIndexShift == 6U, "Wrong bit count" );

#endif // GAME_TYPES_H
