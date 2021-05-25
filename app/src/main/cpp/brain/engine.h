/*
  Embryo, a Gomoku/Renju playing engine
  Copyright (C) 2015-2021 Miroslav Fontan

  Embryo is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Embryo is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifndef ENGINE_H
#define ENGINE_H

/**
 * @file engine.h
 * @brief Game brain declarations
 **/

#include "lockedQueue.h"
#include <atomic>
#include <iostream>
#include <string>
#include <thread>


/**
 * @class Engine
 * @brief Main class, read and execute commands, owns board, transposition table
 */
class Engine {
public:

    /**
    *@brief Constructor
    *@param boardSize desk dimension
    */
    explicit Engine(const int boardSize);

    static Engine &GetInstance() {
        static Engine instance(20);
        return instance;
    }

    ~Engine();

    std::string GetMessage() { return "Hello from Brain"; }

    Engine &AddCommandsToInputQueue(const std::string &LastCommand);

    std::string ReadFromOutputQueue(int timeOutMs);

    /**
    *@brief Main engine loop
    */
    bool Loop();

    /**
    * @brief One iteration action in a main engine loop
    * @param cmd string command
    * @return false by END command, true otherwise
    */
    [[nodiscard]] bool CmdExecute(const std::string &cmd);

    std::string ReadLine();

    /**
    *@brief Send about info
    */
    void CmdAbout() const;

    void StartLoop();

    void StopLoop();

    LockedQueue<std::string> m_queueIn;                    /**< input data  */
    LockedQueue<std::string> m_queueOut;                    /**< input data  */
    std::thread m_runner;
    std::atomic_bool m_loopIsRunning = false;

};

#endif // ENGINE_H
