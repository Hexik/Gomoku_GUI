package cz.fontan.gomoku_gui

import cz.fontan.gomoku_gui.game.Move

interface InterfaceMain {
    fun canMakeMove(move: Move): Boolean
    fun getIthMove(i: Int): Move
    fun isSearching(): Boolean
    fun makeMove(move: Move)
    fun moveCount(): Int
}