Will contain all webdriver functions to be added into ZWL. If any implementation require custom
code to support different browsers, we'll extend a browser specific class by appending the browser
name in the end to the original class name. After all the original classes have loaded, we'll
look what is the desired browser and based on that we'll delete classes that have browser specific
classes available (manually, no reflection). So whenever a new browser specific class is added,
we'll manually update the list of classes to be added and removed. This way we'll implement browser
specific classes only for the browser's that require it and only for the functions that have
browser specific behaviour.