import Dependencies.Version._

micrositeName := "PureConfig"
micrositeDescription := "A boilerplate-free library for loading configuration files"
micrositeAuthor := "com.github.pureconfig"
micrositeHomepage := "https://pureconfig.github.io/"
//micrositeBaseUrl := "pureconfig", // keep this empty to not have a base URL
micrositeDocumentationUrl := "docs/"
micrositeGithubOwner := "pureconfig"
micrositeGithubRepo := "pureconfig"
micrositeTheme := "pattern"
micrositeHighlightTheme := "default"
micrositePalette := Map(
  "brand-primary" -> "#ab4b4b", // link color
  "brand-secondary" -> "#4b4b4b", // nav/sidebar back
  "brand-tertiary" -> "#292929", // sidebar top back
  "gray-dark" -> "#453E46", // section title
  "gray" -> "#837F84", // text color
  "gray-light" -> "#E3E2E3", // star back
  "gray-lighter" -> "#F4F3F4", // code back
  "white-color" -> "#FFFFFF"
)
micrositeGitterChannel := false // ugly

mdocExtraArguments += "--no-link-hygiene"
mdocVariables := Map("VERSION" -> version.value)

publish / skip := true
