/**
 * @file engine.cpp
 * @brief Game brain implementation
 **/

#include "engine.h"
#include "board.h"

#include <android/log.h>

Engine::Engine( const uint32_t boardSize ) :
        m_queueIn(), m_queueOut(), m_infoWidth( boardSize ), m_infoHeight( boardSize ) {
    StartLoop();
}

Engine::~Engine() {
    StopLoop();
    if( m_runner.joinable()) {
        m_runner.join();
    }
}

Engine& Engine::AddCommandsToInputQueue( const std::string& LastCommand ) {
    __android_log_write( ANDROID_LOG_DEBUG, "AddCommandsToInputQueue", LastCommand.c_str());
    auto res      = Util::StringToUpper( LastCommand );
    auto posToken = size_t{0};

    while(( posToken = res.find( '\n' )) != std::string::npos ) {
        m_queueIn.push( res.substr( 0, posToken ));
        res.erase( 0, posToken + 1 );
    }
    m_queueIn.push( res );
    return *this;
}

bool Engine::Loop() {
    m_loopIsRunning = true;
    while( CmdExecute( ReadInputLine())) {
    }
    __android_log_write( ANDROID_LOG_DEBUG, "Loop", "" );
    m_loopIsRunning = false;
    return true;
}

std::string Engine::ReadInputLine() {
    return m_queueIn.pop( 0 );
}

void Engine::WriteOutputLine( const std::string& data ) const {
    m_queueOut.push( data );
}

Engine::eCommand Engine::ParseCmd( const std::string& s, std::string& rest ) {
    rest                = "";
    using eCommandVector = std::vector<std::pair<std::string, Engine::eCommand>>;
    const auto keywords = eCommandVector{{"ABOUT",        eCommand::eAbout},
                                         {"BEGIN",        eCommand::eBegin},
                                         {"END",          eCommand::eEnd},
                                         {"TURN",         eCommand::eTurn},
                                         {"TAKEBACK",     eCommand::eTakeback},
                                         {"BOARD",        eCommand::eBoard},
                                         {"PLAY",         eCommand::ePlay},
                                         {"START",        eCommand::eStart},
                                         {"RESTART",      eCommand::eRestart},
                                         {"INFO",         eCommand::eInfo},
                                         {"YXBOARD",      eCommand::eYxBoard},
                                         {"YXSHOWFORBID", eCommand::eYxShowForbid},
                                         {"YXSTOP",       eCommand::eYxStop}};

    __android_log_write( ANDROID_LOG_VERBOSE, "Parse", s.c_str());

    const auto it = std::find_if( std::begin( keywords ), std::end( keywords ),
                                  [s]( const auto& a ) {
                                      auto nPos = size_t{0};
                                      return (( nPos = s.find( a.first )) != std::string::npos ) &&
                                             ( nPos == 0 ) &&
                                             ( s.length() == a.first.length() ||
                                               s[a.first.length()] == ' ' );
                                  } );

    if( it != std::end( keywords )) {
        rest = Util::Trim( s.substr( it->first.length()));
        return it->second;
    }
    return eCommand::eUnknown;
}

bool Engine::CmdExecute( const std::string& cmd ) {
    const auto tmpUpper = Util::StringToUpper( cmd );
    __android_log_write( ANDROID_LOG_DEBUG, "Exec", cmd.c_str());
    std::string rest;
    const auto  eCmd    = ParseCmd( tmpUpper, rest );

    if( m_bInSearch &&
        ( eCmd != eCommand::eEnd && eCmd != eCommand::eYxStop )) {
        pipeOutMessage( ": BAD COMMAND ", tmpUpper.c_str(), " IN SEARCH MODE" );
        return true;
    }

    auto bLoop = true;

    switch( eCmd ) {
        case eCommand::eAbout:
            CmdAbout();
            break;
        case eCommand::eBegin:
            CmdTurn();
            break;
        case eCommand::eBoard:
            CmdParseBoard( false );
            CmdTurn();
            break;
        case eCommand::eEnd:
            bLoop = false;
            break;
        case eCommand::eInfo:
            CmdParseInfo( rest );
            break;
        case eCommand::ePlay:
            CmdParsePlay( rest );
            break;
        case eCommand::eRestart:
            m_board->Reset();
            pipeOut( "OK" );
            break;
        case eCommand::eStart:
            CmdParseStart( rest );
            break;
        case eCommand::eTakeback:
            CmdParseTakeback( rest );
            break;
        case eCommand::eTurn:
            CmdParseTurn( rest );
            CmdTurn();
            break;
        case eCommand::eYxBoard:
            CmdParseBoard( false );
            break;
        case eCommand::eYxShowForbid:
            CmdShowForbid();
            break;
        case eCommand::eYxStop:
            break;
        case eCommand::eUnknown:
            pipeOut( "UNKNOWN ", tmpUpper.c_str());
            break;
    }
    return bLoop;
}

std::string Engine::ReadFromOutputQueue( int timeOutMs ) {
    return m_queueOut.pop( timeOutMs );
}

void Engine::StartLoop() {
    if( !m_loopIsRunning ) {
        m_runner = std::thread( &Engine::Loop, this );
    }
}

void Engine::StopLoop() {
    if( m_loopIsRunning ) {
        m_queueOut.push( "end" );
    }
}

void Engine::CmdAbout() const {
    pipeOut( R"(Generic Engine)" );
}

void Engine::CmdTurn() {
    const auto m = m_board->GenerateRandomMove<eMove_t::eXX>();
    pipeOut( GetX( m ), ",", GetY( m ));
}

