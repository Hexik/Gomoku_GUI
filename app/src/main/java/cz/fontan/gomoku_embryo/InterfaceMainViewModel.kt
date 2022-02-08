package cz.fontan.gomoku_embryo

import cz.fontan.gomoku_embryo.game.Move

/**
 * Basic interface between MainActivity, BoardView an MainViewModel
 */
interface InterfaceMainViewModel {

    /**
     * Checks if move can be done, eg. empty square, valid coordinates
     * @param move test this Move
     */
    fun canMakeMove(move: Move): Boolean

    /**
     * Get Move played at i-th order
     * @param i order number
     */
    fun getIthMove(i: Int): Move

    /**
     * Searching or Idle
     */
    fun isSearching(): Boolean

    /**
     * Put move on the Board and recalc all corresponding data
     */
    fun makeMove(move: Move, sendBoard: Boolean)

    /**
     * Get best move in current search
     */
    fun getBestMove(): Move

    /**
     * Get list of fouls
     */
    fun getForbid(): String

    /**
     * Get count of moves on Board
     */
    fun moveCount(): Int

    /**
     * Refresh data, redraw board
     */
    fun refresh()

    /**
     * list of losing moves
     */
    fun getLosers(): ArrayList<Move>

    /**
     * Blocked cells (Wall) list
     */
    fun getBlockers(): ArrayList<Move>
}