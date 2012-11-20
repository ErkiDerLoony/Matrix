#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

#include <X11/Xlib.h>
#include <X11/keysym.h>

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

  XSelectInput(display, window, ExposureMask | KeyReleaseMask | StructureNotifyMask);

  XWindowAttributes attributes;
  XGetWindowAttributes(display, window, &attributes);
  Pixmap buffer = XCreatePixmap(display, window, attributes.width, attributes.height, attributes.depth);

  Atom deleteMessage = XInternAtom(display, "WM_DELETE_WINDOW", 0);
  XSetWMProtocols(display, window, &deleteMessage, 1);

  XColor green;
  green.red = 0;
  green.green = 0xFFFF;
  green.blue = 0;

  XColor black;
  black.red = 0;
  black.green = 0;
  black.blue = 0;

  Colormap cmap = DefaultColormapOfScreen(DefaultScreenOfDisplay(display));
  XAllocColor(display, cmap, &green);
  XAllocColor(display, cmap, &black);

  GC graphics = XCreateGC(display, window, 0, NULL);
  XSetGraphicsExposures(display, graphics, 0);

  char *text = malloc(512*sizeof(char));

  if (text == NULL) {
    perror("malloc");
    exit(1);
  }

  XEvent event;
  int counter = 0;
  int killed = 0;

  while (!killed) {
    counter++;
    sprintf(text, "Hallo Welt %d", counter);

    XSetForeground(display, graphics, black.pixel);
    XFillRectangle(display, buffer, graphics, 0, 0, attributes.width, attributes.height);
    XSetForeground(display, graphics, green.pixel);
    XDrawString(display, buffer, graphics, 10, 50, text, strlen(text));
    XCopyArea(display, buffer, window, graphics, 0, 0, attributes.width, attributes.height, 0, 0);
    XSync(display, 0);

    if (XEventsQueued(display, &event) > 0) {
      XNextEvent(display, &event);

      switch (event.type) {
      case ConfigureNotify:
        XGetWindowAttributes(display, window, &attributes);
        XFreePixmap(display, buffer);
        buffer = XCreatePixmap(display, window, attributes.width, attributes.height, attributes.depth);
        break;
      case MapNotify:
      case Expose:;
        break;
      case ClientMessage:

        if (event.xclient.data.l[0] == deleteMessage) {
          killed = 1;
        }

        break;
      case KeyRelease:;
        KeySym key = XLookupKeysym(&event.xkey, 0);

        if (key == XK_Escape || key == XK_q) {
          killed = 1;
        }

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
    }

    usleep(1000);
  }

  free(text);
  XFreeColors(display, cmap, &black.pixel, 1, 0);
  XFreeColors(display, cmap, &green.pixel, 1, 0);
  XFreeGC(display, graphics);
  XCloseDisplay(display);
  return 0;
}
