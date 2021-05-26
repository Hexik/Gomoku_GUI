/**
 * @file board.cpp
 * @brief Game board representation
 */

#include "board.h"

#include <cstring>

Board::Board( const coord_t dim ) : Board( dim, dim ) {}

Board::Board( const coord_t dimX, const coord_t dimY ) :
        m_gamePly( DEPTH_ZERO ),
        m_DimX( dimX ),
        m_DimY( dimY ),
        sideToMove( true ) {
    assert( m_DimX <= kPlaySize );
    assert( m_DimY <= kPlaySize );
    assert( m_DimX >= 5 );
    assert( m_DimY >= 5 );
    Reset();
}

Board::Board( const Board& other ) : Board( other.GetDimX(), other.GetDimY()) {
    for ( coord_t x = 0; x < m_DimX; x++ ) {
        for ( coord_t y = 0; y < m_DimY; y++ ) {
            if ( other.m_desk[x * kBoardSize + y] != eMove_t::eEmpty ) {
                if ( other.m_desk[x * kBoardSize + y] == eMove_t::eXX ) {
                    MakeMove( createMove<eMove_t::eXX>( x, y ));
                } else {
                    MakeMove( createMove<eMove_t::eOO>( x, y ));
                }
            }
        }
    }

    sideToMove = other.SideToMove();
}

void Board::MakeMove( const Move m ) {
    assert( !IsEmpty( m ));
    assert( CanMakeMove( m ));

    if ( IsTypeXX( m )) {
        PutMove<eMove_t::eXX>( m );
        SetSideToMove( false );
    } else {
        PutMove<eMove_t::eOO>( m );
        SetSideToMove( true );
    }
}

template<eMove_t player>
void Board::PutMove( const Move m ) {
    static_assert( player == eMove_t::eXX || player == eMove_t::eOO, "Bad player" );

    m_playedMoves[m_gamePly++] = GetPlainMove( m );
    SetDesk( m, player );
}

void Board::UndoMove( const Move m ) {
    assert( m_gamePly > 0 );
    assert( CheckCoords( m ));
    assert( IsTypeXX( m ) || IsTypeOO( m ));
    assert( m_playedMoves[m_gamePly - 1] == GetPlainMove( m ));

    SwitchSideToMove();
    m_desk[GetCoords( m )] = eMove_t::eEmpty;
    --m_gamePly;

    assert( CanMakeMove( m ));
}

void Board::Reset() {
    m_gamePly  = DEPTH_ZERO;
    sideToMove = true;

    for ( auto& x : m_desk ) {
        x = eMove_t::eEmpty;
    }
}

bool Board::IsValid() const {
    return true;
}

bool Board::IsFull() const {
    assert( GetGamePly() <= static_cast<size_t>( m_DimX ) * m_DimY );
    return GetGamePly() == static_cast<size_t>( m_DimX ) * m_DimY;
}
