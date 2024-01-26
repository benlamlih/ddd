package net.benlamlih
package domain.account
import domain.account.*
import domain.wallet.WalletService

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.Uuid
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.util.UUID as JavaUUID

class WalletServiceTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var mockAccountRepository: AccountRepository = _
  var mockAccountService: AccountService       = _
  var walletService: WalletService             = _

  var fullName: Refined[String, NonEmpty]                 = _
  var email: Refined[String, EmailConstraint]             = _
  var phoneNumber: Refined[String, PhoneNumberConstraint] = _
  var balance: BigDecimal                                 = _
  var amount: BigDecimal                                  = _
  var currency: Currency                                  = _
  var uuidString: String                                  = _
  var accountUuid: Refined[String, Uuid]                  = _
  var account: Account                                    = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAccountRepository = mock[AccountRepository]
    mockAccountService = mock[AccountService]
    walletService = new WalletService(mockAccountRepository, mockAccountService)
    fullName = Refined.unsafeApply[String, NonEmpty]("John Doe")
    email = Refined.unsafeApply[String, EmailConstraint]("john@example.com")
    phoneNumber = Refined.unsafeApply[String, PhoneNumberConstraint]("+33659554266")
    balance = BigDecimal(100.0)
    amount = BigDecimal(100.0)
    currency = Currency.EUR
    uuidString = JavaUUID.randomUUID().toString
    accountUuid = Refined.unsafeApply[String, Uuid](uuidString)
    account = Account(accountUuid, fullName, email, phoneNumber, balance, currency)
  }

  "WalletService" should "correctly fund an account" in {
    val expectedUpdatedAccount =
      account.copy(balance = account.balance + walletService.convertToEUR(amount, currency))

    when(mockAccountRepository.retrieve(accountUuid)).thenReturn(IO.pure(Some(account)))
    when(mockAccountRepository.update(expectedUpdatedAccount)).thenReturn(IO.unit)

    val result = walletService.fundWallet(accountUuid, amount, currency).unsafeRunSync()

    result shouldBe expectedUpdatedAccount
  }

  it should "convert currency amounts to EUR correctly" in {
    val service = new WalletService(null, null)

    service.convertToEUR(BigDecimal(100), Currency.USD) shouldBe BigDecimal(89.0)
    service.convertToEUR(BigDecimal(100), Currency.GBP) shouldBe BigDecimal(117.0)
    service.convertToEUR(BigDecimal(100), Currency.JPY) shouldBe BigDecimal(0.74)
    service.convertToEUR(BigDecimal(100), Currency.CHF) shouldBe BigDecimal(95.0)
    service.convertToEUR(BigDecimal(100), Currency.EUR) shouldBe BigDecimal(100.0)
  }

  "deductFromBalance" should "successfully deduct from the balance" in {
    val amountToDeduct = BigDecimal(50)

    when(mockAccountService.getBalance(accountUuid)).thenReturn(IO.pure(balance))
    when(mockAccountRepository.retrieve(accountUuid)).thenReturn(IO.pure(Some(account)))
    when(mockAccountRepository.update(account.copy(balance = balance - amountToDeduct))).thenReturn(IO.unit)

    walletService.deductFromBalance(accountUuid, amountToDeduct).unsafeRunSync()

    verify(mockAccountService, times(2)).getBalance(accountUuid)
    verify(mockAccountRepository).retrieve(accountUuid)
    verify(mockAccountRepository).update(account.copy(balance = balance - amountToDeduct))

  }

  "deductFromBalance" should "fail to deduct from the balance due to insufficient funds" in {
    val amountToDeduct = BigDecimal(200)

    when(mockAccountService.getBalance(accountUuid)).thenReturn(IO.pure(balance))
    when(mockAccountRepository.retrieve(accountUuid)).thenReturn(IO.pure(Some(account)))

    assertThrows[Exception] {
      walletService.deductFromBalance(accountUuid, amountToDeduct).unsafeRunSync()
    }
  }
}