void Engine::CmdPutMyMove( const coord_t x, const coord_t y ) {
    if( !CmdPutMove<eMove_t::eXX>( x, y )) {
        pipeOut( "ERROR my move [", x, ",", y, "]" );
    }
}

void Engine::CmdPutYourMove( const coord_t x, const coord_t y ) {
    if( !CmdPutMove<eMove_t::eOO>( x, y )) {
        pipeOut( "ERROR opponent's move [", x, ",", y, "]" );
    }
}

void
Engine::CmdUndoMove( const coord_t x, const coord_t y, const eMove_t type, const bool takeback ) {
    const auto m = SetType( SetCoords( x, y ), type );

    if( !m_board->CheckCoords( m ) || m_board->CanMakeMove( m )) {
        pipeOut( "ERROR takeback move [", x, ",", y, "] = ",
                 static_cast<unsigned int>( m_board->GetDesk( m )));
        return;
    }
    m_board->UndoMove( m );
    if( type == eMove_t::eXX ) {
        m_board->SwitchSideToMove();
        if( takeback ) {
            pipeOut( "OK" );
        }
    }
}

template<eMove_t player>
bool Engine::CmdPutMove( const coord_t x, const coord_t y ) {
    static_assert( player == eMove_t::eXX || player == eMove_t::eOO, "Bad player" );
    const auto m = createMove<player>( x, y );

    if( m_board->CheckCoords( m ) && m_board->CanMakeMove( m )) {
        m_board->MakeMove( m );
        return true;
    }
    if( !m_board->CheckCoords( m )) {
        pipeOutDebug( "check move [", x, ",", y, "]" );
    } else if( !m_board->CanMakeMove( m )) {
        pipeOutDebug( "make move [", x, ",", y, "] = ", m_board->GetDesk( x, y ));
    }

    return false;
}

void Engine::CmdParseStart( const std::string& params ) {
    const auto&& v = Util::ParseNumbers( params, "," );

    if( v.size() == 1 ) {
        if( v[0] < 5 || static_cast<uint32_t>( v[0] ) > kPlaySize ) {
            __android_log_write( ANDROID_LOG_INFO, "size [0]=", params.c_str());
            pipeOut( "ERROR size of the board" );
            return;
        }
        CmdStart( static_cast<uint32_t>( v[0] ));
        return;
    }

    __android_log_write( ANDROID_LOG_INFO, "size [0]=", params.c_str());
    pipeOut( "ERROR size of the board" );
}

void Engine::CmdStart( const coord_t size ) { CmdRectStart( size, size ); }

void Engine::CmdRectStart( const coord_t sizeX, const coord_t sizeY ) {
    assert( sizeX >= 5 && sizeX <= kPlaySize );
    assert( sizeY >= 5 && sizeY <= kPlaySize );

    m_infoWidth  = sizeX;
    m_infoHeight = sizeY;

    m_board = std::make_unique<Board>( m_infoWidth, m_infoHeight );
    pipeOut( "OK" );
}

void Engine::CmdParseBoard( bool flipSides ) {
    m_board = std::make_unique<Board>( m_infoWidth, m_infoHeight );

    auto firstMove = true;
    while( true ) {
        const auto&& s = ReadInputLine();
        if( s.find( "DONE" ) != std::string::npos ) {
            m_board->SetSideToMove( true );
            break;
        }
        const auto&& v = Util::ParseNumbers( s, "," );

        if( v.size() == 3 ) {
            if(( !flipSides && v[2] == 1 ) || ( flipSides && v[2] == 2 )) {
                if( firstMove ) {
                    firstMove = false;
                }
                CmdPutMyMove( static_cast<uint32_t>( v[0] ), static_cast<uint32_t>( v[1] ));
            } else if(( !flipSides && v[2] == 2 ) || ( flipSides && v[2] == 1 )) {
                if( firstMove ) {
                    firstMove = false;
                }
                CmdPutYourMove( static_cast<uint32_t>( v[0] ), static_cast<uint32_t>( v[1] ));
            } else {
                break;
            }
        } else {
            if( s.find( "DONE" ) == std::string::npos ) {
                pipeOut( "ERROR x,y,who or DONE expected after BOARD" );
            }
            break;
        }
    }

}

void Engine::CmdShowForbid() {
}

void Engine::CmdParseInfo( const std::string&/*params*/) {
}

std::optional<std::vector<int64_t>> Engine::CmdParseCoords( const std::string& params ) {
    const auto v = Util::ParseNumbers( params, "," );

    if( v.size() != 2 || static_cast<uint32_t>( v[0] ) >= m_infoWidth ||
        static_cast<uint32_t>( v[1] ) >= m_infoHeight ) {
        pipeOut( "ERROR bad coordinates" );
        return std::nullopt;
    }
    return std::make_optional( v );
}

void Engine::CmdParsePlay( const std::string& params ) {
    if( const auto v = CmdParseCoords( params )) {
        CmdPutMyMove( static_cast<uint32_t>( v.value()[0] ), static_cast<uint32_t>( v.value()[1] ));
    }
}

void Engine::CmdParseTurn( const std::string& params ) {
    if( const auto v = CmdParseCoords( params )) {
        CmdPutYourMove( static_cast<uint32_t>( v.value()[0] ),
                        static_cast<uint32_t>( v.value()[1] ));
    }
}

void Engine::CmdParseTakeback( const std::string& params ) {
    if( const auto v = CmdParseCoords( params )) {
        CmdUndoMove( static_cast<uint32_t>( v.value()[0] ), static_cast<uint32_t>( v.value()[1] ),
                     eMove_t::eXX, true );
    }
}
