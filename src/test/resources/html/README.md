hosted at static.wditp.zylitics.io (wdtip = webdriver test internal pages)
Prettify is done using https://htmlformatter.com/
All sites must be compatible with IE11.
Note that all sites have content inside a frame, the reason behind that is:
Previously they were created using jsfiddle, I later learned that jsfiddle
doesn't work on IE11 at all and decide to create and host my own sites. Since
the zwl code used the jsfiddle structure where the content has to be in a
frame called 'result'. To save the time, I ported jsffidle sites as is into
new sites using a frame, same name and same html/js/css structure. 