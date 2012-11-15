#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <X11/Xlib.h>

int main(int argc, char** argv) {
  Display *display = XOpenDisplay(NULL);

  if (display == NULL) {
    fprintf(stderr, "Cannot open display!\n");
    exit(1);
  }

  int screen = DefaultScreen(display);
  Window window;

  if (argc < 2) {
    window = XCreateSimpleWindow(display, RootWindow(display, screen), 200, 200, 400, 400, 1,
                                 BlackPixel(display, screen),
                                 WhitePixel(display, screen));
    XStoreName(display, window, "Matrix");
    XMapWindow(display, window);
  } else {
    window = DefaultRootWindow(display);
  }

  XSelectInput(display, window, ExposureMask | KeyPressMask | StructureNotifyMask);

  XWindowAttributes attributes;
  XGetWindowAttributes(display, window, &attributes);

  XEvent event;
  int killed = 0;
  XColor darkGreenx, darkGreens, blackx, blacks;
  XAllocNamedColor(display, DefaultColormapOfScreen(DefaultScreenOfDisplay(display)),
                   "black", &blacks, &blackx);
  XAllocNamedColor(display, DefaultColormapOfScreen(DefaultScreenOfDisplay(display)),
                   "dark green", &darkGreens, &darkGreenx);
  XSetForeground(display, DefaultGC(display, screen), darkGreens.pixel);
  XSetBackground(display, DefaultGC(display, screen), blacks.pixel);

  while (!killed) {
    XNextEvent(display, &event);

    switch (event.type) {
    case ConfigureNotify:
      XGetWindowAttributes(display, window, &attributes);
      break;
    case MapNotify:
    case Expose:
      XFillRectangle(display, window, DefaultGC(display, screen), 10, 10, attributes.width - 20, attributes.height - 20);
      XDrawString(display, window, DefaultGC(display, screen), 10, 50, "Hallo Welt", strlen("Hallo Welt"));
      break;
    case KeyPress:
      killed = 1;
      break;
    default:;
      char *message = malloc(512*sizeof(char));

      if (message == NULL) {
        perror("malloc");
        exit(1);
      }

      sprintf(message, "Unknown X event type: %d\n", event.type);
      fprintf(stderr, message);
    }
  }

  XCloseDisplay(display);
  return 0;
}
