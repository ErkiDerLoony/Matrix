file(GLOB SRC ${CMAKE_CURRENT_SOURCE_DIR}/*.c)

find_package(X11 REQUIRED)
include_directories(${X11_INCLUDE_DIR} ${X11_Xft_INCLUDE_PATH})

find_package(Freetype REQUIRED)
include_directories(${FREETYPE_INCLUDE_DIRS})

add_executable(matrix ${SRC})
target_link_libraries(matrix ${X11_LIBRARIES} ${X11_Xft_LIB})
target_link_libraries(matrix ${FREETYPE_LIBRARIES})

install(TARGETS matrix DESTINATION bin)
