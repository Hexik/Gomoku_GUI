#ifndef GOMOKU_GUI_ANDROIDBUFFER_H
#define GOMOKU_GUI_ANDROIDBUFFER_H

#include <streambuf>

class AndroidBuffer : public std::streambuf {
public:
    AndroidBuffer();

private:
    int overflow( int c ) override;

    size_t                  idx         = 0U;
    static constexpr size_t kBufferSize = 1024U;
    char                    buffer[kBufferSize];
};

#endif //GOMOKU_GUI_ANDROIDBUFFER_H
