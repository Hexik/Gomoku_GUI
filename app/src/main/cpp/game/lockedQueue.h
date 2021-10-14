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
class LockedQueue final {
public:
    LockedQueue() {}

    ~LockedQueue() {}

    /**
    *@brief Peeks the first item, blocks if no data until timeout occurs
    *@param timeout_ms how long to wait, 0ms is blocking wait
    */
    T front( int timeout_ms ) const {
        auto lock = std::unique_lock<std::mutex>( m_mutex );

        if( timeout_ms == 0 ) {
            m_cond_var.wait( lock, [this] { return !m_queue.empty(); } );
        } else {
            if( !m_cond_var.wait_for( lock, std::chrono::milliseconds( timeout_ms ),
                                      [=] { return !m_queue.empty(); } )) {
                // timeout occurred, empty data
                return {};
            }
        }

        return m_queue.front();
    }

    /**
    *@brief Pops the first item, blocks if no data until timeout occurs
    *@param timeout_ms how long to wait, 0ms is blocking wait
    */
    T pop( int timeout_ms ) {
        auto lock = std::unique_lock<std::mutex>( m_mutex );

        if( timeout_ms == 0 ) {
            m_cond_var.wait( lock, [this] { return !m_queue.empty(); } );
        } else {
            if( !m_cond_var.wait_for( lock, std::chrono::milliseconds( timeout_ms ),
                                      [=] { return !m_queue.empty(); } )) {
                // timeout occurred, empty data
                return {};
            }
        }

        T ret = std::move( m_queue.front());
        m_queue.pop();
        return ret;
    }

    /**
    *@brief Add item to the queue
    *@param item data to store
    */
    void push( const T& item ) {
        {
            const auto lock = std::unique_lock<std::mutex>( m_mutex );
            m_queue.push( item );
        }
        m_cond_var.notify_one();
    }

    /**
    *@brief Move item to the queue
    *@param item data to store
    */
    void push( T&& item ) {
        {
            const auto lock = std::unique_lock<std::mutex>( m_mutex );
            m_queue.push( std::move( item ));
        }
        m_cond_var.notify_one();
    }

    /**
    *@brief Return true if empty
    */
    bool is_empty() const {
        const auto lock = std::unique_lock<std::mutex>( m_mutex );
        return m_queue.empty();
    }

    /**
    *@brief Clear the queue
    */
    void clear() {
        const auto lock = std::unique_lock<std::mutex>( m_mutex );
        m_queue.swap( std::queue<T>());
    }

    /**
    *@brief Returns count of items
    */
    size_t count() const {
        const auto lock = std::unique_lock<std::mutex>( m_mutex );
        return static_cast<size_t>( m_queue.size());
    }

private:
    std::queue<T>                   m_queue;    /**< data container */
    mutable std::mutex              m_mutex;    /**< sync primitive, internal state */
    mutable std::condition_variable m_cond_var; /**< conditional var, internal state */
};

#endif // LOCKED_QUEUE_H
