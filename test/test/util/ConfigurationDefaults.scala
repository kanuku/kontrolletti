package test.util

object ConfigurationDefaults {
  object SCMConfigurationDefaults {

    //github hosts
    val ghost = "github.com"
    val ghehost = "github-enterprise.company.io"
    //stash host
    val shost = "stash.com"
    //OAuth
    val githubAccessToken = "IfyividCysaiTwacEabWytNararacDuKuesyapnaunairtyunhyecidCowfoshIn"
    val githubEnterpriseAccessToken = "VucdamishGivogcudiphQuonJeevEtKekdajLaithaugjunBevReajNeQuidIrd6"
    val stashAccessToken = "cramTinMejWetNuvcaddonckEenAjhofyafcykOnfiWoadPechlidduffAygyiv9"
    val stashUser = "stashUser"
    // Precedents
    val httpsAPI = "https://api."
    val https = "https://"

    def scmConfigurations: Map[String, _] = Map(
      // github
      "client.scm.github.host.0" -> ghost,
      "client.scm.github.urlPrecedent.0" -> httpsAPI,
      "client.scm.github.authToken.0" -> githubAccessToken,

      //github-enterprise
      "client.scm.github.host.1" -> ghehost,
      "client.scm.github.urlPrecedent.1" -> https,
      "client.scm.github.authToken.1" -> githubEnterpriseAccessToken,

      //stash
      "client.scm.stash.host.0" -> shost,
      "client.scm.stash.urlPrecedent.0" -> https,
      "client.scm.stash.authToken.0" -> stashAccessToken,
      "client.scm.stash.user.0" -> stashUser)
  }
}