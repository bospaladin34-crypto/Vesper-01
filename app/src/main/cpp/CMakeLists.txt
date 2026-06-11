cmake_minimum_required(VERSION 3.22.1)
project("braidc")

add_library(${CMAKE_PROJECT_NAME} SHARED
        braidc.cpp
        nephilim_ops.cc)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log)
