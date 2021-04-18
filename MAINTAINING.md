# Maintaining PureConfig

This document outlines useful information for PureConfig maintainers to do day-to-day tasks like releasing new versions.

# Releasing a new version

There are a few steps we follow every time we publish a new PureConfig version to Sonatype:

1. Update CHANGELOG with an entry for the new version
    - You can explore all the commits since the last version with a link [like this](https://github.com/pureconfig/pureconfig/compare/v0.14.1...master).
    - Consider what the next version will be. If the new release contains any breaking changes, please bump the minor version (we usually consider a breaking change to be one that is [source incompatibile](https://en.wikipedia.org/wiki/Source-code_compatibility)).
1. If needed, update the version in `version.sbt`
    - At this point, the version should still be a snapshot of the version to be released. If you are releasing version `x.y.z`, the version in `version.sbt` should be `x.y.z-SNAPSHOT`.
1. Run `sbt mdoc`
    - `mdoc` uses the latest CHANGELOG entry to determine the latest stable version and exposes it as a macro. You should expect changes in generated READMEs.
1. Commit changes locally (without pushing them)
    - Example commit: 877528e6e67a202b033c629eed94c6ea1fbed1d6
1. Run `sbt release`
    - This step requires you to have a Sonatype account correctly configured (see instructions [here](https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html#step+3%3A+Credentials)) and your account should have access to the com.github.pureconfig project there.
    - sbt will ask a few questions regarding version numbers. The defaults should all be correct, but please double-check everything.
    - When asked if you want to push changes to the remote repository, please do so.
    - Be patient - this process will take several minutes!
1. Check out the code of the newly released version
    - e.g. `git checkout v0.15.0`
1. Run `./scripts/update_website.sh`
    - This will update PureConfig's website with the docs for the newly released version.
    - The script will pause before actually publishing the changes. If you want to check the website will look before you commit, you can run `jekyll serve` in `docs/target/website`.
    - Either before or after publishing, please check that https://pureconfig.github.io/docs shows the correct version and that a frozen version of the docs for your new version was generated (e.g. https://pureconfig.github.io/v0.15.0).
