package net.benlamlih
package domain.account

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.all.NonEmptyString
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.util.UUID as JavaUUID

class AccountServiceTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var mockAccountRepository: AccountRepository = _
  var service: AccountService                  = _
  var uuidString: String                       = _
  var accountId: Refined[String, Uuid]         = _
  var account: Account                         = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    mockAccountRepository = mock[AccountRepository]
    service = new AccountService(mockAccountRepository)
    uuidString = JavaUUID.randomUUID().toString
    accountId = Refined.unsafeApply[String, Uuid](uuidString)

    val fullName: NonEmptyString = Refined.unsafeApply("Mohammed Benlamlih")
    val email: Email             = Refined.unsafeApply("benlamlih.med@gmail.com")
    val phoneNumber: PhoneNumber = Refined.unsafeApply("+33659554266")
    account = Account(accountId, fullName, email, phoneNumber, BigDecimal(1000), Currency.EUR)
  }

  "createAccount" should "successfully create an account" in {
    when(mockAccountRepository.create(any[Account])).thenReturn(IO.unit)

    val result = service
      .createAccount(account.fullName, account.email, account.phoneNumber, account.balance, account.currency)
      .unsafeRunSync()

    result.fullName shouldBe account.fullName
    result.email shouldBe account.email
    result.phoneNumber shouldBe account.phoneNumber
    result.balance shouldBe account.balance
    result.currency shouldBe account.currency
    verify(mockAccountRepository).create(any[Account])
  }

  "getAccount" should "retrieve a specific account" in {
    when(mockAccountRepository.retrieve(accountId)).thenReturn(IO.pure(Some(account)))

    val result = service.getAccount(accountId).unsafeRunSync()

    result should not be empty
    result.get shouldBe account
  }

  "getAllAccounts" should "retrieve all accounts" in {
    val accounts = List(account)

    when(mockAccountRepository.retrieveAll()).thenReturn(IO.pure(accounts))

    val result = service.getAllAccounts.unsafeRunSync()

    result should have size 1
    result.head shouldBe account
  }

  "getBalance" should "retrieve the balance of an account" in {
    when(mockAccountRepository.retrieve(accountId)).thenReturn(IO.pure(Some(account)))

    val result = service.getBalance(accountId).unsafeRunSync()

    result shouldBe account.balance
  }

  "updateAccount" should "successfully update an account" in {
    when(mockAccountRepository.update(any[Account])).thenReturn(IO.unit)

    val updatedAccount = account.copy(fullName = Refined.unsafeApply("Mohammed Benlamlih"))
    val result         = service
      .updateAccount(
        accountId,
        updatedAccount.fullName,
        updatedAccount.email,
        updatedAccount.phoneNumber,
        updatedAccount.balance,
        updatedAccount.currency
      )
      .unsafeRunSync()

    result should not be empty
    result.get.fullName shouldBe updatedAccount.fullName
    verify(mockAccountRepository).update(any[Account])
  }

  "deleteAccount" should "successfully delete an account" in {
    when(mockAccountRepository.delete(accountId)).thenReturn(IO.unit)

    noException should be thrownBy service.deleteAccount(accountId).unsafeRunSync()

    verify(mockAccountRepository).delete(accountId)
  }
}
