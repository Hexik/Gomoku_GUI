#ifndef BOARD_H
#define BOARD_H

/**
 * @file board.h
 * @brief Game board representation
 */

#include "gameTypes.h"

#include <cassert>
#include <cstring>

/**
 * @class Board
 * @brief Game board representation
 */
class Board {
public:

    /**
     * @brief Constructor
     * @param dim board dimension for the game
     */
    explicit Board( const coord_t dim );

    /**
     * @brief Constructor
     * @param dimX board dimension for the game
     * @param dimY board dimension for the game
     */
    explicit Board( const coord_t dimX, const coord_t dimY );

    /**
     * @brief Copy Constructor
     * @param other board copy from
     */
    Board( const Board& other );
    ~Board() = default; /**< destructor */
    Board() = delete;                 /**< hidden default constructor */
    Board( Board&& ) = delete;                 /**< hidden move copy constructor */
    Board& operator=( const Board& ) = delete; /**< hidden assignment operator @return this */
    Board& operator=( Board&& ) = delete;      /**< hidden move assignment operator @return this */

    /**
     * @brief Prepare board for game
     */
    void Reset();

    /**
     * @brief Add move, update board structures
     * @param m move coordinates and piece
     */
    void MakeMove( const Move m );

    /**
     * @brief Put move on board, real worker, update board structures
     * @param m move coordinates and piece
     */
    template<eMove_t player>
    void PutMove( const Move m );

    /**
     * @brief Undo the move, update board structures
     * @param m move coordinates and piece
     */
    void UndoMove( const Move m );

    /**
     * @brief Test if move coordinates are in range
     * @param m move coordinates and piece
     */
    [[nodiscard]] bool CheckCoords( const Move m ) const;

    /**
     * @brief Check if field for move is free and m has valid coords and type
     * @param m move coordinates and piece
     */
    [[nodiscard]] bool CanMakeMove( const Move m ) const;

    /**
     * @brief Generate random move
     * @tparam player side to move
     */
    template<eMove_t player>
    [[nodiscard]] Move GenerateRandomMove() const {
        auto m = MOVE_NONE;
        if( !IsFull()) {
            do {
                m = createMove<player>( Util::rand_xor128() % GetDimX(),
                                        Util::rand_xor128() % GetDimY());
            } while( !CanMakeMove( m ));
        }
        return m;
    }

    /**
     * @brief Check if board is full
     * @return board full status
     */
    [[nodiscard]] bool IsFull() const;

    /**
     * @brief Put move on desk
     * @param m Move
     * @param player side to move
     */
    void SetDesk( Move m, eMove_t player ) { m_desk[GetCoords( m )] = player; }
    /**@}*/

    /**@{*/
    /** Getters */
    [[nodiscard]] coord_t GetDimX() const { return m_DimX; }

    [[nodiscard]] coord_t GetDimY() const { return m_DimY; }

    [[nodiscard]] size_t GetGamePly() const { return m_gamePly; }

    [[nodiscard]] inline eMove_t GetDesk( coord_t x, coord_t y ) const {
        return m_desk[x * kBoardSize + y];
    }

    [[nodiscard]] inline eMove_t GetDesk( Move m ) const { return m_desk[GetCoords( m )]; }
    /**@}*/

    /**@{
     * @brief Other properties of the position
     */
    void SwitchSideToMove() const { sideToMove = !sideToMove; }

    bool SideToMove() const { return sideToMove; }

    void SetSideToMove( bool stm ) { sideToMove = stm; }
    /**@}*/

    /**@{
    * @brief subscript operator
    * @param ply move order in game
    * @return move at index
    */
    constexpr Move
    operator[]( size_t ply ) const { return m_playedMoves[ply]; } /* const subscript operator */
    Move& operator[]( size_t ply ) { return m_playedMoves[ply]; } /* subscript operator */
    /**@} */

private:
    size_t        m_gamePly;                         /**< how many moves on the board */
    const coord_t m_DimX;                            /**< board dimension X-axis */
    const coord_t m_DimY;                            /**< board dimension Y-axis */
    mutable bool  sideToMove;                        /**< player to move */

    eMove_t m_desk[kBoardSize * kMaxBoard];          /**< main board */
    Move    m_playedMoves[kBoardSize * kPlaySize];   /**< already played moves in correct order */

};

inline bool Board::CheckCoords( const Move m ) const {
    return ( GetX( m ) < m_DimX ) && ( GetY( m ) < m_DimY );
}

inline bool Board::CanMakeMove( const Move m ) const {
    assert( CheckCoords( m ));
    assert( !IsEmpty( m ));

    return m_desk[GetCoords( m )] == eMove_t::eEmpty;
}

#endif // BOARD_H
