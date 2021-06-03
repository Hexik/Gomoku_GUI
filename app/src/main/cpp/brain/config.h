#ifndef CONFIG_H
#define CONFIG_H

/**
 * @file config.h
 * @brief Configuration info
 */

#include "gameTypes.h"

/**
 * @class Config
 * @brief Configuration info, set of values
 */
class Config
{
public:
    Config();

    /**@{*/
    /** Setters */
    Config& SetContinuous( int32_t continuous )
    {
        m_continuous = continuous;
        return *this;
    }
    Config& SetRule( int32_t rule )
    {
        m_rule = rule;
        return *this;
    }
    Config& SetGameType( int32_t game_type )
    {
        m_game_type = game_type;
        return *this;
    }
    Config& SetHeight( coord_t height )
    {
        m_height = height;
        return *this;
    }
    Config& SetLimitDepth( uint32_t limit_depth )
    {
        m_limit_depth = limit_depth;
        return *this;
    }
    Config& SetLimitNodes( uint64_t limit_nodes )
    {
        m_limit_nodes = limit_nodes;
        return *this;
    }
    Config& SetMaxMemory( uint64_t max_memory )
    {
        m_max_memory = ( max_memory == 0ULL ) ? kTTMemorySize : max_memory;
        return *this;
    }
    Config& SetTimeLeft( uint32_t time_left )
    {
        m_time_left = time_left;
        return *this;
    }
    Config& SetTimeInc( uint32_t time_inc )
    {
        m_time_inc = time_inc;
        return *this;
    }
    Config& SetTimeoutMatch( uint32_t timeout_match )
    {
        m_timeout_match = timeout_match;
        return *this;
    }
    Config& SetTimeoutTurn( uint32_t timeout_turn )
    {
        m_timeout_turn = timeout_turn;
        return *this;
    }
    Config& SetWidth( coord_t width )
    {
        m_width = width;
        return *this;
    }
    /**@}*/

    /**@{*/
    /** Getters */
    [[nodiscard]] int32_t  GetContinuous() const { return m_continuous; }
    [[nodiscard]] int32_t  GetGameType() const { return m_game_type; }
    [[nodiscard]] coord_t  GetHeight() const { return m_height; }
    [[nodiscard]] uint32_t GetLimitDepth() const { return m_limit_depth; }
    [[nodiscard]] uint64_t GetLimitNodes() const { return m_limit_nodes; }
    [[nodiscard]] uint64_t GetMaxMemory() const { return m_max_memory; }
    [[nodiscard]] int32_t  GetRule() const { return m_rule; }
    [[nodiscard]] uint32_t GetTimeLeft() const { return m_time_left; }
    [[nodiscard]] uint32_t GetTimeInc() const { return m_time_inc; }
    [[nodiscard]] uint32_t GetTimeoutMatch() const { return m_timeout_match; }
    [[nodiscard]] uint32_t GetTimeoutTurn() const { return m_timeout_turn; }
    [[nodiscard]] coord_t  GetWidth() const { return m_width; }
    /**@}*/

private:
    uint32_t m_timeout_turn { 0U };          /**< time for one turn in milliseconds */
    uint32_t m_timeout_match { 0U };         /**< total time for a game */
    uint32_t m_time_left { 0U };             /**< remaining time for a game */
    uint32_t m_time_inc { 0U };              /**< time increment per move */
    uint64_t m_max_memory { kTTMemorySize }; /**< maximum memory in bytes, zero if unlimited */
    uint32_t m_limit_depth { 28U };          /**< search depth in plys */
    uint64_t m_limit_nodes { 0U };           /**< search limit in nodes searched */
    int32_t  m_game_type { 0 };              /**< 0:human, 1:AI opponent, 2:tournament, 3:network tournament */
    int32_t  m_rule { 0 };                   /**< 0:five or more stones win, 1:exactly five stones win, 4:renju */
    int32_t  m_continuous { 0 };             /**< 0:single game, 1:continuous */
    coord_t  m_width { 20U };                /**< the board size */
    coord_t  m_height { 20U };               /**< the board size */
};

#endif // CONFIG_H
