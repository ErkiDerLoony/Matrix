#include <string.h>
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
  Pixmap buffer = XCreatePixmap(display, window, attributes.width, attributes.height, attributes.depth);

  Atom deleteMessage = XInternAtom(display, "WM_DELETE_WINDOW", 0);
  XSetWMProtocols(display, window, &deleteMessage, 1);

  XEvent event;
  int killed = 0;
  XColor darkGreenx, darkGreens, blackx, blacks;
  XAllocNamedColor(display, DefaultColormapOfScreen(DefaultScreenOfDisplay(display)),
                   "black", &blacks, &blackx);
  XAllocNamedColor(display, DefaultColormapOfScreen(DefaultScreenOfDisplay(display)),
                   "dark green", &darkGreens, &darkGreenx);

  GC graphics = XCreateGC(display, window, 0, NULL);
  XSetGraphicsExposures(display, graphics, 0);

  char *text = malloc(512*sizeof(char));

  if (text == NULL) {
    perror("malloc");
    exit(1);
  }

  int counter = 0;

  while (!killed) {
    counter++;
    sprintf(text, "Hallo Welt %d", counter);
    XNextEvent(display, &event);

    switch (event.type) {
    case ConfigureNotify:
      XGetWindowAttributes(display, window, &attributes);
      XFreePixmap(display, buffer);
      buffer = XCreatePixmap(display, window, attributes.width, attributes.height, attributes.depth);
      break;
    case MapNotify:
    case Expose:;
      XSetForeground(display, graphics, blackx.pixel);
      XFillRectangle(display, buffer, graphics, 0, 0, attributes.width, attributes.height);
      XSetForeground(display, graphics, darkGreens.pixel);
      XDrawString(display, buffer, graphics, 10, 50, text, strlen(text));
      XCopyArea(display, buffer, window, graphics, 0, 0, attributes.width, attributes.height, 0, 0);
      break;
    case ClientMessage:

      if (event.xclient.data.l[0] == deleteMessage) {
        killed = 1;
      }

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

      sprintf(message, "Unhandled X event type: %d\n", event.type);
      fprintf(stderr, message);
      free(message);
    }

    usleep(33);
  }

  XCloseDisplay(display);
  return 0;
}
