#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
 This small application is used to block until a file with a given filename is
 created on the filesystem under a specified directory.
"""

import logging
import sys
import time

from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

logging.basicConfig(level=logging.ERROR)

class MyEventHandler(FileSystemEventHandler):
    def __init__(self, observer, filename):
        self.observer = observer
        self.filename = filename

    def on_created(self, event):
        if not event.is_directory and event.src_path.endswith(self.filename):
            self.observer.stop()
                
def main(argv=None):
    path = argv[1]
    filename = argv[2]

    observer = Observer()
    event_handler = MyEventHandler(observer, filename)
    
    observed_watch = observer.schedule(event_handler, path, recursive=False)
    observer.start()
    
    # used to have the possibility to intercept keyboard interrupts
    try:
        while observer.should_keep_running():
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    
    observer.join()

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))
