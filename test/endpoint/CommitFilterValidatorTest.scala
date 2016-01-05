package endpoint

import model.Error
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.FunSuite
import org.scalatest.Matchers._


import dao.FilterParameters
class CommitFilterValidatorTest extends FunSuite with CommitFilterValidator {
  val since = Some("sinceId")
  val until = Some("untilId")
  val isValidTrue = Some(true)
  val isValidFalse = Some(false)
  val fromDateString = Some("2015-01-04T15:07:58.989+01:00")
  val fromDate=fromDateString.map(ISODateTimeFormat.dateTimeParser().parseDateTime(_))
  val toDateString = Some("2016-01-04T15:07:58.989+01:00")
  val toDate=toDateString.map(ISODateTimeFormat.dateTimeParser().parseDateTime(_))
  val unvalidDate = Some("unvalidDate")

  test("Validations#validate with empty params(None's) should always be VALID") {
    validate(None, None, None, None, None)(400, INVALID_INPUT) shouldBe Right(FilterParameters(None, None, None, None, None))
  }
  test("Validations#validate with [since, until] and [isValid=true] should always be VALID") {
    validate(since, until, isValidTrue, None, None)(400, INVALID_INPUT) shouldBe Right(FilterParameters(since, until, isValidTrue, None, None))
  }
  test("Validations#validate with [since/untill] and  [fromDate/toDate] filled should always be UNVALID") {
	  validate(None, until, None, fromDateString, toDateString)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
	  validate(None, until, None, None, toDateString)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
	  validate(None, until, None, fromDateString, None)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
	  validate(since, None, None, fromDateString, toDateString)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
	  validate(since, None, None, None, toDateString)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
	  validate(since, None, None, fromDateString, None)(400, INVALID_INPUT) shouldBe Left(Error(combinedNotAllowedError, 400, INVALID_INPUT))
  }
  test("Validations#validate with [fromDate, toDate] and [isValid=true] should always be VALID") {
    validate(None, None, isValidFalse, None, toDateString)(400, INVALID_INPUT) shouldBe Right(FilterParameters(None, None, isValidFalse, None, toDate))
    validate(None, None, isValidFalse, fromDateString, None)(400, INVALID_INPUT) shouldBe Right(FilterParameters(None, None, isValidFalse, fromDate, None))
  }
  test("Validations#validate a unvalid [fromDate, toDate] with [isValid=true] should always be UNVALID") {
	  validate(None, None, isValidFalse, unvalidDate, None)(400, INVALID_INPUT) shouldBe Left(Error(dateTimeParsingError, 400, INVALID_INPUT))
	  validate(None, None, isValidFalse, None, unvalidDate)(400, INVALID_INPUT) shouldBe Left(Error(dateTimeParsingError, 400, INVALID_INPUT))
  }
}
 