package client.scm


object SCMModel {

  sealed trait SCMResource { type ResourceId }
  case object SCMCommit extends SCMResource { type ResourceId = String }
  case object SCMRepo extends SCMResource { type ResourceId = String }

  case class SCMResponse[Resource <: SCMResource](
    httpStatus: Int,
    headers: Map[String, Seq[String]],
    body: String
  )

  sealed trait SCMPagenation[Resource <: SCMResource]
  case class FirstPage[Resource <: SCMResource](
    resId: Option[Resource#ResourceId] // start of page. concrete meaning depends on different SCM
  ) extends SCMPagenation[Resource]
  case class NextPage[Resource <: SCMResource, Next](
    resId: Option[Resource#ResourceId], // start of page. concrete meaning depends on different SCM
    nextParam: Next
  )
}
