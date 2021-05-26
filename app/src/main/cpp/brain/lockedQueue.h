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

#ifndef LOCKED_QUEUE_H
#define LOCKED_QUEUE_H

/**
 * @file lockedQueue.h
 * @brief locked queue implementation
 */

#include <condition_variable>
#include <mutex>
#include <queue>

/**
 * @class LockedQueue
 * @brief Simple concurrent queue
 * @tparam T data type
 */
template<typename T>
class LockedQueue final : std::queue<T> {
public:
    LockedQueue() {}

    ~LockedQueue() {}

    /**
    *@brief Peeks the first item, blocks if no data until timeout occurs
    *@param timeout_ms how long to wait, 0ms is blocking wait
    */
    T front( int timeout_ms ) const {
        auto lock = std::unique_lock<std::mutex>( m_mutex );

        if ( timeout_ms == 0 ) {
            m_cond_var.wait( lock, [this] { return !std::queue<T>::empty(); } );
        } else {
            if ( !m_cond_var.wait_for( lock, std::chrono::milliseconds( timeout_ms ),
                                       [=] { return !std::queue<T>::empty(); } )) {
                // timeout occurred, empty data
                return {};
            }
        }

        return std::queue<T>::front();
    }

    /**
    *@brief Pops the first item, blocks if no data until timeout occurs
    *@param timeout_ms how long to wait, 0ms is blocking wait
    */
    T pop( int timeout_ms ) {
        auto lock = std::unique_lock<std::mutex>( m_mutex );

        if ( timeout_ms == 0 ) {
            m_cond_var.wait( lock, [this] { return !std::queue<T>::empty(); } );
        } else {
            if ( !m_cond_var.wait_for( lock, std::chrono::milliseconds( timeout_ms ),
                                       [=] { return !std::queue<T>::empty(); } )) {
                // timeout occurred, empty data
                return {};
            }
        }

        T ret = std::move( std::queue<T>::front());
        std::queue<T>::pop();
        return ret;
    }

    /**
    *@brief Peeks the first items, blocks if no data
    *@param item data to store
    */
    void push( const T& item ) {
        {
            auto lock = std::unique_lock<std::mutex>( m_mutex );
            std::queue<T>::push( item );
        }
        m_cond_var.notify_one();
    }

    /**
    *@brief Peeks the first items, blocks if no data
    *@param item data to store
    */
    void push( T&& item ) {
        {
            auto lock = std::unique_lock<std::mutex>( m_mutex );
            std::queue<T>::push( std::move( item ));
        }
        m_cond_var.notify_one();
    }

    /**
    *@brief Return true if empty
    */
    bool is_empty() const {
        auto lock = std::unique_lock<std::mutex>( m_mutex );
        return std::queue<T>::empty();
    }

    /**
    *@brief Clear the queue
    */
    void clear() {
        auto lock = std::unique_lock<std::mutex>( m_mutex );
        std::queue<T>::swap( std::queue<T>());
    }

    /**
    *@brief Returns count of items
    */
    size_t count() const {
        auto lock = std::unique_lock<std::mutex>( m_mutex );
        return static_cast<size_t>( std::queue<T>::size());
    }

private:
    mutable std::mutex              m_mutex;    /**< sync primitive, internal state*/
    mutable std::condition_variable m_cond_var; /**< conditional var, internal state */
};

#endif // LOCKED_QUEUE_H
