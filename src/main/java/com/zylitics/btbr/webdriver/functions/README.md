Will contain all webdriver functions to be added into ZWL. If any implementation require custom
code to support different browsers, it will do it inline conditionally rather than creating browser
specific extension class so we can simply add all these to list at runtime without having to use
reflection to detect existence of browser specific class and without having to have duplicate
browser extension classes of all function. We gonna have lot of functions here and hopefully
custom browser based functionality isn't needed in many.  