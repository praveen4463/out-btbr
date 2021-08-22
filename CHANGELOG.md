##v0.1.0

First version completed.

## v0.2.2

Added auth and other refactoring

## v0.2.3

* To mark VM as available when a test done, just send a flag rather than adding labels
* In browser session config, mention exact browser names to be consistent
* sending extra params to build script such as timezone

## v0.2.4

* waiting until pre-build script has downloaded the driver should it is unavailable
* fixed logback config to be able to debug app
* fix in process builder

## v0.2.5

* fixed problem in ProductionVmService, it wasn't able to contact wzgp, using WebClient now

## v0.2.6

* There was still problem in contacting wzgp unexpectedly, fixed that now
* Runner wasn't able to a ps1 file, now using a bat as before as a bridge to the ps1

## v0.2.7

* Reverted the manual wait while downloading driver, the wait for build prepare script works
  and is tested.

## v0.2.8

* local resources like esdb, auth of local clients shouldn't require to use production secrets as it
  compromises secret's security when transmitting in local network.
* fixed issue in ID driver as it wasn't storing driver logs

## v0.2.9

* Upgraded zwl to 0.4.2

## v0.2.10

Minor enhancements

### Enhancements

1. Cleaning up a finished test before driver quit.
2. Upgraded `zwl` to 0.4.3.

## v0.2.11

Tests

### Missed tests

1. Wrote and fixed tests for `v0.2.10` that I missed in last release. Was a mistake.

## v0.2.12

Minor enhancements

### Enhancements

1. Upgraded `zwl` to 0.4.4.

## v0.2.13

Bug fixes

### BugFixes

1. Don't log exception when stop has occurred
2. Put cleanup on build finish code in try-catch so that if it fails for some reason, we still can
   finish the process.

## v0.2.14

Minor enhancements

### Enhancements

1. Upgraded `zwl` to 0.4.5.

## v0.2.15

Minor enhancements

### Enhancements

1. Upgraded `zwl` to 0.6.0.

## v0.2.16

Minor enhancements and bug fixed

### Enhancements

1. Upgraded `zwl` to 0.6.1.
2. Refactored some redundant code
3. Even when it's IDE request, marking build request completed once all done. The reason
   is, sometimes ppl re-start very fast leading to new server start that will take
   2+ mins leading to more frustration that waiting for 2-3 seconds more for assets to
   upload.

## BugFixes

1. Retrying on errors when contacting grid provisioner service.