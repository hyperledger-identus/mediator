package org.hyperledger.identus.mediator

import zio.*
import zio.http.*

object TraceIdMiddleware {

  private val NAME_TRACE_ID = "requestid"

  private def requestId(req: Request) = ZIO.logAnnotateScoped(
    req.headers
      .find(h => h.headerName.equalsIgnoreCase("x-request-id"))
      .toSet
      .map(h => LogAnnotation(NAME_TRACE_ID, h.renderedValue))
  )

  def addTraceId[R] = {
    HandlerAspect.interceptHandler[R, Unit](
      Handler.fromFunctionZIO[Request] { request =>
        ZIO.scoped {
          requestId(request).map(_ => (request, ()))
        }
      }
    )(Handler.identity)
  }

}
