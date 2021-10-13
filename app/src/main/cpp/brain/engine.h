#ifndef ENGINE_H
#define ENGINE_H

/**
 * @file engine.h
 * @brief Game brain declarations
 **/

#include "gameTypes.h"
#include "config.h"
#include "lockedQueue.h"
#include <string>
#include <thread>
#include <sstream>
#include <iostream>
#include <optional>

class Board;

class Config;

/**
 * @class Engine
 * @brief Main class, read and execute commands, owns board, transposition table
 */
class Engine {
public:
    /**
    *@enum eCommand
    *@brief Enumerated commands for the brain
    */
    enum class eCommand {
        eAbout,              // standard Gomocup protocol
        eBegin,              // standard Gomocup protocol
        eBoard,              // standard Gomocup protocol
        eEnd,                // standard Gomocup protocol
        eInfo,               // standard Gomocup protocol
        ePlay,               // standard Gomocup protocol
        eRestart,            // standard Gomocup protocol
        eStart,              // standard Gomocup protocol
        eTakeback,           // standard Gomocup protocol
        eTurn,               // standard Gomocup protocol
        eUnknown,            // standard Gomocup protocol
        eAnResult,           // Android GUI extension
        eYxBoard,            // Yixin protocol enhancement
        eYxShowForbid,       // Yixin protocol enhancement
        eYxStop              // Yixin protocol enhancement
    };

    /**
    *@brief Constructor
    *@param boardSize desk dimension
    */
    explicit Engine( const uint32_t boardSize );

    ~Engine();

    Engine& AddCommandsToInputQueue( const std::string& LastCommand );

    std::string ReadFromOutputQueue( int timeOutMs );

    bool IsEmptyOutputQueue();

    /**
     * @brief If data are ready to read
     */
    [[nodiscard]] bool HasReaderInput() const { return !m_queueIn.is_empty(); }

    /**
    *@brief Read last pipe output
    *@return last output string
    */
    [[nodiscard]] std::string GetLastPipeOut() const { return std::move( m_LastPipeOut ); }

    /**
     * @brief Main engine loop
     */
    bool Loop();

    /**
     * @brief Send about info
     */
    void CmdAbout() const;

    static eCommand ParseCmd( const std::string& s, std::string& rest );

    /**
     * @brief Returns Board representation
     */
    [[nodiscard]] Board* GetBoard() const { return m_board.get(); }

    /**
     * @brief Reference to info data
     */
    [[nodiscard]] Config& GetInfo() { return m_info; }

    void CmdParseInfo( const std::string& params );

    /**
     * @brief One iteration action in a main engine loop
     * @param cmd string command
     * @return false by END command, true otherwise
     */
    [[nodiscard]] bool CmdExecute( const std::string& cmd );

    void CmdParseStart( const std::string& params );

    /**
     * @brief Start engine
     * @param size board size
     */
    void CmdStart( const coord_t size );
    void CmdPutMyMove( uint32_t x, uint32_t y );
    void CmdPutYourMove( uint32_t x, uint32_t y );
    std::string ReadInputLine();
    void StartLoop();
private:

    /**
    * @brief Parse info command data
    * @param s string to parse
    * @param rest data after command keyword
    * @return info data
    */
    [[nodiscard]] static std::string ParseInfo( const std::string& s, std::string& rest );

    void WriteOutputLine( const std::string& data ) const;

    void CmdResult() const;

    void StopLoop();

    void CmdTurn();

    void CmdParseBoard( bool flipSides );

    void CmdShowForbid();

    std::optional <std::vector<int64_t>> CmdParseCoords( const std::string& params );

    void CmdParseTurn( const std::string& params );

    void CmdParsePlay( const std::string& params );

    void CmdParseTakeback( const std::string& params );

    /**
    *@brief Remove move from board
    *@param x coordinate
    *@param y coordinate
    *@param type player
    *@param takeback call context
    */
    void CmdUndoMove( const coord_t x, const coord_t y, const eMove_t type, const bool takeback );

    /**
    *@brief Start engine
    *@param sizeX board size
    *@param sizeY board size
    */
    void CmdRectStart( const coord_t sizeX, const coord_t sizeY );

    /**
    * @brief Worker function, put move on board
    * @tparam player side to move
    * @param x coordinate
    * @param y coordinate
    * @return operation result
    */
    template<eMove_t player>
    [[nodiscard]] bool CmdPutMove( const coord_t x, const coord_t y );

    /**
    *@brief Send response to command
    *@param args data to output
    */
    template<typename... Args>
    void pipeOut( Args&& ... args ) const {
        std::stringstream ss;

        ( ss << ... << args );

        m_LastPipeOut = ss.str();

        __android_log_write( ANDROID_LOG_INFO, "PipeOut ", ss.str().c_str());

        WriteOutputLine( ss.str());
    }

    /**
    *@brief Send DEBUG info to console
    *@param args data to output
    */
    template<typename... Args>
    void pipeOutDebug( Args&& ... args ) const {
        pipeOut( "DEBUG ", args... );
    }

    /**
    *@brief Send MESSAGE info to console
    *@param args data to output
    */
    template<typename... Args>
    void pipeOutMessage( Args&& ... args ) const { pipeOut( "MESSAGE ", args... ); }

    Config                           m_info;                     /**< configuration data */
    std::unique_ptr <Board>          m_board{
            nullptr }; /**< pointer to main board representation */
    mutable LockedQueue<std::string> m_queueIn;        /**< input data  */
    mutable LockedQueue<std::string> m_queueOut;       /**< output data  */
    std::thread                      m_runner;
    std::atomic_bool                 m_loopIsRunning = false;
    uint32_t                         m_infoWidth;
    uint32_t                         m_infoHeight;
    bool                             m_bInSearch     = false;

    Move CalculateMove();
    mutable std::string              m_LastPipeOut;
};

extern Engine* instance; /**< singleton ptr */

#endif // ENGINE_H
