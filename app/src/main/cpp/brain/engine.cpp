/**
 * @file engine.cpp
 * @brief Game brain implementation
 **/

#include "engine.h"

#include <android/log.h>

Engine::Engine(const int /*boardSize*/) :
        m_queueIn(), m_queueOut() {
    StartLoop();
}

Engine::~Engine() {
    StopLoop();
    if (m_runner.joinable()) {
        m_runner.join();
    }
}

Engine &Engine::AddCommandsToInputQueue(const std::string &LastCommand) {
    __android_log_write(ANDROID_LOG_DEBUG, "AddCommandsToInputQueue", LastCommand.c_str());
    auto res = LastCommand;
    auto posToken = size_t{0};

    while ((posToken = res.find('\n')) != std::string::npos) {
        m_queueIn.push(res.substr(0, posToken));
        res.erase(0, posToken + 1);
    }
    m_queueIn.push(res);
    return *this;
}

bool Engine::Loop() {
    m_loopIsRunning = true;
    while (CmdExecute(ReadLine())) {
    }
    __android_log_write(ANDROID_LOG_DEBUG, "Loop", "");
    m_loopIsRunning = false;
    return true;
}

std::string Engine::ReadLine() {
    return m_queueIn.pop(0);
}

bool Engine::CmdExecute(const std::string &cmd) {
    const auto tmpUpper = cmd;
    __android_log_write(ANDROID_LOG_DEBUG, "Exec", cmd.c_str());
    if (tmpUpper == "about") { m_queueOut.push("ABOUT BRAIN"); }
    if (tmpUpper == "end") { return false; }

    return true;
}

std::string Engine::ReadFromOutputQueue(int timeOutMs) {
    return m_queueOut.pop(timeOutMs);
}

void Engine::StartLoop() {
    if (!m_loopIsRunning) {
        m_runner = std::thread(&Engine::Loop, this);
    }
}

void Engine::StopLoop() {
    if (m_loopIsRunning) {
        m_queueOut.push("end");
    }
}

