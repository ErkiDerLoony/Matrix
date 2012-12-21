#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <limits.h>

#include <X11/Xlib.h>
#include <X11/keysym.h>
#include <X11/Xft/Xft.h>

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

  Colormap cmap = DefaultColormapOfScreen(DefaultScreenOfDisplay(display));

  GC graphics = XCreateGC(display, window, 0, NULL);
  XSetGraphicsExposures(display, graphics, 0);

  Visual *visual = DefaultVisualOfScreen(DefaultScreenOfDisplay(display));
  XftFont *font = XftFontOpen(display, screen, XFT_FAMILY, XftTypeString, "Terminus", NULL);

  if (font == NULL) {
    fprintf(stderr, "Error loading font!\n");
    exit(1);
  }

  XftDraw *xft = XftDrawCreate(display, window, visual, cmap);

  XRenderColor greenColour;
  greenColour.red = 0;
  greenColour.green = USHRT_MAX;
  greenColour.blue = 0;
  greenColour.alpha = USHRT_MAX;

  XRenderColor blackColour;
  blackColour.red = 0;
  blackColour.green = 0;
  blackColour.blue = 0;
  blackColour.alpha = USHRT_MAX;

  XftColor green;
  XftColorAllocValue(display, visual, cmap, &greenColour, &green);

  XftColor black;
  XftColorAllocValue(display, visual, cmap, &blackColour, &black);

  char text[512];

  XEvent event;
  int counter = 0;
  int killed = 0;

  while (!killed) {
    counter++;
    sprintf(text, "Hallo Welt %d", counter);

    if (XEventsQueued(display, QueuedAlready) > 0) {
      XNextEvent(display, &event);

      switch (event.type) {
      case ConfigureNotify:
        XGetWindowAttributes(display, window, &attributes);
        XFreePixmap(display, buffer);
        buffer = XCreatePixmap(display, window, attributes.width, attributes.height, attributes.depth);
        XftDrawDestroy(xft);
        xft = XftDrawCreate(display, buffer, visual, cmap);
        break;
      case MapNotify:
      case Expose:;
        XftDrawRect(xft, &black, 0, 0, attributes.width, attributes.height);
        XCopyArea(display, buffer, window, graphics, 0, 0, attributes.width, attributes.height, 0, 0);
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

    XftDrawRect(xft, &black, 0, 0, attributes.width, attributes.height);
    XftDrawString8(xft, &green, font, 10, 10, (unsigned char *) text, strlen(text));
    XCopyArea(display, buffer, window, graphics, 0, 0, attributes.width, attributes.height, 0, 0);
    XSync(display, 0);

    usleep(33000);
  }

  XftColorFree(display, visual, cmap, &green);
  XftColorFree(display, visual, cmap, &black);
  XftDrawDestroy(xft);
  XFreeGC(display, graphics);
  XCloseDisplay(display);
  return 0;
}
