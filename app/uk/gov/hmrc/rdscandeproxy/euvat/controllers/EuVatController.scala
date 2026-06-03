/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdscandeproxy.euvat.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdscandeproxy.actions.AuthAction
import uk.gov.hmrc.rdscandeproxy.euvat.services.EuVatService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EuVatController @Inject() (authorise: AuthAction, euVatService: EuVatService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def retrieveTraderByVrn(): Action[AnyContent] =
    authorise.async:
      implicit request =>
        println("********* calling service")
        euVatService
          .retrieveTraderByVrn(123) // TODO - dummy vrn
          .map(result =>
            println(s"********* response received from service: $result")
            Ok(Json.toJson(result))
          )
          .recover { case ex: Exception =>
            logger.warn("Error while retrieving traders known facts from oracle database", ex)
            InternalServerError("Failed to retrieve traders known facts")
          }

}
