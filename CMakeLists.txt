project(Matrix C)
cmake_minimum_required(VERSION 2.6)
add_definitions("-Wall -O2 -g")

include_directories(src)
add_subdirectory(src)

find_package(Doxygen)

if (DOXYGEN_FOUND)
  add_custom_target(doc ${DOXYGEN_EXECUTABLE} ${CMAKE_SOURCE_DIR}/Doxyfile)
  set_property(DIRECTORY APPEND PROPERTY ADDITIONAL_MAKE_CLEAN_FILES doc)
endif (DOXYGEN_FOUND)
