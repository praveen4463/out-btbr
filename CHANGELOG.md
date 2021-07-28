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