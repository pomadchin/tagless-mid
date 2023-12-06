# Tagless Mid

[![CI](https://github.com/pomadchin/tagless-mid/actions/workflows/ci.yml/badge.svg)](https://github.com/pomadchin/tagless-mid/actions/workflows/ci.yml)
[![Maven Badge](https://img.shields.io/maven-central/v/io.github.pomadchin/tagless-mid-core_3?color=blue)](https://central.sonatype.com/search?q=g%3Aio.github.pomadchin&smo=true&name=tagless-mid-core_3)
[![Snapshots Badge](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.github.pomadchin/tagless-mid-core_3)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/pomadchin/tagless-mid-core_3/)

This library is [ToFu.Mid](https://github.com/tofu-tf/tofu/blob/v0.12.1/modules/kernel/higherKind/src/main/scala-2/tofu/higherKind/Mid.scala) implementation separated out of the [ToFu library](https://github.com/tofu-tf/tofu).

## Quick Start with SBT

```scala
// it is published to maven central, but you can use the following repos in addition
resolvers ++= 
  Resolver.sonatypeOssRepos("releases") ++ 
  Resolver.sonatypeOssRepos("snapshots") // for snaphots

// `<latest version>` refers to the version indicated by the badge above
libraryDependencies += "io.github.pomadchin" %% "tagless-mid-core" % "<latest version>"
```

## License
Code is provided under the Apache 2.0 license available at http://opensource.org/licenses/Apache-2.0,
as well as in the LICENSE file. This is the same license used as Spark.
