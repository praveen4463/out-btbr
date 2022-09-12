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

## v0.2.17

Minor enhancements and bug fixed

### Enhancements

1. Upgraded `zwl` to 0.6.2.
2. Added support for `callTest` function

## BugFixes

1. Driver quit isn't attempted when there is error in interacting with browser

## v0.2.18

Minor enhancements and bug fixed

### Enhancements

1. Upgraded `zwl` to 0.6.3.

## BugFixes

1. In callTest, error should say 'test' rather than file.
2. storage clearing shouldn't throw error as it leads to no driver close.

## v0.2.19

Minor enhancements

### Enhancements

1. Added up team sharing. Changed queries accordingly.

## v0.2.20

Minor enhancements

### Enhancements

1. Bringing back the previous shot capture, more shots per sec.

## v0.2.21

Minor enhancements

### Enhancements

1. Supported CI/CD.

## v0.2.22

Minor enhancements

### Enhancements

1. Supported shots and logs preferences.

## v0.2.23

Bug fixes

### BugFixes

1. Fixed a bug in chrome driver. We were still giving log related properties even when we were asked
not to log. Now no log properties will be set unless logs are enabled.
2. Firefox driver was failing to reach `about:blank` sporadically. Now it has been changed to `data:,`
just for firefox.

## v0.2.24

Major enhancements

### Enhancements

1. Added support for email notification on build completion.

## v0.2.25

Major enhancements

### Enhancements

1. Added retry feature in tests

## v0.3.0

Major enhancements

### Enhancements

1. Added mobile emulation support
2. Bumped zwl to 0.6.4

## v0.3.1

Minor enhancements

### Enhancements

1. Record url of current window upon error
2. Take a few more screenshots upon error

## v0.3.2

Bug fixes

### BugFixes

1. Upon error, get the current url only if the error is a Zwl exception. 

## v0.3.3

Minor enhancements

### Enhancements

1. Updated build emails link to build to have `simple_view` in qs.

## v0.3.4

Bug fixes

### BugFixes

1. Added missing project_id condition in a sql for fetching tests used in `callTest`.

## v0.3.5

Enhancements

### Enhancements

1. Bumped zwl to 0.6.5

## v0.3.6

Enhancements

### Enhancements

1. Bumped zwl to 0.6.6

## v0.3.7

Enhancements

### Enhancements

1. Added error in email

## v0.3.8

Bug fixes

### BugFixes

1. Trying to fix chrome timeout in long-running scripts using some chrome options.

## v0.3.9

### Enhancements

1. Bumped zwl to 0.6.7

## v0.3.10

### Enhancements

1. Added timestamps to alert email

## v0.3.11

### Enhancements

1. Bumped zwl to 0.6.8

## v0.3.12

### Enhancements

1. Bumped zwl to 0.6.9

## v0.3.13

### Enhancements

1. Bumped zwl to 0.6.10
2. Fixed timezone and format in Failed At date.

## v0.3.14

### Enhancements

1. Bumped zwl to 0.6.11
2. User's upload directory now is organization's
3. Bumped up libraries

## v0.3.15

### Enhancements

1. Bumped zwl to 0.6.12

## v0.3.16

### Enhancements

1. Bumped zwl to 0.6.13
2. `_global` is now provided to zwl from runner.
3. While calling a function, version name is no longer required if it's v1.
4. Currently running Test Path is also provided from runner.

## v0.3.17

### Enhancements

1. Bumped zwl to 0.6.14
2. Added support to run build from git

## v0.3.18

Bug fixes

### BugFixes

1. Fixed a bug while fetching build

## v0.3.19

Bug fixes

### BugFixes

1. Git was associated with an org rather than a project. Fixed that.

## v0.3.20

Bug fixes

### BugFixes

1. Fixed a bug in a sql query.