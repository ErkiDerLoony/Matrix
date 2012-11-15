file(GLOB SRC ${CMAKE_CURRENT_SOURCE_DIR}/*.c)

find_package(X11)

add_executable(matrix ${SRC})
target_link_libraries(matrix ${X11_LIBRARIES})

install(TARGETS matrix DESTINATION bin)
