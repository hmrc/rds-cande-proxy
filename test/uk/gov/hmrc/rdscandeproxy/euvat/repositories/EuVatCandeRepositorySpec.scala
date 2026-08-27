/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdscandeproxy.euvat.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.*
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.{ApplicationResponse, DeletePurchaseResponse, GetPurchaseDetailsResponse}

import java.sql.{CallableStatement, Connection, ResultSet}
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class EuVatCandeRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: EuVatCandeRepository = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    // Mocking the database connection and callable statement
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection) // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any())).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new EuVatCandeRepository(db)
  }

  "getLatestApplications" should "return a LatestApplicationResponse with correct data" in {
    val request = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = Some("LV"),
      startDate             = Some(LocalDateTime.of(2025, 2, 1, 0, 0)),
      endDate               = Some(LocalDateTime.of(2025, 5, 31, 0, 0)),
      representativeId      = Some("rep123"),
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    when(mockCallableStatement.getObject("p_applications", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_total_applications")).thenReturn(1)

    when(mockResultSet.next()).thenReturn(true, false)
    when(mockResultSet.getLong("application_id")).thenReturn(133L)
    when(mockResultSet.getString("refunding_country_code")).thenReturn("LV")
    when(mockResultSet.getTimestamp("period_start_date")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 2, 1, 0, 0)))
    when(mockResultSet.getTimestamp("period_end_date")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 5, 31, 23, 59)))
    when(mockResultSet.getString("application_number")).thenReturn("GB0000000000000133")
    when(mockResultSet.getString("application_status")).thenReturn("D")
    when(mockResultSet.getString("submission_status")).thenReturn("S")
    when(mockResultSet.getTimestamp("application_version")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 2, 11, 10, 38)))

    val result = repository.getLatestApplications(request).futureValue

    result.totalApplication                       shouldBe 1
    result.applications.head.applicationId        shouldBe 133L
    result.applications.head.refundingCountryCode shouldBe "LV"
  }

  "getLatestApplications" should "return empty list when no applications found" in {
    val request = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = Some("LV"),
      startDate             = Some(LocalDateTime.of(2025, 2, 1, 0, 0)),
      endDate               = Some(LocalDateTime.of(2025, 5, 31, 0, 0)),
      representativeId      = Some("rep123"),
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    when(mockCallableStatement.getObject("p_applications", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_total_applications")).thenReturn(0)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getLatestApplications(request).futureValue

    result.totalApplication shouldBe 0
    result.applications     shouldBe List.empty
  }

  "getLatestApplications" should "set null params when optional fields are absent" in {
    val request = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = None,
      startDate             = None,
      endDate               = None,
      representativeId      = None,
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    when(mockCallableStatement.getObject("p_applications", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_total_applications")).thenReturn(0)
    when(mockResultSet.next()).thenReturn(false)

    repository.getLatestApplications(request).futureValue

    verify(mockCallableStatement).setNull("p_refunding_country", java.sql.Types.VARCHAR)
    verify(mockCallableStatement).setNull("p_start_date", java.sql.Types.DATE)
    verify(mockCallableStatement).setNull("p_end_date", java.sql.Types.DATE)
    verify(mockCallableStatement).setNull("p_representative_id", java.sql.Types.VARCHAR)
  }

  "addApplication" should "return saved application response" in {
    val appRequest: ApplicationRequest = ApplicationRequest(
      refundingCountryCode          = Some("FR"),
      periodStartDate               = Some(LocalDateTime.of(2025, 1, 1, 0, 0, 0)),
      periodEndDate                 = Some(LocalDateTime.of(2025, 3, 31, 23, 59, 59)),
      applicantEmailAddress         = Some("test@email.com"),
      applicantTelephoneNumber      = Some("0123456789"),
      applicationLanguage           = Some("EN"),
      businessActivityCode1         = Some("7090"),
      businessActivityCode2         = Some("8903"),
      businessActivityCode3         = None,
      representativeId              = None,
      representativeCountryCode     = None,
      representativeEmailAddress    = None,
      representativeIdType          = None,
      representativeTelephoneNumber = None,
      bankAccountOwnerName          = None,
      bankAccountOwnerType          = None,
      iBanCode                      = None,
      bicCode                       = None,
      bankAccountCurrencyCode       = None
    )

    val applicationResponse: ApplicationResponse = ApplicationResponse(1, "GB123456", 1)

    // Mock output parameters
    when(mockCallableStatement.getInt("p_application_id")).thenReturn(1)
    when(mockCallableStatement.getString("p_application_number")).thenReturn("GB123456")
    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(1)

    val result = await(repository.addApplication(appRequest, "123456789"))

    result shouldBe applicationResponse
  }

  "addPurchase" should "return purchase response" in {
    val purchaseRequest: AddPurchaseRequest = AddPurchaseRequest(
      applicationId              = 123456,
      goodsDescriptionCategory   = "1",
      goodsDescriptionText       = Some("Fuel"),
      purchaseSubcategory        = None,
      simplifiedInvoiceIndicator = None,
      supplierName               = None,
      supplierAddress1           = None,
      supplierAddress2           = None,
      supplierAddress3           = None,
      supplierVatRegNumber       = None,
      supplierTaxIdentifier      = None,
      invoiceDate                = None,
      invoiceNumber              = None,
      currencyCode               = None,
      taxableAmount              = None,
      vatAmount                  = None,
      deductibleVatAmount        = None,
      updateSequenceNumber       = 1
    )

    val purchaseResponse: AddPurchaseResponse = AddPurchaseResponse(itemNumber = 4, updateSequenceNumber = 1)

    // Mock output parameters
    when(mockCallableStatement.getInt("p_item_number")).thenReturn(4)
    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(1)

    val result = await(repository.addPurchase(purchaseRequest))

    result shouldBe purchaseResponse
  }

  "getPurchaseDetails" should "return the purchase record with the update sequence number" in {
    val request = GetPurchaseDetailsRequest(applicationId = 123456, itemNumber = 4)

    when(mockCallableStatement.getObject("p_purchase_details", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(7)

    when(mockResultSet.next()).thenReturn(true, false)
    when(mockResultSet.getString("goods_description_category")).thenReturn("1")
    when(mockResultSet.getString("goods_description_subcategory")).thenReturn("1.1")
    when(mockResultSet.getString("goods_description_text")).thenReturn("Fuel")
    when(mockResultSet.getString("simplified_invoice_indicator")).thenReturn(null)
    when(mockResultSet.getString("supplier_name")).thenReturn("Supplier Ltd")
    when(mockResultSet.getString("supplier_address_1")).thenReturn("1 High Street")
    when(mockResultSet.getString("supplier_address_2")).thenReturn(null)
    when(mockResultSet.getString("supplier_address_3")).thenReturn(null)
    when(mockResultSet.getString("supplier_vat_reg_number")).thenReturn("LV40003567907")
    when(mockResultSet.getString("supplier_tax_identifier")).thenReturn(null)
    when(mockResultSet.getTimestamp("invoice_date")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 3, 15, 0, 0)))
    when(mockResultSet.getString("invoice_number")).thenReturn("INV-001")
    when(mockResultSet.getString("currency_code")).thenReturn("EUR")
    when(mockResultSet.getBigDecimal("taxable_amount")).thenReturn(new java.math.BigDecimal("100.50"))
    when(mockResultSet.getBigDecimal("vat_amount")).thenReturn(new java.math.BigDecimal("21.10"))
    when(mockResultSet.getBigDecimal("deductible_vat_amount")).thenReturn(null)

    val result = repository.getPurchaseDetails(request).futureValue

    result shouldBe Some(
      GetPurchaseDetailsResponse(
        goodsDescriptionCode       = "1",
        goodsDescriptionSubCode    = Some("1.1"),
        goodsDescriptionText       = Some("Fuel"),
        simplifiedInvoiceIndicator = None,
        supplierName               = Some("Supplier Ltd"),
        supplierAddressLine1       = Some("1 High Street"),
        supplierAddressLine2       = None,
        supplierAddressLine3       = None,
        supplierVatNumber          = Some("LV40003567907"),
        supplierTaxIdentifier      = None,
        invoiceDate                = Some(LocalDateTime.of(2025, 3, 15, 0, 0)),
        invoiceNumber              = Some("INV-001"),
        currencyCode               = Some("EUR"),
        taxableAmount              = Some(BigDecimal("100.50")),
        vatAmount                  = Some(BigDecimal("21.10")),
        deductibleVatAmount        = None,
        updateSequenceNumber       = 7
      )
    )
  }

  "getPurchaseDetails" should "return None when the cursor holds no purchase record" in {
    val request = GetPurchaseDetailsRequest(applicationId = 123456, itemNumber = 99)

    when(mockCallableStatement.getObject("p_purchase_details", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(7)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getPurchaseDetails(request).futureValue

    result shouldBe None
  }

  "deletePurchaseDetails" should "return the new update sequence number after the delete" in {
    val request = DeletePurchaseRequest(applicationId = 123456, itemNumber = 4, updateSequenceNumber = 7)

    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(8)

    val result = repository.deletePurchaseDetails(request).futureValue

    result shouldBe DeletePurchaseResponse(updateSequenceNumber = 8)
    verify(mockCallableStatement).setLong("p_application_id", 123456L)
    verify(mockCallableStatement).setInt("p_item_number", 4)
    verify(mockCallableStatement).setInt("p_update_seq_number", 7)
  }

  "getSupplierVrnCount" should "return a SupplierVrnCountResponse with the duplicate count" in {
    val request = SupplierVrnCountRequest(
      applicationId = 133,
      itemNumber    = 4,
      vatNumber     = "500000881",
      invoiceNumber = "a444"
    )

    when(mockCallableStatement.getInt("p_count")).thenReturn(3)
    val result = repository.getSupplierVrnCount(request).futureValue
    result.duplicateCount shouldBe 3
  }

  "getSupplierVrnCount" should "return zero when no duplicates exist" in {
    val request = SupplierVrnCountRequest(
      applicationId = 133,
      itemNumber    = 4,
      vatNumber     = "500000881",
      invoiceNumber = "a444"
    )

    when(mockCallableStatement.getInt("p_count")).thenReturn(0)
    val result = repository.getSupplierVrnCount(request).futureValue
    result.duplicateCount shouldBe 0
  }

  "getSupplierTaxIdentifierDuplicateCount" should "return the duplicate count from proc" in {
    val req = uk.gov.hmrc.rdscandeproxy.euvat.models.requests.SupplierTaxIdentifierCountRequest(
      applicationId = 133,
      itemNumber    = 4,
      taxIdentifier = "500000881",
      invoiceNumber = "a444"
    )

    when(mockCallableStatement.getInt("p_count")).thenReturn(4)
    val result = repository.getSupplierTaxIdentifierDuplicateCount(req).futureValue
    result shouldBe 4
  }

}
